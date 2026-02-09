package com.assetmanager.mapper;

import com.assetmanager.entity.ManualBalance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ManualBalanceMapper {

    int insert(ManualBalance manualBalance);

    ManualBalance findLatestByAccountId(@Param("accountId") Long accountId);

    List<ManualBalance> findByAccountIdOrderByRecordedAtDesc(@Param("accountId") Long accountId, @Param("limit") int limit);
}
