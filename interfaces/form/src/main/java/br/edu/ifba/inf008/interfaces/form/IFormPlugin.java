package br.edu.ifba.inf008.interfaces.form;

import br.edu.ifba.inf008.interfaces.controller.IPlugin;

import javafx.scene.layout.*;

public interface IFormPlugin<T extends IForm> extends IDatabasePlugin<T> implements IPlugin {
    default boolean getSaveForm(final T formInstance, final GridPane grid, final Button button) {

        return true;
    }

    default boolean getDeleteForm(final T formInstance, final GridPane grid, final Button button) {

    }

    default boolean getUpdateForm(final T formInstance, final GridPane grid, final Button button) {

    }
}
