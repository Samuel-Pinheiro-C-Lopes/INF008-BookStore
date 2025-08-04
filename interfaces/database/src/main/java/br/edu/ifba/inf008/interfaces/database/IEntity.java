package br.edu.ifba.inf008.interfaces.database;

import br.edu.ifba.inf008.interfaces.database.annotations.*;
import br.edu.ifba.inf008.interfaces.database.exceptions.*;

import java.lang.reflect.Field;
import java.lang.StringBuilder;

import java.util.LinkedHashMap;
import java.util.Map;

public interface IEntity {

    default String getSelectQuery() {
        final StringBuilder selectQueryBuilder = new StringBuilder();
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
                    continue;
                }

                selectQueryBuilder.append(column.name())
                                  .append(", ");
            }

            IEntity.removeUnnecessaryEnding(selectQueryBuilder);

            selectQueryBuilder.append(" FROM ")
                              .append(tableName);

            if (pkField != null && pkColumn != null) {
                Object val = pkField.get(this);

                if (val != null)
                    selectQueryBuilder.append(" WHERE ")
                                    .append(pkColumn.name())
                                    .append(" = ")
                                    .append(IEntity.toSqlValue(pkField, val.toString()));
            }

            selectQueryBuilder.append(";");

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return selectQueryBuilder.toString();
    }

    default String getInsertQuery() {
        final StringBuilder insertQueryBuilder = new StringBuilder();
        final StringBuilder insertColumnsBuilder = new StringBuilder();
        final StringBuilder insertValuesBuilder = new StringBuilder();
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
                insertValuesBuilder.append(IEntity.toSqlValue(field, field.get(this)))
                                   .append(", ");
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

        return insertQueryBuilder.toString();
    }

    default String getUpdateQuery() {
        final StringBuilder updateQueryBuilder = new StringBuilder();
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
                                  .append(" = ")
                                  .append(IEntity.toSqlValue(field, field.get(this).toString()))
                                  .append(", ");

            }

            if (pkField == null)
                throw new NoPrimaryKeyException();

            IEntity.removeUnnecessaryEnding(updateQueryBuilder);

            updateQueryBuilder.append(" WHERE ")
                              .append(pkColumn.name())
                              .append(" = ")
                              .append(IEntity.toSqlValue(pkField, pkField.get(this).toString()))
                              .append(";");

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return updateQueryBuilder.toString();
    }

    default String getDeleteQuery() {
        final StringBuilder deleteQueryBuilder = new StringBuilder();
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
                .append(" = ")
                .append(IEntity.toSqlValue(pkField, pkField.get(this).toString()))
                .append(";");

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return deleteQueryBuilder.toString();
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
        throw new IllegalArgumentException("Unsupported type: " + type.getName());
    }

    // helper method to add safety against sql injection when dealing
    // with input from user
    private static String toSqlValue(final Field field, final Object value) throws IllegalArgumentException {
        if (value == null)
            return "NULL";

        final String sqlType = mapJavaTypeToSqlType(field.getType());

        final String stringValue = value.toString().replace("'", "''");

        return "CAST('" + stringValue + "' AS " + sqlType + ")";
    }
}
