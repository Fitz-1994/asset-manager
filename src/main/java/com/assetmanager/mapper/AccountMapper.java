package com.assetmanager.mapper;

import com.assetmanager.entity.Account;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AccountMapper {

    int insert(Account account);

    int update(Account account);

    int deleteById(Long id);

    Account findById(Long id);

    List<Account> findByUserIdOrderById(@Param("userId") Long userId);
}
