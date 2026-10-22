package com.example.ai_interview.security;

import com.example.ai_interview.common.ApiException;
import com.example.ai_interview.config.AppProperties;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 轻量 JWT（HS256）签发与校验。
 * 仅依赖 JDK 的 HMAC-SHA256 与 Jackson，token 结构：base64url(header).base64url(payload).base64url(signature)
 */
@Service
public class JwtService {

    private static final String HEADER_JSON = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
    private static final Base64.Encoder B64_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder B64_DECODER = Base64.getUrlDecoder();

    private final byte[] secret;
    private final Duration expiration;
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    public JwtService(AppProperties props) {
        String secret = props.getJwt().getSecret();
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("app.jwt.secret 必须至少 32 字节（HS256 要求）");
        }
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.expiration = Duration.ofHours(props.getJwt().getExpirationHours());
    }

    /** 签发 token */
    public String issue(Long userId, String email) {
        long now = Instant.now().getEpochSecond();
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("sub", email);
        claims.put("uid", userId);
        claims.put("iat", now);
        claims.put("exp", now + expiration.toSeconds());

        String header = b64(HEADER_JSON.getBytes(StandardCharsets.UTF_8));
        String payload = b64(jsonMapper.writeValueAsBytes(claims));
        String signingInput = header + "." + payload;
        String signature = b64(hmacSha256(signingInput.getBytes(StandardCharsets.UTF_8)));
        return signingInput + "." + signature;
    }

    /** 校验 token 并解析出用户；无效/过期抛 401 */
    public LoginUser parse(String token) {
        String[] parts = token == null ? new String[0] : token.split("\\.");
        if (parts.length != 3) {
            throw ApiException.unauthorized("无效的 token");
        }
        String signingInput = parts[0] + "." + parts[1];
        byte[] expectedSig = hmacSha256(signingInput.getBytes(StandardCharsets.UTF_8));
        byte[] actualSig;
        try {
            actualSig = B64_DECODER.decode(parts[2]);
        } catch (IllegalArgumentException e) {
            throw ApiException.unauthorized("无效的 token");
        }
        if (!MessageDigest.isEqual(expectedSig, actualSig)) {
            throw ApiException.unauthorized("token 签名校验失败");
        }

        JsonNode claims;
        try {
            claims = jsonMapper.readTree(B64_DECODER.decode(parts[1]));
        } catch (IllegalArgumentException e) {
            throw ApiException.unauthorized("无效的 token");
        }
        long exp = claims.path("exp").asLong(0);
        if (exp < Instant.now().getEpochSecond()) {
            throw ApiException.unauthorized("token 已过期，请重新登录");
        }
        long uid = claims.path("uid").asLong(0);
        String email = claims.path("sub").asString("");
        if (uid <= 0 || email.isEmpty()) {
            throw ApiException.unauthorized("无效的 token");
        }
        return new LoginUser(uid, email);
    }

    private byte[] hmacSha256(byte[] data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return mac.doFinal(data);
        } catch (Exception e) {
            throw new IllegalStateException("HMAC-SHA256 计算失败", e);
        }
    }

    private static String b64(byte[] data) {
        return B64_ENCODER.encodeToString(data);
    }
}
