package br.edu.ifba.inf008.interfaces.controller;

import javafx.scene.control.MenuItem;
import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.scene.control.*;

public interface IUIController
{
    VBox createForm(String formTitle);
    <T> ComboBox<T> addComboToForm(VBox form);
    GridPane addGridToForm(VBox form);
    Button addButtonToForm(VBox form, String buttonText);

    VBox createDisplay(String displayTitle);
    <T> TableView<T> addTableToDisplay(VBox display);
    Button addButtonToDisplay(VBox display, String buttonText);
}
