package br.edu.ifba.inf008.shell;

import br.edu.ifba.inf008.interfaces.controller.*;

import javafx.application.Application;
import javafx.application.Platform;

public class Core extends ICore
{
    private final IDatabaseController databaseController = DatabaseController.getInstance("jdbc:mariadb://localhost:3306/bookstore", "root", "root");
    private final IPluginController pluginController = PluginController.getInstance();

    private Core() {}

    public static boolean init() {
        instance = new Core();

        UIController.launch(UIController.class);

        return true;
    }

    public IUIController getUIController() {
        return UIController.getInstance();
    }

    public IPluginController getPluginController() {
        return pluginController;
    }

    public IDatabaseController getDatabaseController() {
        return databaseController;
    }
}
