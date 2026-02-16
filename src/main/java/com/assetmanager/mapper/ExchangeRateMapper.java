package com.assetmanager.mapper;

import com.assetmanager.entity.ExchangeRate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ExchangeRateMapper {
    void insert(ExchangeRate rate);
    List<ExchangeRate> findAll();
    ExchangeRate findByFromAndTo(@Param("from") String from, @Param("to") String to);
    ExchangeRate findLatestByFromAndTo(@Param("from") String from, @Param("to") String to);
    void update(ExchangeRate rate);
}
