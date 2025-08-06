package br.edu.ifba.inf008.interfaces.form;

import br.edu.ifba.inf008.interfaces.database.IEntity;

import br.edu.ifba.inf008.interfaces.form.annotations.*;
import br.edu.ifba.inf008.interfaces.form.exceptions.*;
import br.edu.ifba.inf008.interfaces.form.enums.*;

import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import java.lang.Thread;
import java.lang.StackTraceElement;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

import java.util.List;
import java.util.Map;
import java.util.AbstractMap;
import java.util.HashMap;


public interface IForm extends IEntity {

    abstract String getSelectDisplayText();

    default boolean bindGrid(final GridPane grid) {
        final Map<Integer, ComboBox<IForm>> comboMapper = new HashMap<>();
        final Map<Integer, List<IForm>> optionsMapper = new HashMap<>();

        try {
            for (final Field field : this.getClass().getDeclaredFields()) {
                final Input input = field.getAnnotation(Input.class);

                if (input == null)
                    continue;

                field.setAccessible(true);

                if (input.type() != InputType.OPTIONS)
                    grid.add(new Label(input.label() + ": "), 0, input.position());

                switch (input.type()) {
                    case SIMPLE:
                        grid.add(new TextField(), 1, input.position());
                        break;

                    case OPTIONS:
                        final Object value = field.get(this);

                        if (List.class.isAssignableFrom(value.getClass()) == false) throw new NotIterableOptionException();

                        final ParameterizedType genericType = (ParameterizedType) field.getGenericType();
                        final Class<?> genericClazz = (Class<?>) genericType.getActualTypeArguments()[0];

                        if (IEntity.class.isAssignableFrom(genericClazz))
                            optionsMapper.put(input.position(), (List<IForm>) value);
                        else throw new Exception("Generic type wasn't [" + IForm.class.getName() + "] while trying to add to optionsMapper.");

                        break;
                    case SELECT:
                        comboMapper.put(input.position(), new ComboBox<IForm>());
                        break;
                    case DATE:
                        grid.add(new DatePicker(), 1, input.position());
                        break;
                }
            }

            for (final Map.Entry<Integer, ComboBox<IForm>> positionCombo : comboMapper.entrySet()) {
                final List<IForm> currentOptions = optionsMapper.get(positionCombo.getKey());
                final ComboBox<IForm> combo = positionCombo.getValue();

                if (currentOptions == null)
                    throw new NoOptionSetForSelectionException();

                combo.setCellFactory(listView -> new ListCell<>() {
                    @Override
                    protected void updateItem(IForm item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? null : item.getSelectDisplayText());
                    }
                });

                combo.setButtonCell(combo.getCellFactory().call(null));
                combo.getItems().addAll(currentOptions);

                grid.add(combo, 1, positionCombo.getKey());
            }
        } catch (Exception e) {
           final StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();

            System.err.println("Exception: [ "+ e.getClass().getName() + "] with message [" + e.getMessage() + "]  while executing [" + stackTraces[1].getMethodName() + "] called from [" + stackTraces[2].getMethodName() + "].");

            return false;
        }

