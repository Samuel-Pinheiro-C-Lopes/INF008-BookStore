package br.edu.ifba.inf008.interfaces.grid;

import br.edu.ifba.inf008.interfaces.controller.IPlugin;
import br.edu.ifba.inf008.interfaces.controller.ICore;
import br.edu.ifba.inf008.interfaces.controller.IDatabaseController;
import br.edu.ifba.inf008.interfaces.controller.IUIController;
import br.edu.ifba.inf008.interfaces.controller.util.ControllerUtil;

import br.edu.ifba.inf008.interfaces.database.IDatabasePlugin;
import br.edu.ifba.inf008.interfaces.database.util.DatabaseUtil;

import br.edu.ifba.inf008.interfaces.grid.annotations.*;

import java.util.List;
import java.util.Vector;

import javafx.scene.layout.*;
import javafx.scene.control.*;

import javafx.collections.ObservableList;
import javafx.collections.FXCollections;

import javafx.beans.property.SimpleStringProperty;

/*
    VBox createForm(String formTitle);
    ComboBox addComboToForm(VBox form);
    GridPane addGridToForm(VBox form);
    Button addButtonToForm(VBox form, String buttonText);

    VBox createDisplay(String displayTitle);
    TableView<T> addTableToDisplay<T>(VBox display);
*/
public interface IGridPlugin<T extends IGrid> extends IDatabasePlugin<T> {
    boolean initGrid();

    ObservableList<T> getTableItems();

    default boolean addTable(final String tableName) {

        // ========== Getting controllers ==========

        final IDatabaseController database = ControllerUtil.tryGetAndValidateDatabaseController();
        final IUIController ui = ControllerUtil.tryGetAndValidateIUIController();
        if (database == null || ui == null) return false;

        // ========== Getting instance ==========

        final Class<T> clazz = DatabaseUtil.tryGetClazz(this);
        if (clazz == null) return false;
        final T instance = DatabaseUtil.tryGetInstance(clazz);
        if (instance == null) return false;

        // ========== Creating Display and inner table ==========

        final VBox display = ui.createDisplay(tableName == null ? clazz.getSimpleName() : tableName);
        if (display == null) return false;
        final TableView<T> table = ui.addTableToDisplay(display);
        if (table == null) return false;
        final Button button = ui.addButtonToDisplay(display, "Reload");
        if (button == null) return false;

        // ========== Setting table ==========

        if (instance.bindTable((TableView<IGrid>) table) == false) return false;

        // ========== Binding table's data ==========

        if (tryBindTableData(table) == false) return false;

        // ========== Binding table's reload button ==========

        button.setOnAction(e -> {
            if (tryBindTableData(table) == false)
                System.err.println("Error while trying to rebind table's data for [" + this.getClass().getName() + "].");
        });

        return true;
    }

    // ========== Bind table's data ==========

    default boolean tryBindTableData(final TableView<T> table) {
        final List<T> selectedBd = new Vector<>();

        if (this.select(selectedBd) == false) return false;

        final ObservableList<T> tableItems = this.getTableItems();

        tableItems.setAll(selectedBd);

        table.setItems(tableItems);

        return true;
    }

    // =======================================
}


/* Apparently this doesn't work because it needs a different
 * button for each row (think about passing a )*/
/*

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
