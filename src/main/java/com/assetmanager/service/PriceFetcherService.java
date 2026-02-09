package com.assetmanager.service;

import com.assetmanager.entity.InvestmentTarget;
import com.assetmanager.mapper.InvestmentTargetMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class PriceFetcherService {

    private static final Logger log = LoggerFactory.getLogger(PriceFetcherService.class);
    private final InvestmentTargetMapper targetMapper;
    private final RestTemplate restTemplate;

    public PriceFetcherService(InvestmentTargetMapper targetMapper) {
        this.targetMapper = targetMapper;
        this.restTemplate = new RestTemplate();
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
        headers.set("Referer", "https://finance.sina.com.cn/");
        headers.set("Accept", "*/*");
        return headers;
    }

    public Optional<Double> fetchPrice(String market, String code) {
        String symbol = toSinaSymbol(market, code);
        if (symbol == null || symbol.isEmpty()) return Optional.empty();
        String url = "https://hq.sinajs.cn/list=" + symbol;
        try {
            HttpEntity<String> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                String body = resp.getBody();
                int q = body.indexOf('"');
                if (q >= 0) {
                    int q2 = body.indexOf('"', q + 1);
                    if (q2 > q) {
                        String part = body.substring(q + 1, q2);
                        String[] cols = part.split(",");
                        if (cols.length >= 2) {
                            return Optional.of(Double.parseDouble(cols[1].trim()));
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("fetchPrice error, market={}, code={}", market, code, e);
        }
        return Optional.empty();
    }

    public static class NameAndPrice {
        private final String name;
        private final Double price;

        public NameAndPrice(String name, Double price) {
            this.name = name;
            this.price = price;
        }

        public String getName() { return name; }
        public Double getPrice() { return price; }
    }

    public Optional<NameAndPrice> fetchNameAndPrice(String market, String code) {
        String symbol = toSinaSymbol(market, code);
        if (symbol == null || symbol.isEmpty()) {
            log.debug("fetchNameAndPrice: empty symbol for market={}, code={}", market, code);
            return Optional.empty();
        }
        String url = "https://hq.sinajs.cn/list=" + symbol;
        log.debug("fetchNameAndPrice: requesting URL={}", url);
        try {
            HttpEntity<String> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                String body = resp.getBody();
                log.debug("fetchNameAndPrice: response body length={}, preview={}", body.length(), body.length() > 200 ? body.substring(0, 200) : body);
                int q = body.indexOf('"');
                if (q >= 0) {
                    int q2 = body.indexOf('"', q + 1);
                    if (q2 > q) {
                        String part = body.substring(q + 1, q2);
                        String[] cols = part.split(",");
                        log.debug("fetchNameAndPrice: parsed {} columns, first few: {}", cols.length, cols.length > 0 ? String.join(",", java.util.Arrays.copyOf(cols, Math.min(5, cols.length))) : "");
                        if (cols.length >= 2) {
                            String name = cols[0].trim();
                            Double price = null;
                            try {
                                price = Double.parseDouble(cols[1].trim());
                            } catch (NumberFormatException e) {
                                log.debug("fetchNameAndPrice: failed to parse price from '{}', trying column 3", cols[1]);
                                // 港股可能价格在第3列（索引2），尝试解析
                                if (cols.length >= 3) {
                                    try {
                                        price = Double.parseDouble(cols[2].trim());
                                    } catch (NumberFormatException ignored) { }
                                }
                            }
                            if (!name.isEmpty()) {
                                log.debug("fetchNameAndPrice: success, name={}, price={}", name, price);
                                return Optional.of(new NameAndPrice(name, price));
                            } else {
                                log.debug("fetchNameAndPrice: empty name");
                            }
                        } else {
                            log.debug("fetchNameAndPrice: insufficient columns, got {}", cols.length);
                        }
                    } else {
                        log.debug("fetchNameAndPrice: no closing quote found");
                    }
                } else {
                    log.debug("fetchNameAndPrice: no opening quote found in response");
                }
            } else {
                log.warn("fetchNameAndPrice: non-2xx status or empty body, status={}", resp.getStatusCode());
            }
        } catch (Exception e) {
            log.warn("fetchNameAndPrice error, market={}, code={}, symbol={}, error={}", market, code, symbol, e.getMessage(), e);
        }
        return Optional.empty();
    }

    private static String toSinaSymbol(String market, String code) {
        if (code == null) return "";
        String raw = code.trim().toUpperCase();
        if ("A_SHARE".equals(market)) {
            if (raw.startsWith("SH")) return "sh" + raw.substring(2);
            if (raw.startsWith("SZ")) return "sz" + raw.substring(2);
            char first = raw.isEmpty() ? 0 : raw.charAt(0);
            String prefix = (first == '6' || first == '5') ? "sh" : "sz";
            return prefix + raw;
        }
        if ("HK".equals(market)) {
            // 移除可能的 HK 前缀
            String codeOnly = raw.replaceFirst("^HK", "");
            // 港股代码需要补零到5位（如 700 -> 00700）
            try {
                // 尝试解析为数字，如果是纯数字则补零
                int num = Integer.parseInt(codeOnly);
                codeOnly = String.format("%05d", num);
            } catch (NumberFormatException e) {
                // 如果不是纯数字（如指数代码 HSI），保持原样
            }
            return "hk" + codeOnly;
        }
        if ("US".equals(market)) {
            // 美股格式: gb_<SYMBOL> (小写)
            return "gb_" + raw.toLowerCase();
        }
        return "";
    }

    @Transactional
    public Optional<Double> updateTargetPrice(Long targetId) {
        InvestmentTarget t = targetMapper.findById(targetId);
        if (t == null) return Optional.empty();
        Optional<NameAndPrice> nameAndPrice = fetchNameAndPrice(t.getMarket(), t.getCode());
        if (nameAndPrice.isPresent()) {
            NameAndPrice np = nameAndPrice.get();
            // 更新价格
            if (np.getPrice() != null) {
                t.setLastPrice(np.getPrice());
                t.setPriceUpdatedAt(Instant.now());
            }
            // 更新名称（如果查询到名称且当前名称为空或不同）
            if (np.getName() != null && !np.getName().isEmpty()) {
                // 如果当前名称为空，或者名称发生变化，则更新
                if (t.getName() == null || t.getName().isEmpty() || !t.getName().equals(np.getName())) {
                    t.setName(np.getName());
                }
            }
            targetMapper.update(t);
            return Optional.ofNullable(np.getPrice());
        }
        return Optional.empty();
    }

    @Transactional
    public int updateAllPrices(String market) {
        List<InvestmentTarget> targets = (market != null && !market.isEmpty())
            ? targetMapper.findByMarketOrderByCode(market)
            : targetMapper.findAll();
        int updated = 0;
        for (InvestmentTarget t : targets) {
            if (updateTargetPrice(t.getId()).isPresent()) updated++;
        }
        return updated;
    }
}
