package br.edu.ifba.inf008.interfaces.database;

import br.edu.ifba.inf008.interfaces.controller.ICore;
import br.edu.ifba.inf008.interfaces.controller.IDatabaseController;
import br.edu.ifba.inf008.interfaces.controller.IPlugin;

import br.edu.ifba.inf008.interfaces.controller.util.ControllerUtil;

import br.edu.ifba.inf008.interfaces.database.exceptions.*;
import br.edu.ifba.inf008.interfaces.database.annotations.*;
import br.edu.ifba.inf008.interfaces.database.util.*;

import java.lang.StringBuilder;

import java.sql.Timestamp;

import java.lang.reflect.Field;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import java.util.List;
import java.util.Map;

public interface IDatabasePlugin<T extends IEntity> extends IPlugin {
    abstract Class<T> getEntityClazz();

    default boolean delete(final T toBeDeleted) {

        // ========== Getting controllers ==========

        final IDatabaseController database = ControllerUtil.tryGetAndValidateDatabaseController();
        if (database == null) return false;

        // ========== Getting Query ==========

        if (toBeDeleted == null) return false;
        final QueryData deleteQuery = toBeDeleted.getDeleteQuery();
        if (deleteQuery == null) return false;

        // ========== Executing Query ==========

        final int rowsAffected = database.executeNonQuery(deleteQuery.getSql(), deleteQuery.getParameters());

        return rowsAffected == 1;
    }

    default boolean update(final T toBeUpdated) {

        // ========== Getting controllers ==========

        final IDatabaseController database = ControllerUtil.tryGetAndValidateDatabaseController();
        if (database == null) return false;

        // ========== Getting Query ==========

        if (toBeUpdated == null) return false;
        final QueryData updateQuery = toBeUpdated.getUpdateQuery();
        if (updateQuery == null) return false;

        // ========== Executing Query ==========

        final int rowsAffected = database.executeNonQuery(updateQuery.getSql(), updateQuery.getParameters());

        return rowsAffected == 1;
    }

    default boolean insert(final T toBeInserted) {

        // ========== Getting controllers ==========

        final IDatabaseController database = ControllerUtil.tryGetAndValidateDatabaseController();
        if (database == null) return false;

        // ========== Getting Query ==========

        if (toBeInserted == null) return false;
        final QueryData insertQuery = toBeInserted.getInsertQuery();
        if (insertQuery == null) return false;

        // ========== Executing Query ==========

        final int rowsAffected = database.executeNonQuery(insertQuery.getSql(), insertQuery.getParameters());

        return rowsAffected == 1;
    }

    default boolean select(final T toBeSelected) {

        // ========== Getting clazz ==========

        final Class<T> clazz = DatabaseUtil.tryGetClazz(this);
        if (clazz == null) return false;

        // ========== Selecting ==========

        return this.parametrizedSelect(toBeSelected);
    }

    default boolean select(final List<T> toBeSelected) {

        // ========== Getting clazz ==========

        final Class<T> clazz = DatabaseUtil.tryGetClazz(this);
        if (clazz == null) return false;

        // ========== Selecting ==========

        return this.parametrizedSelect(clazz, toBeSelected);
    }

    default <G extends IEntity> boolean parametrizedSelect(final G toBeSelected) {

        // ========== Getting controllers ==========

        final IDatabaseController database = ControllerUtil.tryGetAndValidateDatabaseController();
        if (database == null) return false;

        // ========== Getting row ==========

        if (toBeSelected == null) return false;
        final Map<String, Object> row = IDatabasePlugin.tryFetchSingleRow(database, toBeSelected);
        if (row == null) return false;

        // ========== Setting selected ==========

        if (IDatabasePlugin.trySetInstanceFromRow(this, toBeSelected, row) == false) return false;

        return true;
    }

    default <G extends IEntity> boolean parametrizedSelect(final Class<G> clazz, final List<G> toBeSelected) {

        // ========== Getting controller ==========

        final IDatabaseController database = ControllerUtil.tryGetAndValidateDatabaseController();
        if (database == null) return false;

        // ========== Getting instance ==========

        if (clazz == null) return false;
        final G instance = DatabaseUtil.tryGetInstance(clazz);
        if (instance == null) return false;

        // ========== Getting rows ==========

        final QueryData queryData = instance.getSelectQuery();
        final List<Map<String, Object>> rows = database.executeEntityQuery(queryData.getSql(), queryData.getParameters());
        if (rows == null) return false;

        // ========== Setting list ==========

        if (IDatabasePlugin.trySetListFromRows(this, clazz, toBeSelected, rows) == false) return false;


        return true;
    }

    // ========== Getting list from rows ==========

    private static <T extends IEntity> boolean trySetListFromRows(
        final IDatabasePlugin plugin,
        final Class<T> clazz,
        final List<T> list,
        final List<Map<String, Object>> rows
    ) {
        try {
            final Constructor<T> constructor = clazz.getConstructor();

            for (final Map<String, Object> row : rows) {
                final T instance = constructor.newInstance();

                if (IDatabasePlugin.trySetInstanceFromRow(plugin, instance, row))
                    list.add(instance);
            }
        } catch (Exception e) {
            final StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();

            System.err.println("Exception: [" + e.getClass().getName() + "] with message [" + e.getMessage() + "] when trying to select many from database controller while executing [" + stackTraces[1].getMethodName() + "] called from [" + stackTraces[2].getMethodName() + "].");

            return false;
        }

        return true;
    }

