package com.assetmanager.mapper;

import com.assetmanager.entity.AssetSnapshot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.List;

@Mapper
public interface AssetSnapshotMapper {

    int insert(AssetSnapshot snapshot);

    List<AssetSnapshot> findByUserIdOrderBySnapshotAtDesc(@Param("userId") Long userId, @Param("limit") int limit);

    List<AssetSnapshot> findByUserIdAndSnapshotAtBetweenOrderBySnapshotAtAsc(
        @Param("userId") Long userId, @Param("from") Instant from, @Param("to") Instant to);

    AssetSnapshot findById(@Param("id") Long id);
}
