package com.assetmanager.controller;

import com.assetmanager.dto.ManualBalanceCreate;
import com.assetmanager.dto.ManualBalanceResponse;
import com.assetmanager.entity.Account;
import com.assetmanager.entity.ManualBalance;
import com.assetmanager.entity.User;
import com.assetmanager.mapper.AccountMapper;
import com.assetmanager.mapper.ManualBalanceMapper;
import com.assetmanager.service.SnapshotService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/accounts")
public class ManualBalanceController {

    private final AccountMapper accountMapper;
    private final ManualBalanceMapper manualBalanceMapper;
    private final SnapshotService snapshotService;

    public ManualBalanceController(AccountMapper accountMapper, ManualBalanceMapper manualBalanceMapper,
                                   SnapshotService snapshotService) {
        this.accountMapper = accountMapper;
        this.manualBalanceMapper = manualBalanceMapper;
        this.snapshotService = snapshotService;
    }

    @GetMapping("/{accountId}/balance")
    public ManualBalanceResponse getLatestBalance(@AuthenticationPrincipal User user, @PathVariable Long accountId) {
        getAccountForUser(accountId, user.getId());
        ManualBalance mb = manualBalanceMapper.findLatestByAccountId(accountId);
        return mb != null ? toResponse(mb) : null;
    }

    @PostMapping("/{accountId}/balance")
    @ResponseStatus(HttpStatus.CREATED)
    public ManualBalanceResponse setBalance(@AuthenticationPrincipal User user, @PathVariable Long accountId, @RequestBody ManualBalanceCreate body) {
        Account acc = getAccountForUser(accountId, user.getId());
        if ("investment".equals(acc.getSubtype()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use positions for investment accounts");
        ManualBalance mb = new ManualBalance();
        mb.setAccountId(accountId);
        mb.setAmount(body.getAmount() != null ? body.getAmount() : 0.0);
        mb.setRecordedAt(body.getRecorded_at() != null ? body.getRecorded_at() : Instant.now());
        mb.setCreatedAt(Instant.now());
        manualBalanceMapper.insert(mb);
        snapshotService.createManualSnapshot(user.getId());
        return toResponse(mb);
    }

    @GetMapping("/{accountId}/balance/history")
    public List<ManualBalanceResponse> balanceHistory(@AuthenticationPrincipal User user, @PathVariable Long accountId,
                                                      @RequestParam(defaultValue = "100") int limit) {
        getAccountForUser(accountId, user.getId());
        return manualBalanceMapper.findByAccountIdOrderByRecordedAtDesc(accountId, limit)
            .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private Account getAccountForUser(Long accountId, Long userId) {
        Account a = accountMapper.findById(accountId);
        if (a == null || !a.getUserId().equals(userId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
        return a;
    }

    private ManualBalanceResponse toResponse(ManualBalance m) {
        ManualBalanceResponse r = new ManualBalanceResponse();
        r.setId(m.getId());
        r.setAccount_id(m.getAccountId());
        r.setAmount(m.getAmount());
        r.setRecorded_at(m.getRecordedAt());
        r.setCreated_at(m.getCreatedAt());
        return r;
    }
}
