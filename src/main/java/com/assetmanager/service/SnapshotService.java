package com.assetmanager.service;

import com.assetmanager.entity.Account;
import com.assetmanager.entity.AssetSnapshot;
import com.assetmanager.entity.SnapshotDetail;
import com.assetmanager.mapper.AccountMapper;
import com.assetmanager.mapper.AssetSnapshotMapper;
import com.assetmanager.mapper.SnapshotDetailMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class SnapshotService {

    private final AccountMapper accountMapper;
    private final AccountValueService accountValueService;
    private final AssetSnapshotMapper assetSnapshotMapper;
    private final SnapshotDetailMapper snapshotDetailMapper;

    public SnapshotService(AccountMapper accountMapper, AccountValueService accountValueService,
                           AssetSnapshotMapper assetSnapshotMapper, SnapshotDetailMapper snapshotDetailMapper) {
        this.accountMapper = accountMapper;
        this.accountValueService = accountValueService;
        this.assetSnapshotMapper = assetSnapshotMapper;
        this.snapshotDetailMapper = snapshotDetailMapper;
    }

    @Transactional
    public void createManualSnapshot(Long userId) {
        createSnapshot(userId, Instant.now(), "manual");
    }

    @Transactional
    public AssetSnapshot createSnapshot(Long userId, Instant snapshotAt, String triggerType) {
        List<Account> accounts = accountMapper.findByUserIdOrderById(userId);
        double total = 0;
        List<SnapshotDetail> details = new ArrayList<>();
        for (Account acc : accounts) {
            double value = accountValueService.getAccountValue(acc);
            total += value;
            SnapshotDetail d = new SnapshotDetail();
            d.setAccountId(acc.getId());
            d.setValue(value);
            d.setCurrency(acc.getCurrency());
            details.add(d);
        }
        total = Math.round(total * 100.0) / 100.0;

        AssetSnapshot snap = new AssetSnapshot();
        snap.setUserId(userId);
        snap.setSnapshotAt(snapshotAt);
        snap.setTriggerType(triggerType);
        snap.setTotalValueCny(total);
        snap.setCreatedAt(Instant.now());
        assetSnapshotMapper.insert(snap);

        for (SnapshotDetail d : details) {
            d.setSnapshotId(snap.getId());
            snapshotDetailMapper.insert(d);
        }
        return snap;
    }
}
