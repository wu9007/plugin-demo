package org.sword;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.binding.MapperProxyFactory;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ServiceConfigurationError;
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

    /**
     * 创建实例（mapper使用proxy特殊处理）
     *
     * @param urlClassLoader 类加载器
     * @return 实例集合
     */
    public static PluginInstancePack loadPluginInstancePack(URLClassLoader urlClassLoader, SqlSession sqlSession) {
        PluginClassPack pluginClassPack = findBaseMapperInterfaces(urlClassLoader);
        List<Object> mapperInstanceList = new ArrayList<>();
        try {
            for (Class<?> mapperInterface : pluginClassPack.getMapperInterfaceList()) {
                MapperProxyFactory<?> mapperProxyFactory = new MapperProxyFactory<>(mapperInterface);
                BaseMapper<?> mapperProxy = (BaseMapper<?>) mapperProxyFactory.newInstance(sqlSession);
                mapperInstanceList.add(mapperProxy);
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
                System.out.println("实例创建成功：" + mapperProxy);
            }
        } catch (ServiceConfigurationError error) {
            error.printStackTrace();
        }

        List<Object> componentInstanceList = new ArrayList<>();
        for (Class<?> componentClazz : pluginClassPack.getComponentClazzList()) {
            try {
                Object componentInstance = componentClazz.newInstance();
                componentInstanceList.add(componentInstance);
                System.out.println("实例创建成功：" + componentInstance);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        List<Object> controllerInstanceList = new ArrayList<>();
        for (Class<?> controllerClazz : pluginClassPack.getControllerClazzList()) {
            try {
                Object controllerInstance = controllerClazz.newInstance();
                controllerInstanceList.add(controllerInstance);
                System.out.println("实例创建成功：" + controllerInstance);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return new PluginInstancePack(mapperInstanceList, componentInstanceList, controllerInstanceList);
    }

    /**
     * 创建类加载器
     *
     * @return 类加载器
     */
    public static URLClassLoader getClassLoader(String pluginPath, String... jarNames) {
        if (jarNames.length == 0) {
            return null;
        }
        File parentDir = new File(pluginPath);
        File[] files = parentDir.listFiles();
        if (files == null) {
            return null;
        }
        List<String> jarNameList = Arrays.stream(jarNames).collect(Collectors.toList());
        List<File> jarFiles = Arrays.stream(files)
                .filter(file -> jarNameList.contains(file.getName()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(jarFiles)) {
            return null;
        }
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
     * 加载类
     *
     * @param classLoader 类加载器
     * @return 接口
     */
    private static PluginClassPack findBaseMapperInterfaces(URLClassLoader classLoader) {
        List<Class<?>> mapperInterfaceList = new ArrayList<>();
        List<Class<?>> componentClazzList = new ArrayList<>();
        List<Class<?>> controllerClazzList = new ArrayList<>();
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
                            System.out.println("加载成功：" + clazz.getName());
                            // 检查是否是接口并且是否继承了BaseMapper
                            if (BaseMapper.class.isAssignableFrom(clazz) && clazz.isInterface()) {
                                mapperInterfaceList.add(clazz);
                            } else if (clazz.isAnnotationPresent(Component.class) || clazz.isAnnotationPresent(Service.class)) {
                                componentClazzList.add(clazz);
                            } else if (clazz.isAnnotationPresent(RestController.class)) {
                                controllerClazzList.add(clazz);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new PluginClassPack(mapperInterfaceList, componentClazzList, controllerClazzList);
    }

    public static class PluginClassPack {
        private final List<Class<?>> mapperInterfaceList;
        private final List<Class<?>> componentClazzList;
        private final List<Class<?>> controllerClazzList;

        public PluginClassPack(List<Class<?>> mapperInterfaceList,
                               List<Class<?>> componentClazzList,
                               List<Class<?>> controllerClazzList) {
            this.mapperInterfaceList = mapperInterfaceList;
            this.componentClazzList = componentClazzList;
            this.controllerClazzList = controllerClazzList;
        }

        protected List<Class<?>> getMapperInterfaceList() {
            return mapperInterfaceList;
        }

        protected List<Class<?>> getComponentClazzList() {
            return componentClazzList;
        }

        protected List<Class<?>> getControllerClazzList() {
            return controllerClazzList;
        }
    }

    public static class PluginInstancePack {
        private final List<Object> mapperInstanceList;
        private final List<Object> componentInstanceList;
        private final List<Object> controllerInstanceList;

        public PluginInstancePack(List<Object> mapperInstanceList,
                                  List<Object> componentInstanceList,
                                  List<Object> controllerInstanceList) {
            this.mapperInstanceList = mapperInstanceList;
            this.componentInstanceList = componentInstanceList;
            this.controllerInstanceList = controllerInstanceList;
        }

        protected List<Object> getMapperInstanceList() {
            return mapperInstanceList;
        }

        protected List<Object> getComponentInstanceList() {
            return componentInstanceList;
        }

        protected List<Object> getControllerInstanceList() {
            return controllerInstanceList;
        }
    }
}
