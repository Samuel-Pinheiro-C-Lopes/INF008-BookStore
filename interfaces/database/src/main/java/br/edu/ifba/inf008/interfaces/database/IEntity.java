package br.edu.ifba.inf008.interfaces.database;

import br.edu.ifba.inf008.interfaces.database.annotations.*;
import br.edu.ifba.inf008.interfaces.database.exceptions.*;
import br.edu.ifba.inf008.interfaces.database.util.*;

import java.lang.Thread;
import java.lang.StackTraceElement;

import java.lang.reflect.Field;
import java.lang.StringBuilder;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public interface IEntity {

    default Field getPrimaryKeyField() {
        Field pkField = null;

        try {
            Column column = null;

            for (final Field field : this.getClass().getDeclaredFields()) {
                column = field.getAnnotation(Column.class);

                if (column != null && column.primaryKey()) {
                    pkField = field;
                    pkField.setAccessible(true);
                    break;
                }
            }
        } catch (Exception e) {
            final StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();

            System.err.println("Exception: [" + e.getClass().getName() + "] with message [" + e.getMessage() + "] when trying to get primary key from instance while executing [" + stackTraces[1].getMethodName() + "] called from [" + stackTraces[2].getMethodName() + "].");

            return null;
        }

        return pkField;
    }

    default Object getPrimaryKey() {
        Object primaryKey = null;
        Column column = null;

        try {
            for (final Field field : this.getClass().getDeclaredFields()) {
                column = field.getAnnotation(Column.class);

                if (column != null && column.primaryKey()) {
                    field.setAccessible(true);
                    primaryKey = field.get(this);
                    break;
                }
            }
        } catch (Exception e) {
            final StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();

            System.err.println("Exception: [" + e.getClass().getName() + "] with message [" + e.getMessage() + "] when trying to get primary key from instance while executing [" + stackTraces[1].getMethodName() + "] called from [" + stackTraces[2].getMethodName() + "].");
        }

        return primaryKey;
    }

    default QueryData getSelectQuery() {
        final StringBuilder selectQueryBuilder = new StringBuilder();
        final List<Object> parameters = new ArrayList<>();
        final String tableName;
        Column column = null;
        Column pkColumn = null;
        Field pkField = null;

        try {
            tableName = IEntity.getTableName(this);

            selectQueryBuilder.append("SELECT ");

            for (Field field : this.getClass().getDeclaredFields()) {
                column = field.getAnnotation(Column.class);

                if (column == null)
                    continue;

                if (column.primaryKey()) {
                    pkColumn = column;
                    pkField = field;
                }

                selectQueryBuilder.append(column.name())
                                  .append(", ");
            }

            IEntity.removeUnnecessaryEnding(selectQueryBuilder);

            selectQueryBuilder.append(" FROM ")
                              .append(tableName);

            if (pkField != null && pkColumn != null) {
                pkField.setAccessible(true);
                Object val = pkField.get(this);

                if (val != null) {
                    selectQueryBuilder.append(" WHERE ")
                                    .append(pkColumn.name())
                                    .append(" = ?;");

                    parameters.add(pkField.get(this));
                }
            }

            selectQueryBuilder.append(";");

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        final QueryData queryData = new QueryData(selectQueryBuilder.toString(), parameters);
        return queryData;
    }

    default QueryData getInsertQuery() {
        final StringBuilder insertQueryBuilder = new StringBuilder();
        final StringBuilder insertColumnsBuilder = new StringBuilder();
        final StringBuilder insertValuesBuilder = new StringBuilder();
        final List<Object> parameters = new ArrayList<>();
        final String tableName;
        Column column = null;
        Column pkColumn = null;
        Field pkField = null;

        final Map<String, Field> columnsField = new LinkedHashMap<String, Field>();

        try {
            tableName = IEntity.getTableName(this);

            insertQueryBuilder.append("INSERT INTO ")
                              .append(tableName)
                              .append(" ");

            for (Field field : this.getClass().getDeclaredFields()) {
                column = field.getAnnotation(Column.class);

                if (column == null)
                    continue;

                if (column.primaryKey()) {
                    pkColumn = column;
                    pkField = field;
                    continue;
                }

                columnsField.put(column.name(), field);
            }

            for (Map.Entry<String, Field> entry : columnsField.entrySet()) {
                Field field = entry.getValue();

                insertColumnsBuilder.append(entry.getKey())
                                    .append(", ");

                insertValuesBuilder.append("?, ");

                field.setAccessible(true);
                final Object value = field.get(this);
                if (value instanceof IEntity)
                    parameters.add(((IEntity)value).getPrimaryKey());
                else parameters.add(value);
            }

            IEntity.removeUnnecessaryEnding(insertColumnsBuilder);
            IEntity.removeUnnecessaryEnding(insertValuesBuilder);

            insertQueryBuilder.append("(")
                              .append(insertColumnsBuilder.toString())
                              .append(") VALUES (")
                              .append(insertValuesBuilder.toString())
                              .append(");");

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        final QueryData queryData = new QueryData(insertQueryBuilder.toString(), parameters);
        return queryData;
    }

    default QueryData getUpdateQuery() {
        final StringBuilder updateQueryBuilder = new StringBuilder();
        final List<Object> parameters = new ArrayList<>();
        final String tableName;
        Column column = null;
        Column pkColumn = null;
        Field pkField = null;

        try {
            tableName = IEntity.getTableName(this);

            updateQueryBuilder.append("UPDATE ")
                              .append(tableName)
                              .append(" SET ");

            for (Field field : this.getClass().getDeclaredFields()) {
                column = field.getAnnotation(Column.class);

                if (column == null)
                    continue;

                if (column.primaryKey()) {
                    pkColumn = column;
                    pkField = field;
                    continue;
                }

                updateQueryBuilder.append(column.name())
                                  .append(" = ?, ");

                field.setAccessible(true);
                final Object value = field.get(this);
                if (value instanceof IEntity)
                    parameters.add(((IEntity)value).getPrimaryKey());
                else parameters.add(value);
            }

            if (pkField == null)
                throw new NoPrimaryKeyException();

            IEntity.removeUnnecessaryEnding(updateQueryBuilder);

            updateQueryBuilder.append(" WHERE ")
                              .append(pkColumn.name())
                              .append(" = ?;");

            pkField.setAccessible(true);
            parameters.add(pkField.get(this));

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        final QueryData queryData = new QueryData(updateQueryBuilder.toString(), parameters);
        return queryData;
    }

    default QueryData getDeleteQuery() {
        final StringBuilder deleteQueryBuilder = new StringBuilder();
        final List<Object> parameters = new ArrayList<>();
        final String tableName;
        Column column = null;
        Column pkColumn = null;
        Field pkField = null;

        try {
            tableName = IEntity.getTableName(this);

            for (Field field : this.getClass().getDeclaredFields()) {
                column = field.getAnnotation(Column.class);

                if (column == null)
                    continue;

                if (column.primaryKey() == false)
                    continue;

                pkColumn = column;
                pkField = field;
                break;
            }

            if (pkField == null)
                throw new NoPrimaryKeyException();

            deleteQueryBuilder
                .append("DELETE FROM ")
                .append(tableName)
                .append(" WHERE ")
                .append(pkColumn.name())
                .append(" = ?;");

            pkField.setAccessible(true);
            parameters.add(pkField.get(this));

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        final QueryData queryData = new QueryData(deleteQueryBuilder.toString(), parameters);
        return queryData;
    }

    // helper method to fetch table name
    private static String getTableName(final IEntity entity) throws TableNotAnnotatedException {
        final Table table = entity.getClass().getAnnotation(Table.class);

        if (table == null) throw new TableNotAnnotatedException();

        return table.name();
    }

    // helper method to remove unnecessary endings
    private static void removeUnnecessaryEnding(final StringBuilder builder) {
        final int length = builder.length();
        // removes unnecessary ", "
        if (length > 2)
            builder.setLength(length - 2);
    }
}


/*
THIS IS STILL PRONE TO SQL INJECTION
    // helper method to map Java types to MariaDB types
    private static String mapJavaTypeToSqlType(final Class<?> type) throws IllegalArgumentException {
        if (type == String.class || type == Character.class) return "CHAR";
        if (type == int.class || type == Integer.class) return "SIGNED";
        if (type == long.class || type == Long.class) return "SIGNED";
        if (type == double.class || type == Double.class) return "DOUBLE";
        if (type == float.class || type == Float.class) return "FLOAT";
        // MariaDB treats booleans as 0/1
        if (type == boolean.class || type == Boolean.class) return "SIGNED";
        if (java.util.Date.class.isAssignableFrom(type)) return "DATE";
        if (java.time.temporal.Temporal.class.isAssignableFrom(type)) return "DATE";
        if (type.isEnum()) return "CHAR"; // assuming enum name storage

        // gets primary key type
        if (IEntity.class.isAssignableFrom(type)) {
            final IEntity instance;

            try {
                instance = type.getConstructor().newInstance();
            } catch (Exception e) {
                StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();

                throw new IllegalArgumentException("Attempt to fetch IEntity primary key type for: [" + type.getName() + "] failed with [" + e.getClass().getName() + "] exception with message [" + e.getMessage() + "]. Error occurred in [" + stackTraces[1].getMethodName() + "] from [" + stackTraces[2].getMethodName() + "] call from [" + stackTraces[3].getMethodName() + "] call.");
            }

            return mapJavaTypeToSqlType(instance.getPrimaryKey().getClass());
        }

        throw new IllegalArgumentException("Unsupported type: " + type.getName());
    }

    // helper method to add safety against sql injection when dealing
    // with input from user
    private static String toSqlValue(final Field field, Object value) throws IllegalArgumentException {
        if (value instanceof IEntity) value = ((IEntity) value).getPrimaryKey();

        if (value == null) return "NULL";

        final String sqlType = mapJavaTypeToSqlType(field.getType());

        final String stringValue = value.toString().replace("'", "''");

        return "CAST('" + stringValue + "' AS " + sqlType + ")";
    }

    */
