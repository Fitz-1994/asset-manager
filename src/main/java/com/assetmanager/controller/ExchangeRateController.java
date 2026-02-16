package com.assetmanager.controller;

import com.assetmanager.entity.ExchangeRate;
import com.assetmanager.service.ExchangeRateService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exchange-rates")
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    public ExchangeRateController(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @GetMapping
    public List<ExchangeRate> list() {
        return exchangeRateService.getAllRates();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void setRate(@RequestBody Map<String, Object> body) {
        String from = (String) body.get("from_currency");
        String to = (String) body.get("to_currency");
        Object rateObj = body.get("rate");
        
        if (from == null || to == null || rateObj == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required fields");
        }
        
        double rate;
        if (rateObj instanceof Number) {
            rate = ((Number) rateObj).doubleValue();
        } else {
            rate = Double.parseDouble(rateObj.toString());
        }
        
        exchangeRateService.setRate(from, to, rate);
    }
}
