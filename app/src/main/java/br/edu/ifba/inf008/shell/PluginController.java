package br.edu.ifba.inf008.shell;

import br.edu.ifba.inf008.App;

import br.edu.ifba.inf008.interfaces.controller.IPluginController;
import br.edu.ifba.inf008.interfaces.controller.IPlugin;
import br.edu.ifba.inf008.interfaces.controller.ICore;

import br.edu.ifba.inf008.interfaces.database.IDatabasePlugin;
import br.edu.ifba.inf008.interfaces.form.IFormPlugin;
import br.edu.ifba.inf008.interfaces.grid.IGridPlugin;

import br.edu.ifba.inf008.interfaces.database.IEntity;
import br.edu.ifba.inf008.interfaces.form.IForm;
import br.edu.ifba.inf008.interfaces.grid.IGrid;

import java.io.File;
import java.io.FilenameFilter;

import java.util.List;
import java.util.Vector;
import java.util.ArrayList;

import java.net.URL;
import java.net.URLClassLoader;

class PluginController implements IPluginController
{
    private static volatile PluginController instance = null;

    public static PluginController getInstance() {
        synchronized (PluginController.class) {
            if (instance == null)
                 instance = new PluginController();
                 instance.init();
        }

        return instance;
    }

    @Override
    public boolean init() {
        return instance.loadPlugins();
    }

    private PluginController() { }

    private final List<IPlugin> plugins = new Vector<>();

    @Override
    public void initHandlers() {
        for (IPlugin plugin : plugins)
            if (plugin instanceof IFormPlugin)
                ((IFormPlugin)plugin).initForm();
    }

    @Override
    public void initViewables() {
        for (IPlugin plugin : plugins)
            if (plugin instanceof IGridPlugin)
                ((IGridPlugin)plugin).initGrid();
    }

    public boolean loadPlugins() {
        final File currentDir = new File("../plugins");
        final FilenameFilter jarFilter;
        final URLClassLoader ulc;
        final String[] loadedPlugins;
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

            loadedPlugins = currentDir.list(jarFilter);
            jars = new URL[loadedPlugins.length];

            for (i = 0; i < loadedPlugins.length; i++)
                jars[i] = (new File("../plugins/" + loadedPlugins[i])).toURL();

            ulc = new URLClassLoader(jars, App.class.getClassLoader());

            for (i = 0; i < loadedPlugins.length; i++) {
                pluginName = loadedPlugins[i].split("\\.")[0];

                clazz = Class.forName("br.edu.ifba.inf008.plugins." + pluginName, true, ulc);

                plugin = (IPlugin) Class.forName("br.edu.ifba.inf008.plugins." + pluginName, true, ulc).newInstance();

                this.plugins.add(plugin);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error: " + e.getClass().getName() + " - " + e.getMessage());
            return false;
        }
    }

}
