package org.sword;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/**
 * @author chuan
 * @version 1.0
 * @since 2023/12/7
 */
public class PluginLoader {

    private static final String PLUGIN_PATH = "plugins";

    public static List<IPluginService> loadPlugins() throws MalformedURLException {
        URL[] urls = getUrls();
        URLClassLoader urlClassLoader = new URLClassLoader(urls);
        ServiceLoader<IPluginService> serviceLoader = ServiceLoader.load(IPluginService.class, urlClassLoader);
        List<IPluginService> servicePlugins = new ArrayList<>();
        for (IPluginService plugin : serviceLoader) {
            servicePlugins.add(plugin);
        }
        return servicePlugins;
    }

    public static List<IPluginController> loadControllerPlugins() throws MalformedURLException {
        URL[] urls = getUrls();
        URLClassLoader urlClassLoader = new URLClassLoader(urls);
        ServiceLoader<IPluginController> controllerPlugin = ServiceLoader.load(IPluginController.class, urlClassLoader);
        List<IPluginController> controllerPlugins = new ArrayList<>();
        for (IPluginController plugin : controllerPlugin) {
            controllerPlugins.add(plugin);
        }
        return controllerPlugins;
    }

    private static URL[] getUrls() throws MalformedURLException {
        File parentDir = new File(PLUGIN_PATH);
        File[] files = parentDir.listFiles();
        if (files == null) {
            return new URL[]{};
        }
        List<File> jarFiles = Arrays.stream(files)
                .filter(file -> file.getName().endsWith(".jar"))
                .collect(Collectors.toList());
        URL[] urls = new URL[jarFiles.size()];
        for (int index = 0; index < urls.length; index++) {
            urls[index] = new URL("file:" + jarFiles.get(index).getAbsolutePath());
        }
        return urls;
    }
}
