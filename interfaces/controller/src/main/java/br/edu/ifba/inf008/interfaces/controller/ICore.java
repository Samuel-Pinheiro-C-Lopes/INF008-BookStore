package br.edu.ifba.inf008.interfaces.controller;

public abstract class ICore
{
    public static ICore getInstance() {
        return instance;
    }

    public abstract IUIController getUIController();
    public abstract IDatabaseController getDatabaseController();
    public abstract IPluginController getPluginController();

    protected static ICore instance = null;
}
