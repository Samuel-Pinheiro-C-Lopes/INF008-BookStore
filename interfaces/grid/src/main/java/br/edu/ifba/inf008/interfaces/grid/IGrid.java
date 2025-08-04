package br.edu.ifba.inf008.interfaces.grid;

import br.edu.ifba.inf008.interfaces.database.IEntity;
import br.edu.ifba.inf008.interfaces.grid.annotations.*;

import javafx.scene.layout.*;

import java.lang.reflect.Field;

public interface IGrid extends IEntity {
    abstract String getColumnDisplayText();

    default void setGridView(final TableView<IGrid> table) {
        for (final Field field : this.getClass().getDeclaredFields()) {
            final Viewable viewable = field.getAnnotation(Viewable.class);

            if (viewable == null) continue;

            field.setAccessible(true);

            final TableColumn<IGrid, String> col = new TableColumn<>(viewable.name());

            col.setCellValueFactory(cellData -> {
                final IGrid row = cellData.getValue();
                final String text;

                try {
                    final Object value = field.get(row);

                    if (value == null) text = "";
                    else if (value instanceof IGrid grid) text = grid.getColumnDisplayText();
                    else text = value.toString();

                } catch (IllegalAccessException e) {
                    text = "ERROR";
                    e.printStackTrace();
                }

                return new SimpleStringProperty(text);
            });

            table.getColumns().add(col);
        }
    }
}
