package org.sword;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author chuan
 */
@Mapper
public interface UserMapper extends BaseMapper<UserDo>, IPluginMapper {
}
