package org.sword;

/**
 * @author chuan
 * @since 2023/12/31
 */
public interface UserService extends IPluginService {

    UserDto detail(String userId);
}
