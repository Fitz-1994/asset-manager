package com.assetmanager.config;

import com.assetmanager.service.SnapshotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class SchedulerConfig {

    private static final Logger log = LoggerFactory.getLogger(SchedulerConfig.class);
    private final SnapshotService snapshotService;

    public SchedulerConfig(SnapshotService snapshotService) {
        this.snapshotService = snapshotService;
    }

    /**
     * 每天0点自动创建资产快照
     * 注意：服务器时区为UTC，实际执行时间为UTC 0点（即北京时间早上8点）
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Shanghai")
    public void createDailySnapshot() {
        log.info("开始执行每日资产快照任务...");
        try {
            // 获取所有用户ID并为每个用户创建快照
            // 这里简化处理：直接调用创建快照方法
            snapshotService.createDailySnapshotsForAllUsers();
            log.info("每日资产快照任务完成");
        } catch (Exception e) {
            log.error("每日资产快照任务执行失败", e);
        }
    }
}
