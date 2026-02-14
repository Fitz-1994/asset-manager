package com.assetmanager.service;

import com.assetmanager.entity.Account;
import com.assetmanager.entity.AssetSnapshot;
import com.assetmanager.entity.SnapshotDetail;
import com.assetmanager.entity.User;
import com.assetmanager.mapper.AccountMapper;
import com.assetmanager.mapper.AssetSnapshotMapper;
import com.assetmanager.mapper.SnapshotDetailMapper;
import com.assetmanager.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class SnapshotService {

    private static final Logger log = LoggerFactory.getLogger(SnapshotService.class);
    
    private final AccountMapper accountMapper;
    private final AccountValueService accountValueService;
    private final AssetSnapshotMapper assetSnapshotMapper;
    private final SnapshotDetailMapper snapshotDetailMapper;
    private final UserMapper userMapper;

    public SnapshotService(AccountMapper accountMapper, AccountValueService accountValueService,
                           AssetSnapshotMapper assetSnapshotMapper, SnapshotDetailMapper snapshotDetailMapper,
                           UserMapper userMapper) {
        this.accountMapper = accountMapper;
        this.accountValueService = accountValueService;
        this.assetSnapshotMapper = assetSnapshotMapper;
        this.snapshotDetailMapper = snapshotDetailMapper;
        this.userMapper = userMapper;
    }

    /**
     * 为所有用户创建每日快照
     */
    @Transactional
    public void createDailySnapshotsForAllUsers() {
        List<User> users = userMapper.findAll();
        log.info("开始为 {} 个用户创建每日快照", users.size());
        for (User user : users) {
            try {
                createSnapshot(user.getId(), Instant.now(), "scheduled");
                log.info("用户 {} 的每日快照创建成功", user.getUsername());
            } catch (Exception e) {
                log.error("用户 {} 的每日快照创建失败: {}", user.getUsername(), e.getMessage());
            }
        }
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
