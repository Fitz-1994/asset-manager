package com.assetmanager.mapper;

import com.assetmanager.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    int insert(User user);

    User findById(Long id);

    User findByUsername(String username);

    boolean existsByUsername(String username);
}
