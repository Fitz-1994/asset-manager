package com.assetmanager.controller;

import com.assetmanager.entity.User;
import com.assetmanager.service.PriceFetcherService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/price")
public class PriceController {

    private final PriceFetcherService priceFetcherService;

    public PriceController(PriceFetcherService priceFetcherService) {
        this.priceFetcherService = priceFetcherService;
    }

    @PostMapping("/target/{targetId}")
    public Map<String, Object> refreshTargetPrice(@AuthenticationPrincipal User user, @PathVariable Long targetId) {
        if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return priceFetcherService.updateTargetPrice(targetId)
            .map(price -> Map.<String, Object>of("target_id", targetId, "price", price))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Price could not be updated"));
    }

    @PostMapping("/refresh-all")
    public Map<String, Integer> refreshAll(@AuthenticationPrincipal User user, @RequestParam(required = false) String market) {
        if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        int updated = priceFetcherService.updateAllPrices(market);
        return Map.of("updated_count", updated);
    }

    @GetMapping("/lookup")
    public Map<String, Object> lookup(@AuthenticationPrincipal User user,
                                       @RequestParam String market,
                                       @RequestParam String code) {
        if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return priceFetcherService.fetchNameAndPrice(market, code)
            .map(np -> Map.<String, Object>of("name", np.getName(), "price", np.getPrice() != null ? np.getPrice() : ""))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "无法查询到该标的信息"));
    }
}
