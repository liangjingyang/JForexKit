package com.jforexcn.wiki.plugin;

/**
 * Created by simple(simple.continue@gmail.com) on 05/12/2017.
 *
 * https://www.dukascopy.com/wiki/en/development/get-started-api/use-in-jforex/plugins
 * https://www.jforexcn.com/development/getting-started/general/create-a-plugin.html
 */

import com.dukascopy.api.IConsole;
import com.dukascopy.api.JFException;
import com.dukascopy.api.plugins.IPluginContext;
import com.dukascopy.api.plugins.Plugin;

public class SimplePlugin extends Plugin {

    private IConsole console;

    @Override
    public void onStart(IPluginContext context) throws JFException {
        console = context.getConsole();
        console.getOut().println("Plugin started");
    }

    @Override
    public void onStop() throws JFException {
        console.getOut().println("Plugin stopped");
    }

}