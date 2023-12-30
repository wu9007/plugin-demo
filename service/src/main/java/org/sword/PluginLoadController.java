package org.sword;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.List;

/**
 * @author chuan
 * @since 2023/12/30
 */
@RequestMapping("/load")
@RestController
public class PluginLoadController {

    @Resource
    private GenericApplicationContext applicationContext;
    @Resource
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @GetMapping
    public boolean load() throws MalformedURLException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        List<IPluginController> iPluginControllers = PluginLoader.loadControllerPlugins();
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
        for (IPluginController iPluginController : iPluginControllers) {
            BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(iPluginController.getClass());
            defaultListableBeanFactory.registerBeanDefinition("myc", beanDefinitionBuilder.getBeanDefinition());
            Method method = requestMappingHandlerMapping.getClass().getSuperclass().getSuperclass().getDeclaredMethod("detectHandlerMethods", Object.class);
            method.setAccessible(true);
            method.invoke(requestMappingHandlerMapping, "myc");
            System.out.println("加载成功：" + iPluginController.getClass());
        }
        return true;
    }
}
