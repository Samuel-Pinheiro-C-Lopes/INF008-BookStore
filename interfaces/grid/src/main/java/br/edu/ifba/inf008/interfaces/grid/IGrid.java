package br.edu.ifba.inf008.interfaces.grid;

import br.edu.ifba.inf008.interfaces.database.IEntity;
import br.edu.ifba.inf008.interfaces.grid.annotations.*;

import javafx.scene.layout.*;
import javafx.scene.control.*;

import java.lang.reflect.Field;

import javafx.beans.property.SimpleStringProperty;

public interface IGrid extends IEntity {
    abstract String getColumnDisplayText();

    default boolean bindTable(final TableView<IGrid> table) {
        try {
            for (final Field field : this.getClass().getDeclaredFields()) {
                final Viewable viewable = field.getAnnotation(Viewable.class);

                if (viewable == null) continue;

                field.setAccessible(true);

                final TableColumn<IGrid, String> col = new TableColumn<>(viewable.name());

                col.setCellValueFactory(cellData -> {
                    final IGrid row = cellData.getValue();
                    String text;

                    try {
                        final Object value = field.get(row);

                        if (value == null) text = "";
                        else if (value instanceof IGrid) text = ((IGrid) value).getColumnDisplayText();
                        else text = value.toString();

                    } catch (IllegalAccessException e) {
                        final StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();

                        System.err.println("Exception: [" + e.getClass().getName() + "] with message ["+ e.getMessage() + "] from table from IGrid: [" + this.getClass().getName() + "] while factoring cell. Origin: [" + stackTraces[1].getMethodName() + "] called from [" + stackTraces[2].getMethodName() + "] from ["+ stackTraces[3].getMethodName() + "].");

                        text = "ERROR";
                    }

                    return new SimpleStringProperty(text);
                });

                table.getColumns().add(col);
            }
        } catch (Exception e) {
            final StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();

            System.err.println("Exception: [" + e.getClass().getName() + "] with message ["+ e.getMessage() + "] from IGrid: [" + this.getClass().getName() + "] while binding table. Origin: [" + stackTraces[1].getMethodName() + "] called from [" + stackTraces[2].getMethodName() + "] from ["+ stackTraces[3].getMethodName() + "].");

            return false;
        }

        return true;
    }
}
