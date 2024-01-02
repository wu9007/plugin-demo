package org.sword.test;

import org.apache.ibatis.binding.MapperProxyFactory;
import org.apache.ibatis.session.SqlSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author chuan
 * @since 2023/12/30
 */
@RequestMapping("/test")
@RestController
public class TestController {

    @Resource
    private SqlSession sqlSession;

    @GetMapping
    public UserDo test() {
        MapperProxyFactory<?> mapperProxyFactory = new MapperProxyFactory<>(UserMapper.class);
        UserMapper userMapper = (UserMapper) mapperProxyFactory.newInstance(sqlSession);
        return userMapper.selectById("1");
    }
}
