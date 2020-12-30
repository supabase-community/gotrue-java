package io.supabase.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.supabase.data.jwt.ParsedToken;
import io.supabase.exceptions.JwtSecretNotFoundException;
import io.supabase.exceptions.MalformedHeadersException;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;

public class ClientUtils {

    private ClientUtils() {
    }

    public static String loadUrl() {
        String url = System.getenv("GOTRUE_URL");
        if (url == null) {
            url = System.getProperty("gotrue.url");
        }
        return url;
    }

    public static Map<String, String> loadHeaders() throws MalformedHeadersException {
        Map<String, String> res = new HashMap<>();
        String headers = System.getenv("GOTRUE_HEADERS");

        if (headers == null) {
            headers = System.getProperty("gotrue.headers");
        }
        if (headers != null) {
            if (!headersValid(headers)) {
                throw new MalformedHeadersException(headers);
            }
            String[] arr = headers.split("[\\s,;]+");
            String[] s;
            for (String value : arr) {
                s = value.split("[=:]+");
                res.put(s[0], s[1]);
            }
        }
        return res;
    }

    private static boolean headersValid(String headers) {
        String regex = "^"; // beginning of line
        regex += "(?:"; // start of non-capturing group
        regex += "[^:=\\s,;]+"; // anything that is not empty(\s) or >,< or >;<
        regex += "[:=]"; // a : or a =
        regex += "[^:=\\s,;]+";
        regex += "[\\s,;]*"; // anything that is empty(\s) or >,< or >;<
        regex += ")+"; // closing of non-capturing group
        regex += "$"; // end of line
        return headers.matches(regex);
    }

    private static Jws<Claims> parseJwt(String jwt, String secret) {
        Key hmacKey = new SecretKeySpec(secret.getBytes(),
                SignatureAlgorithm.HS256.getJcaName());

        return Jwts.parser()
                .setSigningKey(hmacKey)
                .parseClaimsJws(jwt);
    }

    private static String getJwtSecret() {
        String secret = System.getenv("GOTRUE_JWT_SECRET");
        if (secret == null) {
            secret = System.getProperty("gotrue.jwt.secret");
        }
        return secret;
    }

    public static ParsedToken parseJwt(String jwt) throws JwtSecretNotFoundException {
        String secret = getJwtSecret();
        if (secret == null) {
            throw new JwtSecretNotFoundException();
        }
        Jws<Claims> claims = parseJwt(jwt, secret);
        ParsedToken parsed = new ParsedToken();
        parsed.setExp(claims.getBody().getExpiration());
        parsed.setSub(claims.getBody().getSubject());
        parsed.setEmail((String) claims.getBody().get("email"));
        Map<String, String> appData = (Map<String, String>) claims.getBody().get("app_metadata");
        parsed.setAppMetadata(appData != null ? appData : new HashMap<>());
        Map<String, String> userData = (Map<String, String>) claims.getBody().get("user_metadata");
        parsed.setUserMetadata(userData != null ? userData : new HashMap<>());
        parsed.setRole((String) claims.getBody().get("role"));
        return parsed;
    }
}
