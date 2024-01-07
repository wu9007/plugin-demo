package org.sword;

import org.apache.ibatis.session.SqlSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.List;

/**
 * @author chuan
 * @since 2023/12/30
 */
@RequestMapping("/load")
@RestController
public class PluginLoadController {
    private static final String PLUGIN_PATH = "plugins";

    @Resource
    private RequestMappingHandlerMapping requestMappingHandlerMapping;
    @Resource
    private PluginRegistrar pluginRegistrar;
    @Resource
    private SqlSession sqlSession;

    @GetMapping
    public boolean load() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        //一组插件必须使用同一个类加载器
        URLClassLoader classLoader = PluginLoader.getClassLoader(PLUGIN_PATH, "plugin-2-1.0-SNAPSHOT.jar");
        if (classLoader == null) {
            return false;
        }

        PluginLoader.PluginInstancePack pluginInstancePack = PluginLoader.loadPluginInstancePack(classLoader, sqlSession);
        //获取mapper插件并注册为bean
        List<Object> mapperProxyList = pluginInstancePack.getMapperInstanceList();
        for (Object mapperProxy : mapperProxyList) {
            Class<?> interfaceType = (Class<?>) mapperProxy.getClass().getGenericInterfaces()[0];
            pluginRegistrar.registerPlugin("userMapper", mapperProxy, interfaceType);
        }

        //获取component插件并注册为bean
        List<Object> componentInstanceList = pluginInstancePack.getComponentInstanceList();
        for (Object componentInstance : componentInstanceList) {
            Class<?> interfaceType = (Class<?>) componentInstance.getClass().getGenericInterfaces()[0];
            pluginRegistrar.registerPlugin("userService", componentInstance, interfaceType);
        }

        //获取controller插件并注册为bean
        List<Object> controllerInstanceList = pluginInstancePack.getControllerInstanceList();
        for (Object controller : controllerInstanceList) {
            pluginRegistrar.registerPlugin("userController", controller, controller.getClass());
            Method method = requestMappingHandlerMapping.getClass().getSuperclass().getSuperclass().getDeclaredMethod("detectHandlerMethods", Object.class);
            method.setAccessible(true);
            method.invoke(requestMappingHandlerMapping, "userController");
        }
        return true;
    }
}
