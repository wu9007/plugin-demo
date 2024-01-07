package org.sword;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author chuan
 * @version 1.0
 * @since 2023/12/7
 */
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Override
    public UserDto detail(String userId) {
        UserDo userDo = userMapper.selectById("1");
        System.out.println(userDo);
        UserDto userDto = new UserDto();
        userDto.setUserId(userId);
        userDto.setName("zhangsan");
        return userDto;
    }
}
