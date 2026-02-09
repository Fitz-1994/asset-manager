package com.assetmanager.controller;

import com.assetmanager.dto.AccountCreate;
import com.assetmanager.dto.AccountResponse;
import com.assetmanager.dto.AccountWithValue;
import com.assetmanager.entity.Account;
import com.assetmanager.entity.User;
import com.assetmanager.mapper.AccountMapper;
import com.assetmanager.service.AccountValueService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountMapper accountMapper;
    private final AccountValueService accountValueService;

    public AccountController(AccountMapper accountMapper, AccountValueService accountValueService) {
        this.accountMapper = accountMapper;
        this.accountValueService = accountValueService;
    }

    @GetMapping
    public List<AccountWithValue> list(@AuthenticationPrincipal User user) {
        if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return accountMapper.findByUserIdOrderById(user.getId()).stream()
            .map(this::toWithValue)
            .collect(Collectors.toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse create(@AuthenticationPrincipal User user, @RequestBody AccountCreate body) {
        if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        Account acc = new Account();
        acc.setUserId(user.getId());
        acc.setName(body.getName());
        acc.setCurrency(body.getCurrency());
        acc.setType(body.getType());
        acc.setSubtype(body.getSubtype());
        acc.setCreatedAt(Instant.now());
        accountMapper.insert(acc);
        return toResponse(acc);
    }

    @GetMapping("/{accountId}")
    public AccountWithValue get(@AuthenticationPrincipal User user, @PathVariable Long accountId) {
        Account acc = getAccountForUser(accountId, user.getId());
        return toWithValue(acc);
    }

    @PatchMapping("/{accountId}")
    public AccountResponse update(@AuthenticationPrincipal User user, @PathVariable Long accountId, @RequestBody AccountCreate body) {
        Account acc = getAccountForUser(accountId, user.getId());
        if (body.getName() != null) acc.setName(body.getName());
        if (body.getCurrency() != null) acc.setCurrency(body.getCurrency());
        if (body.getType() != null) acc.setType(body.getType());
        if (body.getSubtype() != null) acc.setSubtype(body.getSubtype());
        accountMapper.update(acc);
        return toResponse(acc);
    }

    @DeleteMapping("/{accountId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal User user, @PathVariable Long accountId) {
        Account acc = getAccountForUser(accountId, user.getId());
        accountMapper.deleteById(acc.getId());
    }

    private Account getAccountForUser(Long accountId, Long userId) {
        Account a = accountMapper.findById(accountId);
        if (a == null || !a.getUserId().equals(userId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
        return a;
    }

    private AccountResponse toResponse(Account a) {
        AccountResponse r = new AccountResponse();
        r.setId(a.getId());
        r.setUser_id(a.getUserId());
        r.setName(a.getName());
        r.setCurrency(a.getCurrency());
        r.setType(a.getType());
        r.setSubtype(a.getSubtype());
        r.setCreated_at(a.getCreatedAt());
        return r;
    }

    private AccountWithValue toWithValue(Account a) {
        AccountWithValue w = new AccountWithValue();
        w.setId(a.getId());
        w.setUser_id(a.getUserId());
        w.setName(a.getName());
        w.setCurrency(a.getCurrency());
        w.setType(a.getType());
        w.setSubtype(a.getSubtype());
        w.setCreated_at(a.getCreatedAt());
        w.setValue(accountValueService.getAccountValue(a));
        return w;
    }
}
