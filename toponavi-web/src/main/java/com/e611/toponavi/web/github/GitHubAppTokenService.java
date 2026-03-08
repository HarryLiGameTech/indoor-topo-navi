package com.e611.toponavi.web.github;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import okhttp3.*;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.StringReader;
import java.security.PrivateKey;
import java.time.Instant;
import java.util.Date;

/**
 * Manages GitHub App Installation Access Tokens (IAT).
 * The IAT is cached and refreshed when close to expiry (< 5 minutes remaining).
 *
 * Flow:
 *  1. Build a signed JWT from the App private key
 *  2. Exchange it for an Installation Access Token via GitHub API
 *  3. Cache the IAT; refresh when it is about to expire
 */
@Service
public class GitHubAppTokenService {

    private static final Logger log = LoggerFactory.getLogger(GitHubAppTokenService.class);
    private static final MediaType JSON_MEDIA = MediaType.get("application/json");

    @Value("${platform.github.app-id}")
    private String appId;

    @Value("${platform.github.private-key}")
    private String privateKeyPem;

    @Value("${platform.github.installation-id}")
    private String installationId;

    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** Cached token value */
    private volatile String cachedToken;
    /** Expiry time of the cached token */
    private volatile Instant tokenExpiry = Instant.EPOCH;

    /**
     * Returns a valid Installation Access Token, refreshing if needed.
     */
    public synchronized String getInstallationToken() {
        if (cachedToken == null || Instant.now().isAfter(tokenExpiry.minusSeconds(300))) {
            cachedToken = fetchInstallationToken();
        }
        return cachedToken;
    }

    private String fetchInstallationToken() {
        try {
            String jwt = buildAppJwt();

            Request request = new Request.Builder()
                    .url("https://api.github.com/app/installations/" + installationId + "/access_tokens")
                    .post(RequestBody.create("", JSON_MEDIA))
                    .header("Authorization", "Bearer " + jwt)
                    .header("Accept", "application/vnd.github+json")
                    .header("X-GitHub-Api-Version", "2022-11-28")
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                String body = response.body() != null ? response.body().string() : "";
                if (!response.isSuccessful()) {
                    throw new RuntimeException("GitHub App token exchange failed [" + response.code() + "]: " + body);
                }
                JsonNode node = objectMapper.readTree(body);
                String token = node.get("token").asText();
                String expiresAt = node.get("expires_at").asText(); // ISO-8601
                tokenExpiry = Instant.parse(expiresAt);
                log.info("Refreshed GitHub Installation Access Token. Expires at {}", tokenExpiry);
                return token;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch GitHub Installation Token", e);
        }
    }

    /**
     * Creates a signed JWT for the GitHub App (valid for 10 minutes).
     */
    private String buildAppJwt() throws Exception {
        PrivateKey pk = parsePem(privateKeyPem);
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(appId)
                .issuedAt(Date.from(now.minusSeconds(60)))   // issued 60 s ago to account for clock skew
                .expiration(Date.from(now.plusSeconds(600))) // 10 minutes max
                .signWith(pk, Jwts.SIG.RS256)
                .compact();
    }

    private PrivateKey parsePem(String pem) throws Exception {
        // Accept both "RSA PRIVATE KEY" (PKCS#1) and "PRIVATE KEY" (PKCS#8) PEM formats
        try (PEMParser parser = new PEMParser(new StringReader(pem))) {
            Object obj = parser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            if (obj instanceof PEMKeyPair kp) {
                return converter.getKeyPair(kp).getPrivate();
            } else if (obj instanceof PrivateKeyInfo info) {
                return converter.getPrivateKey(info);
            }
            throw new IllegalArgumentException("Unsupported PEM object: " + obj.getClass());
        }
    }
}

