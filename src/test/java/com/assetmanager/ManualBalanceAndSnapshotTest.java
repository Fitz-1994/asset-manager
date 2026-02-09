package com.assetmanager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("手动余额与快照测试")
class ManualBalanceAndSnapshotTest extends BaseApiTest {

    @Test
    @DisplayName("储蓄账户设置余额后触发快照，列表与 series 有数据")
    void setBalance_createsSnapshot_seriesAvailable() throws Exception {
        String token = registerAndGetToken("snapuser", "pass");
        // 1. 创建储蓄账户
        String accBody = postWithToken("/api/accounts", token,
            "{\"name\":\"银行卡\",\"currency\":\"CNY\",\"type\":\"asset\",\"subtype\":\"savings\"}")
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        long accountId = objectMapper.readTree(accBody).get("id").asLong();

        // 2. 设置余额（会触发 manual 快照）
        postWithToken("/api/accounts/" + accountId + "/balance", token,
            "{\"amount\":10000.5}")
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.amount").value(10000.5));

        // 3. 账户列表应显示该账户 value
        getWithToken("/api/accounts", token)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].value").value(10000.5));

        // 4. 快照列表有一条 manual 记录
        getWithToken("/api/snapshots?limit=10", token)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].trigger_type").value("manual"))
            .andExpect(jsonPath("$[0].total_value_cny").value(10000.5));

        // 5. 走势 series 有数据
        getWithToken("/api/snapshots/series?days=365", token)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].value").value(10000.5))
            .andExpect(jsonPath("$[0].return_pct").value(0.0));
    }

    @Test
    @DisplayName("获取余额历史")
    void balanceHistory() throws Exception {
        String token = registerAndGetToken("histuser", "pass");
        String accBody = postWithToken("/api/accounts", token,
            "{\"name\":\"现金\",\"currency\":\"CNY\",\"type\":\"asset\",\"subtype\":\"savings\"}")
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        long accountId = objectMapper.readTree(accBody).get("id").asLong();

        postWithToken("/api/accounts/" + accountId + "/balance", token, "{\"amount\":100}");
        postWithToken("/api/accounts/" + accountId + "/balance", token, "{\"amount\":200}");

        getWithToken("/api/accounts/" + accountId + "/balance/history?limit=5", token)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].amount").value(200.0))
            .andExpect(jsonPath("$[1].amount").value(100.0));
    }
}
