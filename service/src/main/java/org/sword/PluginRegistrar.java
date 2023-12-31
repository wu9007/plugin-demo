package org.sword;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 注册bean
 *
 * @author chuan
 * @since 2023/12/31
 */
@Component
public class PluginRegistrar {

    DefaultListableBeanFactory defaultListableBeanFactory;

    public PluginRegistrar(GenericApplicationContext applicationContext) {
        defaultListableBeanFactory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
    }

    public void registerPlugin(String beanName, Object instance, Class<?> beanClass) {
        if (!defaultListableBeanFactory.containsBeanDefinition(beanName)) {
            GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
            beanDefinition.setBeanClass(beanClass);
            beanDefinition.setAutowireCandidate(true);
            beanDefinition.setInstanceSupplier(() -> instance);
            defaultListableBeanFactory.registerBeanDefinition(beanName, beanDefinition);
            System.out.println("注册成功：" + instance.getClass());
        }
    }
}
