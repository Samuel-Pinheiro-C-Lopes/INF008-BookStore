package br.edu.ifba.inf008.interfaces.form;

import br.edu.ifba.inf008.interfaces.form.annotations.*;
import br.edu.ifba.inf008.interfaces.form.exceptions.*;

import javafx.scene.layout.*;

import java.lang.reflect.Field;

import java.util.List;
import java.util.Map;
import java.util.AbstractMap;
import java.util.HashMap;


public interface IForm extends IEntity {

    abstract String getSelectDisplayText();

    default Button setFormGrid(final GridPane grid) {
        final Map<Integer, ComboBox<IForm>> comboMapper = new HashMap<>();
        final Map<Integer, List<IForm>> optionsMapper = new HashMap<>();

        try {
            for (Field field : this.getClass().getDeclaredFields()) {
                final Input input = field.getAnnotation(Input.class);

                if (input == null)
                    continue;

                field.setAccessible(true);

                if (input.type() != OPTIONS)
                    grid.add(new Label(input.label() + ": "), 0, input.position());

                switch (input.type()) {
                    case SIMPLE:
                        grid.add(new TextField(), 1, input.position());
                        break;

                    case OPTIONS:
                        final Object value = field.get(this);

                        if (value instanceof List) optionsMapper.put(input.position(), value);
                        else throw new NotIterableOptionException();

                        break;
                    case SELECTION:
                        comboMapper.put(input.position(), new ComboBox<IForm>());
                        break;
                }
            }

            for (final Map.Entry<Integer, ComboBox<IForm>> positionCombo : comboMapper.entrySet()) {
                final List<IForm> currentOptions = optionsMapper.get(positionCombo.getKey());
                final ComboBox<IForm> combo = positionCombo.getValue();
                if (currentOptions == null)
                    throw new NoOptionSetForSelectException();

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
            e.printStackTrace();
            return null;
        }

        return IForm.generateBindingButton(this, grid);
    }

    private static Button generateBindingButton(IForm instance, final GridPane grid) {
        final Button button = new Button();
        final Map<Integer, Field> instanceFields = new HashMap<>();
        final Map<Integer, TextField> textFields = new HashMap<>();
        final Map<Integer, ComboBox<IForm>> comboBoxes = new HashMap<>();

        for (Node node : grid.getChildren()) {
            final int column = GridPane.getColumnIndex(node);
            final int row = GridPane.getRowIndex(node);

            if (column != 1) continue;

            if (node instanceof TextField) textFields.put(row, (TextField) node);
            else if (node instanceof ComboBox) comboBoxes.put(row, (ComboBox) node);
            else throw new NotSupportedNodeFieldTypeException();
        }

        for (Field field : this.getClass().getDeclaredFields()) {
            final Input input = field.getAnnotation(Input.class);

            if (input == null || input.type() == OPTIONS) continue;

            instanceFields.put(input.position(), field);
        }

        button.setOnAction(e -> {
            for (final Map.Entry<Integer, Field> positionField : instanceFields.entrySet()) {
                final Integer position = positionField.getKey();
                final Field classField = positionField.getValue();
                final Input input = classField.getAnnotation(Input.class);

                classField.setAccessible(true);

                switch (input.type()) {
                    case SIMPLE:
                        final TextField textField = textFields.get(position);

                        if (textField == null) throw new NoCorrespondingFieldException();

                        IForm.parseAndAssignFromTextField(this, textField, classField);

                        break;
                    case SELECTION:
                        final ComboBox<IForm> comboBox = comboBoxes.get(position);

                        if (comboBox == null) throw new NoCorrespondingFieldException();

                        IForm.parseAndAssignFromComboField(this, comboBox, classField);

                        break;
                    default:
                        throw new NotSupportedNodeFieldTypeException();
                        break;
                }
            }
        });

        return button;
    }

    private static void parseAndAssignFromTextField(
        final IForm instance,
        final TextField textField,
        final Field classField
    ) {
        final Class<?> clazz = classField.getType();
        final String rawValue = field.getText();

        if (clazz.equals(String.class)) classField.setString(instance, rawValue);
        else if (clazz.equals(Integer.class)) classField.setInt(Integer.valueOf(instance, rawValue));
        else if (clazz.equals(Short.class)) classField.setShort(Short.valueOf(instance, rawValue));
        else if (clazz.equals(Float.class)) classField.setFloat(Float.valueOf(instance, rawValue));
        else if (clazz.equals(Double.class)) classField.setDouble(Double.valueOf(instance, rawValue));
        else if (clazz.equals(Long.class)) classField.setLong(Long.valueOf(instance, rawValue));
        else if (clazz.equals(Boolean.class)) classField.setBoolean(Boolean.valueOf(instance, rawValue));
        else throw new NotSupportedObjectForFormParsingException();
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
