/**
 * Author：周朔鹏
 * 编制时间：2026-08-01
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */
package com.course.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GatewayJwtVerifier {

    private final ResourceLoader resourceLoader;

    @Value("${euipd.security.jwt.public-key-location:classpath:security/jwt-public.pem}")
    private String publicKeyLocation;

    @Value("${euipd.security.jwt.issuer:euipd-auth}")
    private String issuer;

    @Value("${euipd.security.jwt.clock-skew-seconds:30}")
    private long clockSkewSeconds;

    private RSAPublicKey publicKey;

    public GatewayJwtVerifier(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void init() throws Exception {
        Resource res = resourceLoader.getResource(publicKeyLocation);
        String pem;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(res.getInputStream(), StandardCharsets.UTF_8))) {
            pem = reader.lines().collect(Collectors.joining("\n"));
        }
        String normalized = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(normalized);
        publicKey = (RSAPublicKey) KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(decoded));
    }

    public Claims parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .requireIssuer(issuer)
                .setAllowedClockSkewSeconds(clockSkewSeconds)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    @SuppressWarnings("unchecked")
    public static List<String> scopes(Claims claims) {
        Object scp = claims.get("scp");
        if (scp instanceof List) {
            List<String> out = new ArrayList<>();
            for (Object o : (List<?>) scp) {
                if (o != null) {
                    out.add(String.valueOf(o));
                }
            }
            return out;
        }
        if (scp == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(String.valueOf(scp));
    }

    public static String asString(Object v) {
        return v == null ? null : String.valueOf(v);
    }
}
