package com.assetmanager.controller;

import com.assetmanager.dto.PositionCreate;
import com.assetmanager.dto.PositionResponse;
import com.assetmanager.dto.PositionWithTarget;
import com.assetmanager.entity.Account;
import com.assetmanager.entity.InvestmentTarget;
import com.assetmanager.entity.Position;
import com.assetmanager.entity.User;
import com.assetmanager.mapper.AccountMapper;
import com.assetmanager.mapper.InvestmentTargetMapper;
import com.assetmanager.mapper.PositionMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/accounts")
public class PositionController {

    private final AccountMapper accountMapper;
    private final PositionMapper positionMapper;
    private final InvestmentTargetMapper investmentTargetMapper;

    public PositionController(AccountMapper accountMapper, PositionMapper positionMapper,
                             InvestmentTargetMapper investmentTargetMapper) {
        this.accountMapper = accountMapper;
        this.positionMapper = positionMapper;
        this.investmentTargetMapper = investmentTargetMapper;
    }

    @GetMapping("/{accountId}/positions")
    @Transactional(readOnly = true)
    public List<PositionWithTarget> listPositions(@AuthenticationPrincipal User user, @PathVariable Long accountId) {
        Account acc = getAccountForUser(accountId, user.getId());
        if (!"investment".equals(acc.getSubtype()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account is not an investment account");
        return positionMapper.findByAccountId(accountId).stream()
            .map(this::toWithTarget)
            .collect(Collectors.toList());
    }

    @PostMapping("/{accountId}/positions")
    @ResponseStatus(HttpStatus.CREATED)
    public PositionResponse addPosition(@AuthenticationPrincipal User user, @PathVariable Long accountId, @RequestBody PositionCreate body) {
        Account acc = getAccountForUser(accountId, user.getId());
        if (!"investment".equals(acc.getSubtype()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account is not an investment account");
        
        Long targetId = body.getTarget_id();
        
        // If target_id is not provided but target info is provided, create the target first
        if (targetId == null && body.getTarget_code() != null && !body.getTarget_code().isEmpty()) {
            // Check if target already exists
            InvestmentTarget existing = investmentTargetMapper.findByMarketAndCode(
                body.getTarget_market(), body.getTarget_code().trim());
            if (existing != null) {
                targetId = existing.getId();
            } else {
                // Create new target
                InvestmentTarget newTarget = new InvestmentTarget();
                newTarget.setMarket(body.getTarget_market());
                newTarget.setCode(body.getTarget_code().trim());
                newTarget.setName(body.getTarget_name() != null ? body.getTarget_name() : "");
                // Set default currency based on market
                String currency = body.getTarget_currency();
                if (currency == null || currency.isEmpty()) {
                    currency = getDefaultCurrencyByMarket(body.getTarget_market());
                }
                newTarget.setCurrency(currency);
                newTarget.setCreatedAt(Instant.now());
                investmentTargetMapper.insert(newTarget);
                targetId = newTarget.getId();
            }
        }
        
        if (targetId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请选择或创建标的");
        }
        
        Position pos = positionMapper.findByAccountIdAndTargetId(accountId, targetId);
        if (pos != null) {
            pos.setQuantity(body.getQuantity() != null ? body.getQuantity() : 0.0);
            pos.setUpdatedAt(Instant.now());
            positionMapper.update(pos);
        } else {
            pos = new Position();
            pos.setAccountId(accountId);
            pos.setTargetId(targetId);
            pos.setQuantity(body.getQuantity() != null ? body.getQuantity() : 0.0);
            pos.setUpdatedAt(Instant.now());
            positionMapper.insert(pos);
        }
        return toResponse(pos);
    }

    @PatchMapping("/{accountId}/positions/{positionId}")
    public PositionResponse updatePosition(@AuthenticationPrincipal User user, @PathVariable Long accountId,
                                           @PathVariable Long positionId, @RequestBody PositionCreate body) {
        getAccountForUser(accountId, user.getId());
        Position pos = positionMapper.findById(positionId);
        if (pos == null || !pos.getAccountId().equals(accountId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Position not found");
        if (body.getQuantity() != null) pos.setQuantity(body.getQuantity());
        pos.setUpdatedAt(Instant.now());
        positionMapper.update(pos);
        return toResponse(pos);
    }

    @DeleteMapping("/{accountId}/positions/{positionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePosition(@AuthenticationPrincipal User user, @PathVariable Long accountId, @PathVariable Long positionId) {
        getAccountForUser(accountId, user.getId());
        Position pos = positionMapper.findById(positionId);
        if (pos == null || !pos.getAccountId().equals(accountId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Position not found");
        positionMapper.deleteById(pos.getId());
    }

    private Account getAccountForUser(Long accountId, Long userId) {
        Account a = accountMapper.findById(accountId);
        if (a == null || !a.getUserId().equals(userId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
        return a;
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

    private PositionResponse toResponse(Position p) {
        PositionResponse r = new PositionResponse();
        r.setId(p.getId());
        r.setAccount_id(p.getAccountId());
        r.setTarget_id(p.getTargetId());
        r.setQuantity(p.getQuantity());
        r.setUpdated_at(p.getUpdatedAt());
        return r;
    }

    private PositionWithTarget toWithTarget(Position p) {
        PositionWithTarget w = new PositionWithTarget();
        w.setId(p.getId());
        w.setAccount_id(p.getAccountId());
        w.setTarget_id(p.getTargetId());
        w.setQuantity(p.getQuantity());
        w.setUpdated_at(p.getUpdatedAt());
        InvestmentTarget t = investmentTargetMapper.findById(p.getTargetId());
        if (t != null) {
            w.setTarget_code(t.getCode());
            w.setTarget_name(t.getName());
            w.setTarget_market(t.getMarket());
            w.setTarget_currency(t.getCurrency());
            w.setLast_price(t.getLastPrice());
            double q = p.getQuantity() != null ? p.getQuantity() : 0;
            double price = t.getLastPrice() != null ? t.getLastPrice() : 0;
            w.setMarket_value(Math.round(q * price * 100.0) / 100.0);
        }
        return w;
    }
}
