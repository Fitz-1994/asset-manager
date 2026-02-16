package com.assetmanager.service;

import com.assetmanager.entity.ExchangeRate;
import com.assetmanager.mapper.ExchangeRateMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExchangeRateService {

    private final ExchangeRateMapper exchangeRateMapper;

    public ExchangeRateService(ExchangeRateMapper exchangeRateMapper) {
        this.exchangeRateMapper = exchangeRateMapper;
    }

    @Transactional(readOnly = true)
    public double convert(double amount, String fromCurrency, String toCurrency) {
        if (fromCurrency == null || toCurrency == null) return amount;
        if (fromCurrency.equals(toCurrency)) return amount;
        
        // Try to get rate from database
        ExchangeRate rate = exchangeRateMapper.findLatestByFromAndTo(fromCurrency, toCurrency);
        if (rate != null && rate.getRate() != null) {
            return amount * rate.getRate();
        }
        
        // If no rate found, try reverse
        rate = exchangeRateMapper.findLatestByFromAndTo(toCurrency, fromCurrency);
        if (rate != null && rate.getRate() != null && rate.getRate() != 0) {
            return amount / rate.getRate();
        }
        
        // Default fallback rates (hardcoded)
        Double defaultRate = getDefaultRate(fromCurrency, toCurrency);
        if (defaultRate != null) {
            return amount * defaultRate;
        }
        
        // If still no rate, return original amount
        return amount;
    }

    private Double getDefaultRate(String from, String to) {
        Map<String, Double> ratesToCNY = new HashMap<>();
        ratesToCNY.put("CNY", 1.0);
        ratesToCNY.put("USD", 7.2);
        ratesToCNY.put("HKD", 0.92);
        ratesToCNY.put("EUR", 7.8);
        ratesToCNY.put("GBP", 9.1);
        ratesToCNY.put("JPY", 0.048);
        
        if (from.equals(to)) return 1.0;
        
        Double fromRate = ratesToCNY.get(from);
        Double toRate = ratesToCNY.get(to);
        
        if (fromRate != null && toRate != null) {
            return toRate / fromRate;
        }
        return null;
    }

    @Transactional
    public void setRate(String fromCurrency, String toCurrency, double rate) {
        ExchangeRate existing = exchangeRateMapper.findByFromAndTo(fromCurrency, toCurrency);
        ExchangeRate exchangeRate = new ExchangeRate();
        exchangeRate.setFromCurrency(fromCurrency);
        exchangeRate.setToCurrency(toCurrency);
        exchangeRate.setRate(rate);
        exchangeRate.setRecordedAt(Instant.now());
        
        if (existing != null) {
            exchangeRate.setId(existing.getId());
            exchangeRateMapper.update(exchangeRate);
        } else {
            exchangeRateMapper.insert(exchangeRate);
        }
    }

    @Transactional(readOnly = true)
    public List<ExchangeRate> getAllRates() {
        return exchangeRateMapper.findAll();
    }
}
