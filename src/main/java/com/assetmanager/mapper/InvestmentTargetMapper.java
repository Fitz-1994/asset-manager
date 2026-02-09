package com.assetmanager.mapper;

import com.assetmanager.entity.InvestmentTarget;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InvestmentTargetMapper {

    int insert(InvestmentTarget target);

    int update(InvestmentTarget target);

    int deleteById(Long id);

    InvestmentTarget findById(Long id);

    InvestmentTarget findByMarketAndCode(@Param("market") String market, @Param("code") String code);

    List<InvestmentTarget> findByMarketOrderByCode(@Param("market") String market);

    List<InvestmentTarget> findAll();
}
