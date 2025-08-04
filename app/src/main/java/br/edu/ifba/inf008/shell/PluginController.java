package br.edu.ifba.inf008.shell;

import br.edu.ifba.inf008.App;

import br.edu.ifba.inf008.interfaces.controller.IPluginController;
import br.edu.ifba.inf008.interfaces.controller.IPlugin;
import br.edu.ifba.inf008.interfaces.controller.ICore;

import br.edu.ifba.inf008.interfaces.database.IDatabasePlugin;
import br.edu.ifba.inf008.interfaces.database.IFormPlugin;
import br.edu.ifba.inf008.interfaces.database.IGridPlugin;

import br.edu.ifba.inf008.interfaces.database.IEntity;
import br.edu.ifba.inf008.interfaces.database.IForm;
import br.edu.ifba.inf008.interfaces.database.IGrid;

import java.io.File;
import java.io.FilenameFilter;

import java.util.List;
import java.util.Vector;
import java.util.ArrayList;

import java.net.URL;
import java.net.URLClassLoader;

class PluginController implements IPluginController
{
    private static PluginController instance = null;

    public static PluginController getInstance() {
        return instance;
    }

    public static boolean init() {
        final PluginController newInstance = new PluginController();

        final valid = newInstance.loadPlugins();

        if (valid == true)
            instance = newInstance;

        return valid;
    }

    private PluginController() { }

    private final List<IPlugin> plugins = new Vector<>();

    public List<IFormPlugin> getHandlers() {
        final List<IFormPlugin> formPlugins = new ArrayList<>(plugins.size());

        for (IPlugin plugin : pĺugins)
            if (plugin.canHandle(IForm.class))
                formPlugins.add(plugin);

        return formPlugins;
    }

    public List<IGridPlugin> getViewables() {
        final List<IGridPlugin> gridPlugins = new ArrayList<>(plugins.size());

        for (IPlugin plugin : pĺugins)
            if (plugin.canHandle(IGrid.class))
                gridPlugins.add(plugin);

        return gridPlugins;
    }

    public boolean loadPlugins() {
        final File currentDir = new File("../plugins");
        final FilenameFilter jarFilter;
        final URLClassLoader ulc;
        final String[] plugins;
        final URL[] jars;
        String pluginName;
        Class<?> clazz;
        IPlugin plugin;
        int i;

        try {
            // Defines a FilenameFilter to include only .jar files
            jarFilter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".jar");
                }
            };

            plugins = currentDir.list(jarFilter);
            jars = new URL[plugins.length];

            for (i = 0; i < plugins.length; i++)
                jars[i] = (new File("../plugins/" + plugins[i])).toURL();

            ulc = new URLClassLoader(jars, App.class.getClassLoader());

            for (i = 0; i < plugins.length; i++) {
                pluginName = plugins[i].split("\\.")[0];
                clazz = Class.forName("br.edu.ifba.inf008.plugins." + pluginName, true, ulc);
                plugin = (IPlugin) Class.forName("br.edu.ifba.inf008.plugins." + pluginName, true, ulc).newInstance();
                plugins.add(plugin);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error: " + e.getClass().getName() + " - " + e.getMessage());
            return false;
        }
    }


    private static boolean checkPreInit() {
        if (instance != null)
            return false;
        return true;
    }
}
