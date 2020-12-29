package io.supabase.utils;

import io.jsonwebtoken.*;
import io.supabase.data.dto.UserMetadataDto;
import io.supabase.data.jwt.ParsedToken;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;

public class ClientUtils {
    public static String loadUrl() {
        String url = System.getenv("GOTRUE_URL");
        if (url == null) {
            url = System.getProperty("gotrue.url");
        }
        return url;
    }

    public static Map<String, String> loadHeaders() {
        Map<String, String> res = new HashMap<>();
        String headers = System.getenv("GOTRUE_HEADERS");

        if (headers == null) {
            headers = System.getProperty("gotrue.headers");
        }
        if (headers != null) {
            String[] arr = headers.split("[\\s,;-]+");
            if (arr.length > 0) {
                String[] s;
                for (String value : arr) {
                    s = value.split("[=:]+");
                    res.put(s[0], s[1]);
                }
            }
        }
        return res;
    }

    private static Jws<Claims> parseJwt(String jwt, String secret) throws JwtException {
        Key hmacKey = new SecretKeySpec(secret.getBytes(),
                SignatureAlgorithm.HS256.getJcaName());

        Jws<Claims> claims = Jwts.parser()
                .setSigningKey(hmacKey)
                .parseClaimsJws(jwt);

        return claims;
    }

    private static String getJwtSecret() {
        String secret = System.getenv("GOTRUE_JWT_SECRET");
        if (secret == null) {
            secret = System.getProperty("gotrue.jwt.secret");
        }
        return secret;
    }

    public static ParsedToken parseJwt(String jwt) throws JwtException {
        String secret = getJwtSecret();
        if (secret == null) {
            throw new RuntimeException("JWT Secret is not defined.");
        }
        Jws<Claims> claims = parseJwt(jwt, secret);
        ParsedToken parsed = new ParsedToken();
        parsed.setExp(claims.getBody().getExpiration());
        parsed.setSub(claims.getBody().getSubject());
        parsed.setEmail((String) claims.getBody().get("email"));
        parsed.setAppMetadata((Map<String, String>) claims.getBody().get("app_metadata"));
        parsed.setUserMetadata((UserMetadataDto) claims.getBody().get("user_metadata"));
        parsed.setRole((String) claims.getBody().get("role"));
        return parsed;
    }
}