        return true;
    }

    default boolean bindGridValues(final GridPane grid) {
        try {
            final Map<Integer, TextField> textFields = new HashMap<>();
            final Map<Integer, ComboBox<IForm>> comboBoxes = new HashMap<>();
            final Map<Integer, DatePicker> datePickers = new HashMap<>();

            for (final Node node : grid.getChildren()) {
                final int column = GridPane.getColumnIndex(node);
                final int row = GridPane.getRowIndex(node);

                if (column != 1) continue;

                if (node instanceof TextField) textFields.put(row, (TextField) node);
                else if (node instanceof ComboBox) comboBoxes.put(row, (ComboBox<IForm>) node);
                else if (node instanceof DatePicker) datePickers.put(row, (DatePicker) node);
                else throw new NotSupportedNodeFieldTypeException();
            }

            for (final Field field : this.getClass().getDeclaredFields()) {
                final Input input = field.getAnnotation(Input.class);

                if (input == null)
                    continue;

                field.setAccessible(true);
                final Object value = field.get(this);

                switch (input.type()) {
                    case SIMPLE:
                        final TextField textField = textFields.get(input.position());
                        final String textValue = value != null ? value.toString() : "";

                        textField.setText(textValue);
                        break;

                    case SELECT:
                        final ComboBox<IForm> combo = comboBoxes.get(input.position());
                        final int targetIndex = combo.getItems().indexOf(value);

                        if (targetIndex != -1) combo.getSelectionModel().select(targetIndex);
                        break;
                    case DATE:
                        final DatePicker date = datePickers.get(input.position());

                        if (value instanceof LocalDateTime) {
                            final LocalDate local = ((LocalDateTime)value).toLocalDate();
                            date.setValue(local);
                        }

                        break;

                }

            }
        } catch (Exception e) {
            final StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();

            e.printStackTrace();

            System.err.println("Exception: [ "+ e.getClass().getName() + "] with message [" + e.getMessage() + "]  while executing [" + stackTraces[1].getMethodName() + "] called from [" + stackTraces[2].getMethodName() + "].");

            return false;
        }

        return true;
    }

    default boolean resetFields() {
        try {
            for (final Field field : this.getClass().getDeclaredFields()) {
                final Input input = field.getAnnotation(Input.class);

                if (input == null || input.type() == InputType.OPTIONS) continue;

                field.setAccessible(true);

                field.set(this, null);
            }
        } catch (Exception e) {
            final StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();

            e.printStackTrace();

            System.err.println("Exception: [ "+ e.getClass().getName() + "] with message [" + e.getMessage() + "]  while executing [" + stackTraces[1].getMethodName() + "] called from [" + stackTraces[2].getMethodName() + "].");

            return false;
        }

        return true;
    }

    default boolean bindButton(final GridPane grid, final Button button) {
        final Map<Integer, Field> instanceFields = new HashMap<>();
        final Map<Integer, TextField> textFields = new HashMap<>();
        final Map<Integer, ComboBox<IForm>> comboBoxes = new HashMap<>();
        final Map<Integer, DatePicker> datePickers = new HashMap<>();

        try {

            for (final Node node : grid.getChildren()) {
                final int column = GridPane.getColumnIndex(node);
                final int row = GridPane.getRowIndex(node);

                if (column != 1) continue;

                if (node instanceof TextField) textFields.put(row, (TextField) node);
                else if (node instanceof ComboBox) comboBoxes.put(row, (ComboBox<IForm>) node);
                else if (node instanceof DatePicker) datePickers.put(row, (DatePicker) node);
                else throw new NotSupportedNodeFieldTypeException();
            }

            for (Field field : this.getClass().getDeclaredFields()) {
                final Input input = field.getAnnotation(Input.class);

                if (input == null || input.type() == InputType.OPTIONS) continue;

                instanceFields.put(input.position(), field);
            }

            button.setOnAction(e -> {
                for (final Map.Entry<Integer, Field> positionField : instanceFields.entrySet()) {
                    final Integer position = positionField.getKey();
                    final Field classField = positionField.getValue();
                    final Input input = classField.getAnnotation(Input.class);

                    try {
                        classField.setAccessible(true);

                        switch (input.type()) {
                            case SIMPLE:
                                final TextField textField = textFields.get(position);

                                if (textField == null) throw new NoCorrespondingFieldException();

                                IForm.parseAndAssignFromTextField(this, textField, classField);

                                break;
                            case SELECT:
                                final ComboBox<IForm> comboBox = comboBoxes.get(position);

                                if (comboBox == null) throw new NoCorrespondingFieldException();

                                IForm.parseAndAssignFromComboField(this, comboBox, classField);

                                break;
                            case DATE:
                                final DatePicker date = datePickers.get(position);

                                if (date == null) throw new NoCorrespondingFieldException();

                                IForm.parseAndAssignFromDatePicker(this, date, classField);

                                break;
                            default: throw new NotSupportedNodeFieldTypeException();
                        }
                    } catch (Exception ex) {
                        final StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();

                        System.err.println("Exception: [ "+ ex.getClass().getName() + "] with message [" + ex.getMessage() + "] when parsing and assigning grid values to instance on action of button while executing [" + stackTraces[1].getMethodName() + "] called from [" + stackTraces[2].getMethodName() + "].");
                    }
                }
            });

        } catch (Exception e) {
           final StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();

            System.err.println("Exception: [ "+ e.getClass().getName() + "] with message [" + e.getMessage() + "]  while executing [" + stackTraces[1].getMethodName() + "] called from [" + stackTraces[2].getMethodName() + "].");

            return false;
        }

        return true;
    }

    private static void parseAndAssignFromDatePicker(
        final IForm instance,
        final DatePicker date,
        final Field classField
    ) {
        final LocalDate local = date.getValue();

        try {
            if (local != null) {
                final LocalDateTime localTime = local.atStartOfDay();

                classField.set(instance, localTime);
            } else {
                classField.set(instance, null);
            }
        } catch (Exception e) {
            final StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();

            System.err.println("Exception: [ "+ e.getClass().getName() + "] with message [" + e.getMessage() + "]  while executing [" + stackTraces[1].getMethodName() + "] called from [" + stackTraces[2].getMethodName() + "].");
        }
    }

    private static void parseAndAssignFromTextField(
        final IForm instance,
        final TextField textField,
        final Field classField
    ) {
        final Class<?> clazz = classField.getType();
        final String rawValue = textField.getText();

        try {
            if (clazz.equals(String.class)) classField.set(instance, rawValue);
            else if (clazz.equals(Integer.class)) classField.set(instance, Integer.valueOf(rawValue));
            else if (clazz.equals(Short.class)) classField.set(instance, Short.valueOf(rawValue));
            else if (clazz.equals(Float.class)) classField.set(instance, Float.valueOf(rawValue));
            else if (clazz.equals(Double.class)) classField.set(instance, Double.valueOf(rawValue));
            else if (clazz.equals(Long.class)) classField.set(instance, Long.valueOf(rawValue));
            else if (clazz.equals(Boolean.class)) classField.set(instance, Boolean.valueOf(rawValue));
            else throw new NotSupportedObjectForFormParsingException();
        } catch (Exception e) {
            final StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();

            System.err.println("Exception: [ "+ e.getClass().getName() + "] with message [" + e.getMessage() + "]  while executing [" + stackTraces[1].getMethodName() + "] called from [" + stackTraces[2].getMethodName() + "].");
        }
    }

    private static void parseAndAssignFromComboField(
        final IForm instance,
        final ComboBox<IForm> comboBox,
        final Field classField
    ) {
        final Class<?> clazz;
        final IForm selected;

        try {
            clazz = classField.getType();
            selected = comboBox.getValue();

            if (!clazz.isInstance(selected)) throw new NotSupportedObjectForFormParsingException();

            classField.setAccessible(true);
            classField.set(instance, selected);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
