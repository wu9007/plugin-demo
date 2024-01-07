package org.sword;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.binding.MapperProxyFactory;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
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
    public static List<BaseMapper<?>> loadMapperPlugins(URLClassLoader urlClassLoader, SqlSession sqlSession) {
        List<Class<?>> mapperClazzList = findBaseMapperInterfaces(urlClassLoader);
        List<BaseMapper<?>> mapperProxyList = new ArrayList<>();
        try {
            for (Class<?> mapperInterface : mapperClazzList) {
                MapperProxyFactory<?> mapperProxyFactory = new MapperProxyFactory<>(mapperInterface);
                BaseMapper<?> mapperProxy = (BaseMapper<?>) mapperProxyFactory.newInstance(sqlSession);
                mapperProxyList.add(mapperProxy);
                Configuration configuration = sqlSession.getConfiguration();
                if (!configuration.hasMapper(mapperInterface)) {
                    try {
                        configuration.addMapper(mapperInterface);
                    } catch (Exception exception) {
                        throw new IllegalArgumentException(exception);
                    } finally {
                        ErrorContext.instance().reset();
                    }
                }
                System.out.println("加载成功：" + mapperInterface.getName());
            }
        } catch (ServiceConfigurationError error) {
            error.printStackTrace();
        }
        return mapperProxyList;
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

    /**
     * 获取继承了BaseMapper的接口
     *
     * @param classLoader 类加载器
     * @return 接口
     */
    private static List<Class<?>> findBaseMapperInterfaces(URLClassLoader classLoader) {
        List<Class<?>> targetInterfaces = new ArrayList<>();
        try {
            // 获取加载器中的所有URL
            URL[] urls = classLoader.getURLs();
            for (URL url : urls) {
                // 获取JAR文件路径
                String jarFilePath = url.getFile();
                try (JarFile jarFile = new JarFile(jarFilePath)) {
                    // 遍历JAR文件中的每个条目
                    for (JarEntry entry : Collections.list(jarFile.entries())) {
                        if (entry.getName().endsWith(".class")) {
                            String className = entry.getName().replace('/', '.').substring(0, entry.getName().length() - 6);
                            // 使用类加载器加载类
                            Class<?> clazz = classLoader.loadClass(className);
                            // 检查是否是接口并且是否继承了BaseMapper
                            if (BaseMapper.class.isAssignableFrom(clazz) && clazz.isInterface()) {
                                targetInterfaces.add(clazz);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return targetInterfaces;
    }
}
