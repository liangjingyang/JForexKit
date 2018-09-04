package com.jforexcn.inbox.plugin;

/**
 * Created by simple(simple.continue@gmail.com) on 29/11/2017.
 */

import com.dukascopy.api.IConsole;
import com.dukascopy.api.JFException;
import com.dukascopy.api.plugins.IPluginContext;
import com.dukascopy.api.plugins.Plugin;
import com.dukascopy.api.plugins.menu.IPluginMenu;

import javax.swing.JMenu;

public class SimplePlugin extends Plugin {

    private IConsole console;

    @Override
    public void onStart(IPluginContext context) throws JFException {
        console = context.getConsole();
        console.getOut().println("Plugin started");
        IPluginMenu pluginMenu = context.addMenu("Suggested Position");
        JMenu menu = pluginMenu.getMenu();

    }

    @Override
    public void onStop() throws JFException {
        console.getOut().println("Plugin stopped");
    }

}
