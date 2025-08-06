package br.edu.ifba.inf008.interfaces.form;

import br.edu.ifba.inf008.interfaces.controller.IPlugin;
import br.edu.ifba.inf008.interfaces.controller.ICore;
import br.edu.ifba.inf008.interfaces.controller.IDatabaseController;
import br.edu.ifba.inf008.interfaces.controller.IUIController;
import br.edu.ifba.inf008.interfaces.controller.util.ControllerUtil;

import br.edu.ifba.inf008.interfaces.database.IEntity;
import br.edu.ifba.inf008.interfaces.database.IDatabasePlugin;
import br.edu.ifba.inf008.interfaces.database.util.DatabaseUtil;
import br.edu.ifba.inf008.interfaces.database.annotations.Column;

import br.edu.ifba.inf008.interfaces.form.annotations.*;
import br.edu.ifba.inf008.interfaces.form.enums.*;

import java.lang.StackTraceElement;
import java.lang.Thread;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

import java.util.List;
import java.util.Vector;
import java.util.ArrayList;

import javafx.scene.layout.*;
import javafx.scene.control.*;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import javafx.collections.ObservableList;
import javafx.collections.FXCollections;

/*
    VBox createForm(String formTitle);
    ComboBox addComboToForm(VBox form);
    GridPane addGridToForm(VBox form);
    Button addButtonToForm(VBox form, String buttonText);

    VBox createDisplay(String displayTitle);
    TableView<T> addTableToDisplay<T>(VBox display);
*/

public interface IFormPlugin<T extends IForm> extends IDatabasePlugin<T> {
    abstract boolean initForm();

    default boolean addForm() {

        // ========== Getting controllers ==========

        final IUIController ui = ControllerUtil.tryGetAndValidateIUIController();
        if (ui == null) return false;

        // ========== Getting instance ==========

        final Class<T> clazz = DatabaseUtil.tryGetClazz(this);
        if (clazz == null) return false;
        final T instance = DatabaseUtil.tryGetInstance(clazz);
        if (instance == null) return false;

        // ========== Creating Form and inner elements ==========

        final VBox form = ui.createForm(clazz.getSimpleName());
        if (form == null) return false;
        final ComboBox<T> selector = ui.addComboToForm(form);
        final GridPane grid = ui.addGridToForm(form);
        final Button saveButton = ui.addButtonToForm(form, "save " + clazz.getSimpleName());
        final Button deleteButton = ui.addButtonToForm(form, "remove " + clazz.getSimpleName());
        if (selector == null || grid == null || saveButton == null) return false;

        // ========== Filling options grid ==========
        // #TODO logic to select all for each field that is column annotated with a class
        // that is a table and pass those values to the fields that are options with corresponding
        // positioning
        if (IFormPlugin.tryFetchAndSetOptions(this, instance) == false) return false;

        // ========== Setting grid ==========

        if (instance.bindGrid(grid) == false) return false;

        // ========== Setting selector ==========

        if (IFormPlugin.trySetSelector(this, instance, selector, grid, saveButton, deleteButton) == false) return false;

        return true;

    }

    private static <T extends IForm> boolean tryFetchAndSetOptions(final IDatabasePlugin plugin, final T instance) {
        try {
            for (final Field field : instance.getClass().getDeclaredFields()) {
                final Input input = field.getAnnotation(Input.class);

                if (input == null || input.type() != InputType.OPTIONS) continue;

                if (List.class.isAssignableFrom(field.getType()) == false)
                    throw new IllegalArgumentException("Field [" + field.getName() + "] of [" + instance.getClass().getName() + "] annotated as OPTIONS isn't assignable to [" + List.class.getName() + "].");

                final ParameterizedType fieldGenericType = (ParameterizedType) field.getGenericType();
                final Class<?> fieldGenericClazz = (Class<?>) fieldGenericType.getActualTypeArguments()[0];

                if (IEntity.class.isAssignableFrom(fieldGenericClazz) == false)
                    throw new IllegalArgumentException("List of class [" + fieldGenericClazz.getName() + "] from field [" + field.getName() + "] of [" + instance.getClass().getName() + "] isn't assignable to [" + IEntity.class.getName() + "].");

                final List<IEntity> selectedBd = new ArrayList<>();

                if (plugin.parametrizedSelect((Class<IEntity>) fieldGenericClazz, selectedBd) == false) throw new Exception();

                field.setAccessible(true);
                field.set(instance, selectedBd);
            }
        } catch (ClassCastException | ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
            final StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();
            final Object primaryKey = instance.getPrimaryKey();

            System.err.println("Exception: [" + e.getClass().getName() + "] with message [" + e.getMessage() + "] when trying to get generic type setting options for [" + instance.getClass().getName() + "] with id [" + (primaryKey != null ? primaryKey.toString() : "NULL") + "] while executing [" + stackTraces[1].getMethodName() + "] called from [" + stackTraces[2].getMethodName() + "].");

            return false;
        } catch (Exception e ){
            final StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();
            final Object primaryKey = instance.getPrimaryKey();

            System.err.println("Exception: [" + e.getClass().getName() + "] with message [" + e.getMessage() + "] when trying set options for [" + instance.getClass().getName() + "] with id [" + (primaryKey != null ? primaryKey.toString() : "NULL") + "] while executing [" + stackTraces[1].getMethodName() + "] called from [" + stackTraces[2].getMethodName() + "].");

            return false;
        }

        return true;
    }

