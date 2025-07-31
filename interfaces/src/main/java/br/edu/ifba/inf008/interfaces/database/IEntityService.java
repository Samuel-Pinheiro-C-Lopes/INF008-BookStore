package br.edu.ifba.inf008.interfaces.database;

import br.edu.ifba.inf008.interfaces.database.exceptions.*;

import java.util.List;
import java.util.Map;
import java.lang.StringBuilder;

import java.lang.reflect.Field;

import br.edu.ifba.inf008.interfaces.database.annotations.*;

/*
IDatabaseController API
    Object executeScalarQuery(String query);
    List<Map<String, Object>> executeEntityQuery(String query);
    int executeNonQuery(String query);
*/
/*
 * Mate, think about making this class here the IDatabasePlugin, making it implement
 * IPlugin and, whenever in the plugin someones creates a database plugin, that someone just
 * extends this man, implements the getClazz and init (I don't even know if init will be necessary)
 * maybe it is because of the singleton pattern? dunno, you need to evaluate this, I'm going to sleep
 * and hit chest tomorrow ^^
*/
public interface IEntityService<T extends IEntity> {
    abstract Class<T> getClazz();

    default boolean delete(final IDatabaseController database, final T toBeDeleted) {
        final String deleteQuery = toBeDeleted.getDeleteQuery();

        final int rowsAffected = database.executeNonQuery(deleteQuery);

        return rowsAffected == 1;
    }

    default boolean update(final IDatabaseController database, final T toBeUpdated) {
        final String updateQuery = toBeUpdated.getUpdateQuery();

        final int rowsAffected = database.executeNonQuery(updateQuery);

        return rowsAffected == 1;
    }

    default boolean insert(final IDatabaseController database, final T toBeInserted) {
        final String insertQuery = toBeInserted.getInsertQuery();

        final int rowsAffected = database.executeNonQuery(insertQuery);

        return rowsAffected == 1;
    }

    default boolean selectSingle(final IDatabaseController database, final T toBeSelected) {
        final Class<T> clazz = this.getClazz();
        final Map<String, Object> row;
        Column column = null;

        try {
            row = database.executeEntityQuery(toBeSelected.getSelectQuery()).get(0);

            for (Field field : clazz.getDeclaredFields()) {
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

    default List<T> selectAll(final IDatabaseController database) {
        final Class<T> clazz = this.getClazz();
        final List<T> instances = new ArrayList<>();
        final T instance = clazz.getConstructor().newInstance();
        final List<Map<String, Object>> rows;
        Column column = null;

        try {
            rows = database.executeEntityQuery(instance.getSelectQuery());

            for (Map<String, Object> row : rows) {
                instance = clazz.getConstructor().newInstance();

                for (Field field : clazz.getDeclaredFields()) {
                    column = field.getAnnotation(Column.class);

                    if (column == null)
                        continue;

                    field.setAccessible(true); // search further about this bit of code here
                    field.set(instance, row.get(column.name()));
                }

                instances.add(instance);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return instances;
    }
}
