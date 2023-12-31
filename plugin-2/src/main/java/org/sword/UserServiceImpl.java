package org.sword;

import org.springframework.stereotype.Service;

/**
 * @author chuan
 * @version 1.0
 * @since 2023/12/7
 */
@Service
public class UserServiceImpl implements UserService {
    @Override
    public UserDto detail(String userId) {
        UserDto userDto = new UserDto();
        userDto.setUserId(userId);
        userDto.setUsername("zhangsan");
        return userDto;
    }
}