    // ========== Setting selector ==========

    private static <T extends IForm> boolean trySetSelector(
        final IFormPlugin<T> plugin,
        final T instance,
        final ComboBox<T> selector,
        final GridPane grid,
        final Button button,
        final Button deleteButton
    ) {
        try {
            final List<T> selectedDb = new Vector();

            if (plugin.select(selectedDb) == false) return false;

            final ObservableList<T> observableSelected =
                FXCollections.observableArrayList(selectedDb);
            observableSelected.add(instance);

            selector.getItems().setAll(observableSelected);
            instance.resetFields();
            selector.setValue(instance);
            instance.bindGridValues(grid);
            instance.bindButton(grid, button);

            final EventHandler<ActionEvent> bindHandler = button.getOnAction();

            button.setOnAction(newE -> {
                final T selectedCb = selector.getValue();

                if (bindHandler == null) {
                    final StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();

                    System.err.println("Button from IForm: [" + plugin.getEntityClazz().getName() + "] didn't have bind handler when trying to assign the save or update handling. Origin: [" + stackTraces[1].getMethodName() + "] called from [" + stackTraces[2].getMethodName() + "].");

                    return;
                }

                bindHandler.handle(newE);

                final boolean isToBeInserted = selectedCb.getPrimaryKey() == null;
                final boolean databaseReturn;

                if (isToBeInserted) databaseReturn = plugin.insert(selectedCb);
                else databaseReturn = plugin.update(selectedCb);

                if (databaseReturn == false) {
                    final StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();

                    System.err.println("Button's on action lambda from IForm: [" + plugin.getEntityClazz().getName() + "] tried to call " + (isToBeInserted ? "insert" : "update") + " from the plugin but failed. Origin: [" + stackTraces[1].getMethodName() + "] called from [" + stackTraces[2].getMethodName() + "].");

                    return;
                }

                IFormPlugin.trySetSelector(plugin, instance, selector, grid, button, deleteButton);
            });

            deleteButton.setOnAction(newE -> {
                final T selectedCb = selector.getValue();
                final boolean canBeDeleted = selectedCb.getPrimaryKey() != null;
                final StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();

                if (canBeDeleted) {
                    if (plugin.delete(selectedCb) == false)
                        System.err.println("Button's delete action lambda from IForm: [" + plugin.getEntityClazz().getName() + "] tried to call delete from the plugin but failed. Origin: [" + stackTraces[1].getMethodName() + "] called from [" + stackTraces[2].getMethodName() + "].");

                } else {
                    System.err.println("Button's on action lambda from IForm: [" + plugin.getEntityClazz().getName() + "] tried to call delete from the plugin but failed due to the abscence of a not null primary key. Origin: [" + stackTraces[1].getMethodName() + "] called from [" + stackTraces[2].getMethodName() + "].");

                    return;
                }

                IFormPlugin.trySetSelector(plugin, instance, selector, grid, button, deleteButton);
            });

            selector.setCellFactory(list -> new ListCell<>() {
                @Override
                protected void updateItem(T entity, boolean empty) {
                    super.updateItem(entity, empty);
                    setText(entity == null || entity.getPrimaryKey() == null ? "Create New" : entity.getSelectDisplayText());
                }
            });

            selector.setButtonCell(selector.getCellFactory().call(null));

            selector.setOnAction(e -> {
                final T selectedCb = selector.getValue();

                if (selectedCb == null) {
                    final StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();

                    System.err.println("ComboBox from IForm: [" + plugin.getEntityClazz().getName() + "] selectedCb null value. Origin: [" + stackTraces[1].getMethodName() + "] called from [" + stackTraces[2].getMethodName() + "].");

                    return;
                }

                selectedCb.bindGridValues(grid);
                selectedCb.bindButton(grid, button);
            });

        } catch (Exception e) {
            final StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();

            System.err.println("Exception: [" + e.getClass().getName() + "] with message [" + e.getMessage() + "] when trying set selector while executing [" + stackTraces[1].getMethodName() + "] called from [" + stackTraces[2].getMethodName() + "].");

            return false;
        }

        return true;
    }

    // ======================================
}


/*
    default boolean renderSaveForm(final T formInstance) {
        final IDatabaseController database = Core.getInstance().getDatabaseController();
        final IUIController ui = Core.getInstance().getIUIController();

        if (database == null || ui == null) return false;

        final Grid formGrid = ui.createFormGrid();
        final Button saveButton = ui.addButtonToFormGrid(formGrid);

        try {
            formInstance.bindFormGrid(formGrid, saveButton);

            final EventHandler<ActionEvent> bindingHandler = button.getOnAction();

            button.setOnAction(e -> {
                if (bindingHandler != null)
                    bindingHandler.handle(e);
                insert(database, formInstance);
            });
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
*/
