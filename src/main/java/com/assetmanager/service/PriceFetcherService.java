package com.assetmanager.service;

import com.assetmanager.entity.InvestmentTarget;
import com.assetmanager.mapper.InvestmentTargetMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        headers.set("Referer", "https://finance.qq.com/");
        headers.set("Accept", "*/*");
        return headers;
    }

    public Optional<Double> fetchPrice(String market, String code) {
        String symbol = toTencentSymbol(market, code);
        if (symbol == null || symbol.isEmpty()) return Optional.empty();
        String url = "http://qt.gtimg.cn/q=" + symbol;
        try {
            HttpEntity<String> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<byte[]> resp = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                // 腾讯返回GBK编码，先用GBK解码
                String body = new String(resp.getBody(), StandardCharsets.UTF_8);
                return parsePrice(body);
            }
        } catch (Exception e) {
            log.debug("fetchPrice error, market={}, code={}", market, code, e);
        }
        return Optional.empty();
    }

    private Optional<Double> parsePrice(String body) {
        try {
            int eq = body.indexOf('=');
            if (eq >= 0) {
                String data = body.substring(eq + 1).trim();
                if (data.startsWith("\"") && data.endsWith("\"")) {
                    data = data.substring(1, data.length() - 1);
                }
                String[] cols = data.split("~");
                if (cols.length >= 5) {
                    return Optional.of(Double.parseDouble(cols[3].trim()));
                }
            }
        } catch (Exception e) {
            log.debug("parsePrice error: {}", e.getMessage());
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
        String symbol = toTencentSymbol(market, code);
        if (symbol == null || symbol.isEmpty()) {
            log.debug("fetchNameAndPrice: empty symbol for market={}, code={}", market, code);
            return Optional.empty();
        }
        String url = "http://qt.gtimg.cn/q=" + symbol;
        log.debug("fetchNameAndPrice: requesting URL={}", url);
        try {
            HttpEntity<String> entity = new HttpEntity<>(createHeaders());
            // 使用byte[]获取原始响应
            ResponseEntity<byte[]> resp = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                byte[] rawBody = resp.getBody();
                log.debug("fetchNameAndPrice: raw bytes length={}", rawBody.length);
                
                // 尝试用GBK解码
                String body;
                try {
                    body = new String(rawBody, "GBK");
                } catch (Exception e) {
                    body = new String(rawBody, StandardCharsets.UTF_8);
                }
                log.debug("fetchNameAndPrice: decoded body preview: {}", body.substring(0, Math.min(100, body.length())));
                
                return parseNameAndPrice(body);
            } else {
                log.warn("fetchNameAndPrice: non-2xx status or empty body, status={}", resp.getStatusCode());
            }
        } catch (Exception e) {
            log.warn("fetchNameAndPrice error, market={}, code={}, symbol={}, error={}", market, code, symbol, e.getMessage(), e);
        }
        return Optional.empty();
    }

    private Optional<NameAndPrice> parseNameAndPrice(String body) {
        try {
            // 腾讯API返回格式: v_symbol="0~name~code~...~price~..."
            int eq = body.indexOf('=');
            if (eq >= 0) {
                String data = body.substring(eq + 1).trim();
                // 去掉首尾引号
                if (data.startsWith("\"") && data.endsWith("\"")) {
                    data = data.substring(1, data.length() - 1);
                }
                String[] cols = data.split("~");
                log.debug("fetchNameAndPrice: parsed {} columns", cols.length);
                
                if (cols.length >= 4) {
                    // 第一列是名称，第三列是当前价格
                    String name = cols[1].trim();
                    
                    Double price = null;
                    try {
                        price = Double.parseDouble(cols[3].trim());
                    } catch (NumberFormatException e) {
                        log.debug("Failed to parse price from '{}'", cols[3]);
                    }
                    
                    log.debug("fetchNameAndPrice: name={}, price={}", name, price);
                    
                    if (!name.isEmpty()) {
                        return Optional.of(new NameAndPrice(name, price));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("parseNameAndPrice error: {}", e.getMessage(), e);
        }
        return Optional.empty();
    }

    private static String toTencentSymbol(String market, String code) {
        if (code == null) return "";
        String raw = code.trim().toUpperCase();
        
        if ("A_SHARE".equals(market)) {
            String codeOnly = raw.replaceFirst("^SH", "").replaceFirst("^SZ", "");
            if (raw.startsWith("SH") || codeOnly.startsWith("6") || codeOnly.startsWith("5")) {
                return "sh" + codeOnly;
            } else {
                return "sz" + codeOnly;
            }
        }
        if ("HK".equals(market)) {
            String codeOnly = raw.replaceFirst("^HK", "");
            try {
                int num = Integer.parseInt(codeOnly);
                codeOnly = String.format("%05d", num);
            } catch (NumberFormatException e) {
                // ignore
            }
            return "hk" + codeOnly;
        }
        if ("US".equals(market)) {
            String codeOnly = raw.replaceFirst("^US", "").toUpperCase();
            return "us" + codeOnly;
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
            if (np.getPrice() != null) {
                t.setLastPrice(np.getPrice());
                t.setPriceUpdatedAt(Instant.now());
            }
            if (np.getName() != null && !np.getName().isEmpty()) {
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
