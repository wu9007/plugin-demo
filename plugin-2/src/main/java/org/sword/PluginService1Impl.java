package org.sword;

/**
 * @author chuan
 * @version 1.0
 * @since 2023/12/7
 */
public class PluginService1Impl implements IPluginService {
    @Override
    public void service() {
        System.out.println("Start execute plugin 1 service");
    }

    @Override
    public String name() {
        return this.getClass().getName();
    }

    @Override
    public String version() {
        return "v1";
    }
}
