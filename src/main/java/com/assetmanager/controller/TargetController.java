package com.assetmanager.controller;

import com.assetmanager.dto.InvestmentTargetCreate;
import com.assetmanager.dto.InvestmentTargetResponse;
import com.assetmanager.entity.InvestmentTarget;
import com.assetmanager.entity.User;
import com.assetmanager.mapper.InvestmentTargetMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/targets")
public class TargetController {

    private final InvestmentTargetMapper targetMapper;

    public TargetController(InvestmentTargetMapper targetMapper) {
        this.targetMapper = targetMapper;
    }

    @GetMapping
    public List<InvestmentTargetResponse> list(@AuthenticationPrincipal User user,
                                                @RequestParam(required = false) String market,
                                                @RequestParam(required = false) String q) {
        if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        List<InvestmentTarget> list = (market != null && !market.isEmpty())
            ? targetMapper.findByMarketOrderByCode(market)
            : targetMapper.findAll();
        if (q != null && !q.isEmpty()) {
            String lower = q.toLowerCase();
            list = list.stream()
                .filter(t -> (t.getName() != null && t.getName().toLowerCase().contains(lower))
                    || (t.getCode() != null && t.getCode().toLowerCase().contains(lower)))
                .collect(Collectors.toList());
        }
        return list.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InvestmentTargetResponse create(@AuthenticationPrincipal User user, @RequestBody InvestmentTargetCreate body) {
        if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        if (body.getCode() == null || body.getMarket() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "market and code required");
        if (targetMapper.findByMarketAndCode(body.getMarket(), body.getCode().trim()) != null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Target with this market+code already exists");
        InvestmentTarget t = new InvestmentTarget();
        t.setMarket(body.getMarket());
        t.setCode(body.getCode().trim());
        t.setName(body.getName() != null ? body.getName() : "");
        // 根据市场自动设置币种
        String currency = body.getCurrency();
        if (currency == null || currency.isEmpty()) {
            currency = getDefaultCurrencyByMarket(body.getMarket());
        }
        t.setCurrency(currency);
        t.setCreatedAt(Instant.now());
        targetMapper.insert(t);
        return toResponse(t);
    }

    @GetMapping("/{targetId}")
    public InvestmentTargetResponse get(@AuthenticationPrincipal User user, @PathVariable Long targetId) {
        if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        InvestmentTarget t = targetMapper.findById(targetId);
        if (t == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Target not found");
        return toResponse(t);
    }

    @PatchMapping("/{targetId}")
    public InvestmentTargetResponse update(@AuthenticationPrincipal User user, @PathVariable Long targetId, @RequestBody InvestmentTargetCreate body) {
        if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        InvestmentTarget t = targetMapper.findById(targetId);
        if (t == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Target not found");
        if (body.getName() != null) t.setName(body.getName());
        if (body.getCurrency() != null) t.setCurrency(body.getCurrency());
        targetMapper.update(t);
        return toResponse(t);
    }

    @DeleteMapping("/{targetId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal User user, @PathVariable Long targetId) {
        if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        if (targetMapper.findById(targetId) == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Target not found");
        targetMapper.deleteById(targetId);
    }

    private String getDefaultCurrencyByMarket(String market) {
        if (market == null) return "CNY";
        switch (market) {
            case "A_SHARE":
                return "CNY";
            case "HK":
                return "HKD";
            case "US":
                return "USD";
            default:
                return "CNY";
        }
    }

    private InvestmentTargetResponse toResponse(InvestmentTarget t) {
        InvestmentTargetResponse r = new InvestmentTargetResponse();
        r.setId(t.getId());
        r.setMarket(t.getMarket());
        r.setCode(t.getCode());
        r.setName(t.getName());
        r.setCurrency(t.getCurrency());
        r.setLast_price(t.getLastPrice());
        r.setPrice_updated_at(t.getPriceUpdatedAt());
        r.setCreated_at(t.getCreatedAt());
        return r;
    }
}
