package com.sportsaas.auth.infra;

import com.sportsaas.auth.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Service de generation et validation des tokens JWT.
 */
@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration:86400000}")
    private long jwtExpiration; // 24h par defaut

    @Value("${app.jwt.refresh-expiration:604800000}")
    private long refreshExpiration; // 7 jours par defaut

    /**
     * Genere un access token pour un utilisateur.
     */
    public String generateToken(User user) {
        return generateToken(new HashMap<>(), user);
    }

    /**
     * Genere un access token avec des claims supplementaires.
     */
    public String generateToken(Map<String, Object> extraClaims, User user) {
        extraClaims.put("role", user.getRole().name());
        extraClaims.put("tenantId", user.getTenantId().toString());
        extraClaims.put("firstName", user.getFirstName());
        extraClaims.put("lastName", user.getLastName());

        return buildToken(extraClaims, user.getId().toString(), jwtExpiration);
    }

    /**
     * Genere un refresh token.
     */
    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tenantId", user.getTenantId().toString());
        claims.put("role", user.getRole().name());
        return buildToken(claims, user.getId().toString(), refreshExpiration);
    }

    /**
     * Extrait le user ID du token.
     */
    public UUID extractUserId(String token) {
        String subject = extractClaim(token, Claims::getSubject);
        return UUID.fromString(subject);
    }

    /**
     * Extrait le tenant ID du token.
     */
    public UUID extractTenantId(String token) {
        String tenantId = extractClaim(token, claims -> claims.get("tenantId", String.class));
        return UUID.fromString(tenantId);
    }

    /**
     * Extrait le role du token.
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    /**
     * Verifie si le token est valide.
     */
    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verifie si le token est expire.
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String buildToken(Map<String, Object> extraClaims, String subject, long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
