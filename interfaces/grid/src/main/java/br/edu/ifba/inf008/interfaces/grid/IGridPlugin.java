package br.edu.ifba.inf008.interfaces.grid;

import br.edu.ifba.inf008.interfaces.database.IDatabasePlugin;

import javafx.scene.layout.*;

public interface IGridPlugin<T extends IGrid> extends IDatabasePlugin<T> implements IPlugin {

    default boolean setGridView(final TableView<T> table) {
        final List<T> toBeSelected = new ArrayList<T>();
        final IDatabaseController database = Core.getInstance().getDatabaseController();

        if (database == null || select(database, toBeSelected) == false) return false;

        try {
            final T instance = getEntityClazz().getConstructor().newInstance();

            instance.setGridView(table);

            table.getItems().addAll(toBeSelected);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}


/* Apparently this doesn't work because it needs a different
 * button for each row (think about passing a )*/
/*
for (final Button button : actions) {
    final TableColumn<T, void> btnCol = new TableColumn<>(button.getText());

    btnCol.setCellFactory(col -> new TableCell<>() {
        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(empty ? null : button);
        }
    });

    table.getColumns.add(btnCol);
}
*/
