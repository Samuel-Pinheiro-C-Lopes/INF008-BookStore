package br.edu.ifba.inf008.shell;

import br.edu.ifba.inf008.interfaces.controller.IUIController;
import br.edu.ifba.inf008.interfaces.controller.ICore;
import br.edu.ifba.inf008.shell.PluginController;

import br.edu.ifba.inf008.interfaces.controller.IPluginController;
import br.edu.ifba.inf008.interfaces.controller.IPlugin;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tab;
import javafx.geometry.Side;
import javafx.scene.Node;

import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.Map;
import java.util.HashMap;

public class UIController extends Application implements IUIController
{
    private static volatile UIController instance = null;
    private IPluginController pluginController;

    private final VBox displaysButtons = new VBox();
    private final VBox formsButtons = new VBox();
    private final TitledPane displaysMenu = new TitledPane("Displays", displaysButtons);
    private final TitledPane formsMenu = new TitledPane("Forms", formsButtons);
    private final Accordion menuAccordion = new Accordion(displaysMenu, formsMenu);
    private final VBox menu = new VBox(menuAccordion);

    private final StackPane content = new StackPane();
    private final BorderPane root = new BorderPane();

    private final Map<Button, VBox> displayButtonMap = new HashMap<>();
    private final Map<Button, VBox> formButtonMap = new HashMap<>();

    public UIController() { }

    public static IUIController getInstance() {
        return instance;
    }

    @Override
    public void init() {
        instance = this;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Bookstore Application");

        menu.setStyle("-fx-padding: 10; -fx-background-color: #eeeeee;");
        root.setLeft(menu);
        root.setCenter(content);

        final Scene scene = new Scene(root, 1000, 900);
        primaryStage.setScene(scene);
        primaryStage.show();

        pluginController = Core.getInstance().getPluginController();

        pluginController.initHandlers();
        pluginController.initViewables();
    }

    @Override
    public VBox createForm(final String formTitle) {
        final VBox formContent = new VBox();

        final Label titleLabel = new Label(formTitle);

        formContent.getChildren().add(titleLabel);

        this.addFormButton(formTitle, formContent);

        return formContent;
    }

    @Override
    public <T> ComboBox<T> addComboToForm(final VBox form) {
        final ComboBox<T> combo = new ComboBox<>();

        form.getChildren().add(combo);

        return combo;
    }

    @Override
    public GridPane addGridToForm(final VBox form) {
        final GridPane grid = new GridPane();

        form.getChildren().add(grid);

        return grid;
    }

    @Override
    public Button addButtonToForm(final VBox form, final String buttonText) {
        final Button button = new Button();

        button.setText(buttonText);

        form.getChildren().add(button);

        return button;
    }

    @Override
    public VBox createDisplay(final String displayTitle) {
        final VBox displayContent = new VBox();

        final Label titleLabel = new Label(displayTitle);

        displayContent.getChildren().add(titleLabel);

        this.addDisplayButton(displayTitle, displayContent);

        return displayContent;
    }

    @Override
    public <T> TableView<T> addTableToDisplay(final VBox display) {
        final TableView<T> table = new TableView<T>();

        display.getChildren().add(table);

        return table;
    }

    @Override
    public Button addButtonToDisplay(final VBox display, final String buttonText) {
        final Button button = new Button();

        button.setText(buttonText);

        display.getChildren().add(button);

        return button;
    }

    // Method to set up a new display
    private void addDisplayButton(final String buttonText, final VBox displayContent) {
        final Button newButton = new Button(buttonText);

        // Add the button and its associated VBox to the map
        displayButtonMap.put(newButton, displayContent);

        // Set the action for the button
        newButton.setOnAction(e -> {
            // Clear all existing children from the StackPane
            content.getChildren().clear();

            // Add the new VBox from the map to the StackPane
            content.getChildren().add(displayButtonMap.get(e.getSource()));
        });

        // Add the new button to the appropriate menu VBox
        displaysButtons.getChildren().add(newButton);
    }

    // Method to set up a new form
    private void addFormButton(final String buttonText, final VBox formContent) {
        final Button newButton = new Button(buttonText);

        // Add the button and its associated VBox to the map
        formButtonMap.put(newButton, formContent);

        // Set the action for the button
        newButton.setOnAction(e -> {
            // Clear all existing children from the StackPane
            content.getChildren().clear();

            // Add the new VBox from the map to the StackPane
            content.getChildren().add(formButtonMap.get(e.getSource()));
        });

        // Add the new button to the appropriate menu VBox
        formsButtons.getChildren().add(newButton);
    }

    /*
    public MenuItem createMenuItem(String menuText, String menuItemText) {
        // Criar o menu caso ele nao exista
        Menu newMenu = null;
        for (Menu menu : menuBar.getMenus()) {
            if (menu.getText() == menuText) {
                newMenu = menu;
                break;
            }
        }

        if (newMenu == null) {
            newMenu = new Menu(menuText);
            menuBar.getMenus().add(newMenu);
        }

        // Criar o menu item neste menu
        MenuItem menuItem = new MenuItem(menuItemText);
        newMenu.getItems().add(menuItem);

        return menuItem;
    }

    public boolean createTab(String tabText, Node contents) {
        Tab tab = new Tab();
        tab.setText(tabText);
        tab.setContent(contents);
        tabPane.getTabs().add(tab);

        return true;
    }
    */
}