    // ============================================



    // ========== Getting instance from row ==========

    private static <T extends IEntity> boolean trySetInstanceFromRow(final IDatabasePlugin plugin, final T instance, Map<String, Object> row) {

        try {
            for (final Field field : instance.getClass().getDeclaredFields()) {
                final Column column = field.getAnnotation(Column.class);

                if (column == null) continue;

                final Object value = row.get(column.name());

                field.setAccessible(true);
                if (IEntity.class.isAssignableFrom(field.getType()))
                    IDatabasePlugin.tryAssignFromRowToComposition(plugin, instance, field, row);
                else if (value instanceof java.sql.Timestamp)
                    field.set(instance, ((java.sql.Timestamp) value).toLocalDateTime());
                else if (value instanceof java.sql.Date &&
                    field.getType().equals(java.time.LocalDateTime.class))
                    field.set(instance, ((java.sql.Date) value).toLocalDate().atStartOfDay());
                else if (value instanceof java.sql.Date &&
                    field.getType().equals(java.time.LocalDate.class))
                    field.set(instance, ((java.sql.Date) value).toLocalDate());
                else if (value instanceof java.sql.Time && field.getType().equals(java.time.LocalTime.class))
                    field.set(instance, ((java.sql.Time) value).toLocalTime());
                else if (value instanceof Timestamp)
                    field.set(instance, ((Timestamp)value).toLocalDateTime());
                else field.set(instance, value);
            }

        } catch (Exception e) {
            final StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();

            System.err.println("Exception: [" + e.getClass().getName() + "] with message [" + e.getMessage() + "] while executing [" + stackTraces[1].getMethodName() + "] called from [" + stackTraces[2].getMethodName() + "].");

            return false;
        }

        return true;
    }

    // ===============================================




    // ========== Handling references in a recursive manner ==========

    private static <T extends IEntity> boolean tryAssignFromRowToComposition(
        final IDatabasePlugin plugin,
        final T instance,
        final Field field,
        final Map<String, Object> row
    ) {
        try {
            final T composition;
            final Object fieldValue = field.getType().getConstructor().newInstance();

            if (fieldValue instanceof IEntity)
                composition = (T) fieldValue;
            else throw new IllegalArgumentException("Field type isn't [" + IEntity.class.getName() + "].");

            final Field pkField = composition.getPrimaryKeyField();
            final Column column = pkField.getAnnotation(Column.class);

            if (column == null) throw new IllegalArgumentException("No column found in field: [" + field.getName() + "].");

            if (pkField == null) throw new IllegalArgumentException("No primary key found in [" + composition.getClass().getName() + "].");

            pkField.setAccessible(true);
            pkField.set(composition, row.get(column.name()));

            // enables proxy, lazy loads
            if (field.getType().isInterface()) {
                final InvocationHandler handler = new LazyLoadingEvocationHandler(composition, plugin);
                final Object instanceProxy = Proxy.newProxyInstance(field.getType().getClassLoader(), new Class<?>[]{ field.getType() }, handler);
                field.setAccessible(true);
                field.set(instance, instanceProxy);
            // conventional not optimal way
            } else {
                // vvvv the recursion is here vvvvv
                if (plugin.parametrizedSelect(composition) == false)
                    throw new IllegalArgumentException("Couldn't fetch from controller composition [" + composition.getClass().getName() + "] with id [" + row.get(column.name()) + "].");
                field.setAccessible(true);
                field.set(instance, composition);
            }

        } catch (Exception e) {
            final StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();

            System.err.println("Exception: [" + e.getClass().getName() + "] with message [" + e.getMessage() + "] while executing [" + stackTraces[1].getMethodName() + "] called from [" + stackTraces[2].getMethodName() + "] called from [" + stackTraces[2].getMethodName() + "].");

            return false;
        }

        return true;
    }

    // ================================================================



    // ========== Fetching instance from row ==========

    private static <T extends IEntity> Map<String, Object> tryFetchSingleRow(final IDatabaseController database, final T toBeSelected) {
        final QueryData queryData = toBeSelected.getSelectQuery();
        final List<Map<String, Object>> rows = database.executeEntityQuery(queryData.getSql(), queryData.getParameters());
        if (rows == null) return null;

        final Map<String, Object> row;

        try {
            if (rows.size() != 1) throw new InvalidRowsSizeException();

            row = rows.get(0);
        } catch (Exception e) {
            final StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();

            System.err.println("Exception: [" + e.getClass().getName() + "] with message [" + e.getMessage() + "] when trying to select many from database controller while executing [" + stackTraces[1].getMethodName() + "] called from [" + stackTraces[2].getMethodName() + "].");

            return null;
        }

        return row;
    }

    // ===============================================
}
