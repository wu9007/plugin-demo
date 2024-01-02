package org.sword;

import org.apache.ibatis.binding.MapperProxyFactory;
import org.apache.ibatis.session.SqlSession;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 加载插件
 *
 * @author chuan
 * @version 1.0
 * @since 2023/12/7
 */
public class PluginLoader {

    private static final String PLUGIN_PATH = "plugins";

    /**
     * 加载mapper插件
     *
     * @param urlClassLoader 类加载器
     * @return 插件集合
     */
    public static List<IPluginMapper> loadMapperPlugins(URLClassLoader urlClassLoader, SqlSession  sqlSession) {
        ServiceLoader<IPluginMapper> mapperLoader = ServiceLoader.load(IPluginMapper.class, urlClassLoader);
        List<IPluginMapper> mapperPlugins = new ArrayList<>();
        try {
            //FIXME ServiceLoader 无法加载接口，需要无参构造方法
            for (IPluginMapper plugin : mapperLoader) {
                Class<?> mapperInterface = plugin.getClass().getInterfaces()[0];
                MapperProxyFactory<?> mapperProxyFactory = new MapperProxyFactory<>(mapperInterface);
                IPluginMapper mapperProxy = (IPluginMapper) mapperProxyFactory.newInstance(sqlSession);
                mapperPlugins.add(mapperProxy);
                System.out.println("加载成功：" + plugin.getClass());
            }
        } catch (ServiceConfigurationError error) {
            error.printStackTrace();
        }
        return mapperPlugins;
    }

    /**
     * 加载service插件
     *
     * @param urlClassLoader 类加载器
     * @return 插件集合
     */
    public static List<IPluginService> loadServicePlugins(URLClassLoader urlClassLoader) {
        ServiceLoader<IPluginService> serviceLoader = ServiceLoader.load(IPluginService.class, urlClassLoader);
        List<IPluginService> servicePlugins = new ArrayList<>();
        for (IPluginService plugin : serviceLoader) {
            servicePlugins.add(plugin);
            System.out.println("加载成功：" + plugin.getClass());
        }
        return servicePlugins;
    }

    /**
     * 加载controller插件
     *
     * @param urlClassLoader 类加载器
     * @return 插件集合
     */
    public static List<IPluginController> loadControllerPlugins(URLClassLoader urlClassLoader) {
        ServiceLoader<IPluginController> controllerPlugin = ServiceLoader.load(IPluginController.class, urlClassLoader);
        List<IPluginController> controllerPlugins = new ArrayList<>();
        for (IPluginController plugin : controllerPlugin) {
            controllerPlugins.add(plugin);
            System.out.println("加载成功：" + plugin.getClass());
        }
        return controllerPlugins;
    }

    /**
     * 创建类加载器
     *
     * @return 类加载器
     */
    public static URLClassLoader getClassLoader() {
        File parentDir = new File(PLUGIN_PATH);
        File[] files = parentDir.listFiles();
        if (files == null) {
            return null;
        }
        List<File> jarFiles = Arrays.stream(files)
                .filter(file -> file.getName().endsWith(".jar"))
                .collect(Collectors.toList());
        URL[] urls = new URL[jarFiles.size()];
        for (int index = 0; index < urls.length; index++) {
            try {
                urls[index] = new URL("file:" + jarFiles.get(index).getAbsolutePath());
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
        return new URLClassLoader(urls);
    }
}
