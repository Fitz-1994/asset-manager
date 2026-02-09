package com.assetmanager.mapper;

import com.assetmanager.entity.SnapshotDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SnapshotDetailMapper {

    int insert(SnapshotDetail detail);

    List<SnapshotDetail> findBySnapshotId(@Param("snapshotId") Long snapshotId);
}
