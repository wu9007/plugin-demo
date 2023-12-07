package org.sword;

/**
 * @author chuan
 * @version 1.0
 * @since 2023/12/7
 */
public interface IPluginService {

    /**
     * 执行服务
     */
    void service();

    /**
     * 插件名称
     *
     * @return name
     */
    String name();

    /**
     * 插件版本号
     *
     * @return version
     */
    String version();
}
