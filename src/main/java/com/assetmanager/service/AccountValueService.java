package com.assetmanager.service;

import com.assetmanager.entity.Account;
import com.assetmanager.entity.InvestmentTarget;
import com.assetmanager.entity.ManualBalance;
import com.assetmanager.entity.Position;
import com.assetmanager.mapper.InvestmentTargetMapper;
import com.assetmanager.mapper.ManualBalanceMapper;
import com.assetmanager.mapper.PositionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AccountValueService {

    private final PositionMapper positionMapper;
    private final InvestmentTargetMapper investmentTargetMapper;
    private final ManualBalanceMapper manualBalanceMapper;
    private final ExchangeRateService exchangeRateService;

    public AccountValueService(PositionMapper positionMapper, InvestmentTargetMapper investmentTargetMapper,
                               ManualBalanceMapper manualBalanceMapper, ExchangeRateService exchangeRateService) {
        this.positionMapper = positionMapper;
        this.investmentTargetMapper = investmentTargetMapper;
        this.manualBalanceMapper = manualBalanceMapper;
        this.exchangeRateService = exchangeRateService;
    }

    @Transactional(readOnly = true)
    public double getAccountValue(Account account) {
        // Default to CNY for conversion
        String targetCurrency = account.getCurrency() != null ? account.getCurrency() : "CNY";
        
        if ("investment".equals(account.getSubtype())) {
            List<Position> positions = positionMapper.findByAccountId(account.getId());
            double total = 0;
            for (Position p : positions) {
                InvestmentTarget t = investmentTargetMapper.findById(p.getTargetId());
                double price = t != null && t.getLastPrice() != null ? t.getLastPrice() : 0.0;
                double positionValue = (p.getQuantity() != null ? p.getQuantity() : 0) * price;
                
                // Convert to account's currency
                String positionCurrency = t != null ? t.getCurrency() : "CNY";
                positionValue = exchangeRateService.convert(positionValue, positionCurrency, targetCurrency);
                
                total += positionValue;
            }
            return Math.round(total * 100.0) / 100.0;
        }
        ManualBalance mb = manualBalanceMapper.findLatestByAccountId(account.getId());
        if (mb == null) return 0.0;
        
        // Convert to account's currency
        double amount = exchangeRateService.convert(mb.getAmount(), mb.getCurrency(), targetCurrency);
        return Math.round(amount * 100.0) / 100.0;
    }
}
