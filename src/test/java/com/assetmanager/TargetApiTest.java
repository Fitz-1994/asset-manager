package com.assetmanager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("投资标的接口测试")
class TargetApiTest extends BaseApiTest {

    @Test
    @DisplayName("创建标的并出现在列表中")
    void createAndList() throws Exception {
        String token = registerAndGetToken("targetuser", "pass");
        postWithToken("/api/targets", token,
            "{\"market\":\"A_SHARE\",\"code\":\"600519\",\"name\":\"贵州茅台\",\"currency\":\"CNY\"}")
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.market").value("A_SHARE"))
            .andExpect(jsonPath("$.code").value("600519"))
            .andExpect(jsonPath("$.name").value("贵州茅台"));

        getWithToken("/api/targets", token)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.code=='600519')]").isArray())
            .andExpect(jsonPath("$[?(@.code=='600519')].name").value(org.hamcrest.Matchers.hasItem("贵州茅台")));
    }

    @Test
    @DisplayName("同一 market+code 重复创建返回 400")
    void create_duplicateMarketCode_returns400() throws Exception {
        String token = registerAndGetToken("dupuser", "pass");
        postWithToken("/api/targets", token,
            "{\"market\":\"HK\",\"code\":\"00700\",\"name\":\"腾讯\",\"currency\":\"HKD\"}")
            .andExpect(status().isCreated());
        postWithToken("/api/targets", token,
            "{\"market\":\"HK\",\"code\":\"00700\",\"name\":\"腾讯控股\",\"currency\":\"HKD\"}")
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("按 market 筛选标的")
    void list_byMarket() throws Exception {
        String token = registerAndGetToken("filteruser", "pass");
        postWithToken("/api/targets", token,
            "{\"market\":\"A_SHARE\",\"code\":\"000001\",\"name\":\"平安银行\",\"currency\":\"CNY\"}");
        postWithToken("/api/targets", token,
            "{\"market\":\"HK\",\"code\":\"09988\",\"name\":\"阿里\",\"currency\":\"HKD\"}");

        getWithToken("/api/targets?market=A_SHARE", token)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.code=='000001')]").exists())
            .andExpect(jsonPath("$[?(@.code=='09988')]").isEmpty());
    }
}
