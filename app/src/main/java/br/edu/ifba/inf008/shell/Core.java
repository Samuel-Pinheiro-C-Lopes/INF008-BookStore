package br.edu.ifba.inf008.shell;

import br.edu.ifba.inf008.interfaces.controller.*;

import javafx.application.Application;
import javafx.application.Platform;

public class Core extends ICore
{
    private final IDatabaseController databaseController = new DatabaseController("jdbc:mariadb://localhost:3306/bookstore", "root", "root");
    private final IPluginController pluginController;
    private final IUIController uiController;

    private Core() {}

    public static boolean init() {
        checkPreInit();

        instance = new Core();

        pluginController = initializePluginController();
        databaseController = initializeDatabaseController();
        uiController = initializeUIController();

        return true;
    }

    public IUIController getUIController() {
        return uiController;
    }

    public IPluginController getPluginController() {
        return pluginController;
    }

    public IDatabaseController getDatabaseController() {
        return databaseController;
    }

    private static IDatabaseController initializeDatabaseController() {
        final IDatabaseController controller;

        if (DatabaseController.init("jdbc:mariadb://localhost:3306/bookstore", "root", "root") == false) {
            System.err.println("Fatal error: " + DatabaseController.class.getSimpleName() + " couldn't be initialized!");
            System.exit(-1);
        }

        controller = DatabaseController.getInstance();

        if (controller == null) {
            System.err.println("Fatal error: " + DatabaseController.class.getSimpleName() + " instance is null after being initialized!");
            System.exit(-1);
        }

        return controller;
    }

    private static IPluginController initializePluginController() {
        final IPluginController controller;

        if (PluginController.init() == false) {
            System.err.println("Fatal error: " + PluginController.class.getSimpleName() + " couldn't be initialized!");
            System.exit(-1);
        }

        controller = PluginController.getInstance();

        if (controller == null) {
            System.err.println("Fatal error: " + PluginController.class.getSimpleName() + " instance is null after being initialized!");
            System.exit(-1);
        }

        return controller;
    }

    private static IUIController intiializeUIController() {
        final IUIController controller;

        UIController.launch(UIController.class);

        controller = UIController.getInstance();

        if (controller == null) {
            System.err.println("Fatal error: " + UIController.class.getSimpleName() + " instance is null after being initialized!");
            System.exit(-1);
        }

        return controller;
    }

    private static void checkPreInit() {
        if (instance != null) {
            System.err.println("Fatal error: " + Core.class.getSimpleName() + " is already initialized!");
            System.exit(-1);
        }
    }
}
