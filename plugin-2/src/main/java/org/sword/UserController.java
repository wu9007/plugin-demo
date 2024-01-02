package org.sword;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author chuan
 * @since 2023/12/31
 */
@RestController
@RequestMapping("/user")
public class UserController implements IPluginController {

    @Resource
    private UserService userService;
    @Resource
    private UserMapper userMapper;

    @GetMapping("/{id}")
    public UserDto detail(@PathVariable String id) {
        userMapper.selectById(id);
        return userService.detail(id);
    }
}
