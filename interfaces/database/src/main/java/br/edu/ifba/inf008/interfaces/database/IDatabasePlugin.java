package br.edu.ifba.inf008.interfaces.database;

import br.edu.ifba.inf008.interfaces.database.exceptions.*;
import br.edu.ifba.inf008.interfaces.database.annotations.*;
import br.edu.ifba.inf008.interfaces.controller.IDatabaseController;
import br.edu.ifba.inf008.interfaces.controller.IPlugin;

import java.lang.StringBuilder;
import java.lang.reflect.Field;
import java.lang.reflect.Constructor;

import java.util.List;
import java.util.Map;

public interface IDatabasePlugin<T extends IEntity> implements IPlugin {
    abstract Class<T> getEntityClazz();

    default boolean delete(final IDatabaseController database, final T toBeDeleted) {
        final String deleteQuery = toBeDeleted.getDeleteQuery();
        final int rowsAffected;

        if (deleteQuery == null)
            return false;

        rowsAffected = database.executeNonQuery(deleteQuery);

        return rowsAffected == 1;
    }

    default boolean update(final IDatabaseController database, final T toBeUpdated) {
        final String updateQuery = toBeUpdated.getUpdateQuery();
        final int rowsAffected;

        if (updateQuery == null)
            return false;

        rowsAffected = database.executeNonQuery(updateQuery);

        return rowsAffected == 1;
    }

    default boolean insert(final IDatabaseController database, final T toBeInserted) {
        final String insertQuery = toBeInserted.getInsertQuery();
        final int rowsAffected;

        if (insertQuery == null)
            return false;

        rowsAffected = database.executeNonQuery(insertQuery);

        return false;
    }

    default boolean select(final IDatabaseController database, final T toBeSelected) {
        final Map<String, Object> row;
        Column column = null;

        try {
            row = database.executeEntityQuery(toBeSelected.getSelectQuery()).get(0);

            for (Field field : toBeSelected.getClass().getDeclaredFields()) {
                column = field.getAnnotation(Column.class);

                if (column == null)
                    continue;

                field.set(toBeSelected, row.get(column.name()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    default boolean select(final IDatabaseController database, final List<T> toBeSelected) {
        final Class<T> clazz = this.getEntityClazz();
        final List<Map<String, Object>> rows;
        final Constructor<T> constructor;
        Column column = null;
        T instance = null;

        try {
            constructor = clazz.getConstructor();
            instance = constructor.newInstance();
            rows = database.executeEntityQuery(instance.getSelectQuery());

            for (Map<String, Object> row : rows) {
                instance = constructor.newInstance();

                for (Field field : clazz.getDeclaredFields()) {
                    column = field.getAnnotation(Column.class);

                    if (column == null)
                        continue;

                    field.setAccessible(true); // search further about this bit of code here
                    field.set(instance, row.get(column.name()));
                }

                toBeSelected.add(instance);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
