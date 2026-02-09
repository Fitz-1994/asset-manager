package com.assetmanager.mapper;

import com.assetmanager.entity.Position;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PositionMapper {

    int insert(Position position);

    int update(Position position);

    int deleteById(Long id);

    Position findById(Long id);

    List<Position> findByAccountId(@Param("accountId") Long accountId);

    Position findByAccountIdAndTargetId(@Param("accountId") Long accountId, @Param("targetId") Long targetId);
}
