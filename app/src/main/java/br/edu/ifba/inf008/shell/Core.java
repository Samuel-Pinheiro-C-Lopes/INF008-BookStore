package br.edu.ifba.inf008.shell;

import br.edu.ifba.inf008.interfaces.*;
import javafx.application.Application;
import javafx.application.Platform;

public class Core extends ICore
{
    // singleton

    private Core() {}

    public static boolean init() {
        if (instance != null) {
            System.out.println("Fatal error: " + Core.class.getSimpleName() + " is already initialized!");
            System.exit(-1);
        }

        instance = new Core();

        instance.getIOController().test();

        UIController.launch(UIController.class);

        if (instance.getPluginController().init() == false)
            System.out.println("\n\n\nERROR\n\n\n");

        return true;
    }

    public IUIController getUIController() {
        return UIController.getInstance();
    }

    public IAuthenticationController getAuthenticationController() {
        return authenticationController;
    }

    public IIOController getIOController() {
        return ioController;
    }

    public IPluginController getPluginController() {
        return pluginController;
    }

    private final IAuthenticationController authenticationController = new AuthenticationController();
    private final IIOController ioController = new IOController();
    private final IPluginController pluginController = new PluginController();
}
