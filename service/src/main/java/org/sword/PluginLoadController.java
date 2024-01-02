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

    @Resource
    private RequestMappingHandlerMapping requestMappingHandlerMapping;
    @Resource
    private PluginRegistrar pluginRegistrar;
    @Resource
    private SqlSession sqlSession;

    @GetMapping
    public boolean load() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        //必须使用同一个类加载器
        URLClassLoader classLoader = PluginLoader.getClassLoader();
        if (classLoader == null) {
            return false;
        }

        //加载mapper插件并注册为bean
        List<IPluginMapper> iPluginMappers = PluginLoader.loadMapperPlugins(classLoader, sqlSession);
        for (IPluginMapper iPluginMapper : iPluginMappers) {
            pluginRegistrar.registerPlugin("userMapper", iPluginMapper, iPluginMapper.getClass());
        }

        //加载service插件并注册为bean
        List<IPluginService> iPluginServices = PluginLoader.loadServicePlugins(classLoader);
        for (IPluginService iPluginService : iPluginServices) {
            Class<?> interfaceType = (Class<?>) iPluginService.getClass().getGenericInterfaces()[0];
            pluginRegistrar.registerPlugin("userService", iPluginService, interfaceType);
        }

        //加载controller插件并注册为bean
        List<IPluginController> iPluginControllers = PluginLoader.loadControllerPlugins(classLoader);
        for (IPluginController iPluginController : iPluginControllers) {
            pluginRegistrar.registerPlugin("userController", iPluginController, iPluginController.getClass());
            Method method = requestMappingHandlerMapping.getClass().getSuperclass().getSuperclass().getDeclaredMethod("detectHandlerMethods", Object.class);
            method.setAccessible(true);
            method.invoke(requestMappingHandlerMapping, "userController");
        }
        return true;
    }
}
