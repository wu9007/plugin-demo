package org.sword;

import java.net.MalformedURLException;
import java.util.List;

/**
 * @author chuan
 * @version 1.0
 * @since ${DATE}
 */
public class Main {
    public static void main(String[] args) throws MalformedURLException {
        System.out.println("Start to load plugins");
        List<IPluginService> pluginServices = PluginLoader.loadPlugins();
        System.out.println("Already load plugin " + pluginServices.size());
        for (IPluginService pluginService : pluginServices) {
            System.out.println("========== Plugin " + pluginService.name() + " ==========");
            System.out.println("Start plugin service:");
            pluginService.service();
        }
    }
}