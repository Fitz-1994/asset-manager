package com.assetmanager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("持仓接口测试")
class PositionApiTest extends BaseApiTest {

    @Test
    @DisplayName("投资账户可添加持仓并列出")
    void addPosition_andList() throws Exception {
        String token = registerAndGetToken("posuser", "pass");
        // 先建标的（使用唯一 code 避免与其他测试冲突）
        String code = "60" + String.format("%04d", System.currentTimeMillis() % 10000);
        String tBody = postWithToken("/api/targets", token,
            "{\"market\":\"A_SHARE\",\"code\":\"" + code + "\",\"name\":\"茅台\",\"currency\":\"CNY\"}")
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        long targetId = objectMapper.readTree(tBody).get("id").asLong();
        // 再建投资账户
        String aBody = postWithToken("/api/accounts", token,
            "{\"name\":\"证券户\",\"currency\":\"CNY\",\"type\":\"asset\",\"subtype\":\"investment\"}")
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        long accountId = objectMapper.readTree(aBody).get("id").asLong();

        postWithToken("/api/accounts/" + accountId + "/positions", token,
            "{\"target_id\":" + targetId + ",\"quantity\":100}")
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.quantity").value(100.0))
            .andExpect(jsonPath("$.target_id").value(targetId));

        getWithToken("/api/accounts/" + accountId + "/positions", token)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].target_code").value(code))
            .andExpect(jsonPath("$[0].quantity").value(100.0));
    }

    @Test
    @DisplayName("非投资账户不能添加持仓")
    void addPosition_onSavingsAccount_returns400() throws Exception {
        String token = registerAndGetToken("noposuser", "pass");
        String code = "99" + String.format("%04d", System.currentTimeMillis() % 10000);
        String tBody = postWithToken("/api/targets", token,
            "{\"market\":\"HK\",\"code\":\"" + code + "\",\"name\":\"腾讯\",\"currency\":\"HKD\"}")
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        long targetId = objectMapper.readTree(tBody).get("id").asLong();
        String aBody = postWithToken("/api/accounts", token,
            "{\"name\":\"储蓄\",\"currency\":\"CNY\",\"type\":\"asset\",\"subtype\":\"savings\"}")
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        long accountId = objectMapper.readTree(aBody).get("id").asLong();

        postWithToken("/api/accounts/" + accountId + "/positions", token,
            "{\"target_id\":" + targetId + ",\"quantity\":1}")
            .andExpect(status().isBadRequest());
    }
}
