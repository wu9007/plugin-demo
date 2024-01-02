package org.sword.test;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.sword.IPluginMapper;

/**
 * @author chuan
 */
@Mapper
public interface UserMapper extends BaseMapper<UserDo>, IPluginMapper {
}
