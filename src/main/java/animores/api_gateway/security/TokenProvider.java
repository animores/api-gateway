package animores.api_gateway.security;

import io.jsonwebtoken.Jwts;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Getter
public class TokenProvider {
    private final String secretKey;
    private final int expirationHours;
    private final String issuer;

    public TokenProvider(@Value("${spring.jwt.secret-key}") String secretKey,
                         @Value("${spring.jwt.expiration-hours}") int expirationHours,
                         @Value("${spring.jwt.issuer}") String issuer) {
        this.secretKey = secretKey;
        this.expirationHours = expirationHours;
        this.issuer = issuer;
    }

    /**
     * 토큰 복호화
     * @param token
     * @return
     */
    public String validateTokenAndGetSubject(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

}