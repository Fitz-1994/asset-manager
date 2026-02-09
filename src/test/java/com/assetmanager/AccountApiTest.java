package com.assetmanager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("账户接口测试")
class AccountApiTest extends BaseApiTest {

    @Nested
    @DisplayName("账户 CRUD")
    class AccountCrud {

        @Test
        @DisplayName("创建账户并出现在列表中")
        void createAndList() throws Exception {
            String token = registerAndGetToken("accuser", "pass");
            postWithToken("/api/accounts", token,
                "{\"name\":\"海通证券\",\"currency\":\"CNY\",\"type\":\"asset\",\"subtype\":\"investment\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("海通证券"))
                .andExpect(jsonPath("$.subtype").value("investment"));

            getWithToken("/api/accounts", token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("海通证券"))
                .andExpect(jsonPath("$[0].value").exists());
        }

        @Test
        @DisplayName("获取单个账户含当前 value")
        void getOne() throws Exception {
            String token = registerAndGetToken("getoneuser", "pass");
            String body = postWithToken("/api/accounts", token,
                "{\"name\":\"储蓄卡\",\"currency\":\"CNY\",\"type\":\"asset\",\"subtype\":\"savings\"}")
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
            long id = objectMapper.readTree(body).get("id").asLong();

            getWithToken("/api/accounts/" + id, token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("储蓄卡"))
                .andExpect(jsonPath("$.value").value(0.0));
        }

        @Test
        @DisplayName("更新账户名称")
        void update() throws Exception {
            String token = registerAndGetToken("updateuser", "pass");
            String body = postWithToken("/api/accounts", token,
                "{\"name\":\"旧名\",\"currency\":\"CNY\",\"type\":\"asset\",\"subtype\":\"other\"}")
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
            long id = objectMapper.readTree(body).get("id").asLong();

            patchWithToken("/api/accounts/" + id, token,
                "{\"name\":\"新名\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("新名"));

            getWithToken("/api/accounts/" + id, token)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("新名"));
        }

        @Test
        @DisplayName("删除账户")
        void deleteAccount() throws Exception {
            String token = registerAndGetToken("deluser", "pass");
            String body = postWithToken("/api/accounts", token,
                "{\"name\":\"待删\",\"currency\":\"CNY\",\"type\":\"asset\",\"subtype\":\"other\"}")
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
            long id = objectMapper.readTree(body).get("id").asLong();

            deleteWithToken("/api/accounts/" + id, token)
                .andExpect(status().isNoContent());

            getWithToken("/api/accounts/" + id, token)
                .andExpect(status().isNotFound());
        }
    }
}
