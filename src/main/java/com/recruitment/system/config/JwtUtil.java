package com.recruitment.system.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility class để tạo và xác thực JWT tokens
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    // Backward-compatible default expiration (ms). Still used by legacy methods.
    @Value("${jwt.expiration:900000}")
    private Long expiration;

    // New: separate expirations for access and refresh tokens (ms)
    @Value("${jwt.access.expiration:900000}")
    private Long accessExpiration;

    @Value("${jwt.refresh.expiration:2592000000}")
    private Long refreshExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername(), expiration);
    }

    public String generateToken(UserDetails userDetails, Map<String, Object> extraClaims) {
        Map<String, Object> claims = new HashMap<>(extraClaims);
        return createToken(claims, userDetails.getUsername(), expiration);
    }

    private String createToken(Map<String, Object> claims, String subject, long expirationMillis) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(getSigningKey())
                .compact();
    }

    // New: explicit access token generator
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("token_type", "access");
        return createToken(claims, userDetails.getUsername(), accessExpiration);
    }

    public String generateAccessToken(UserDetails userDetails, Map<String, Object> extraClaims) {
        Map<String, Object> claims = new HashMap<>(extraClaims);
        claims.put("token_type", "access");
        return createToken(claims, userDetails.getUsername(), accessExpiration);
    }

    // New: explicit refresh token generator
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("token_type", "refresh");
        return createToken(claims, userDetails.getUsername(), refreshExpiration);
    }

    // Helpers for token type
    public String extractTokenType(String token) {
        return extractClaim(token, c -> (String) c.get("token_type"));
    }

    public boolean isAccessToken(String token) {
        String type = extractTokenType(token);
        return type == null || "access".equals(type); // default legacy tokens treated as access
    }

    public boolean isRefreshToken(String token) {
        String type = extractTokenType(token);
        return "refresh".equals(type);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
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

    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public Boolean validateAccessToken(String token, UserDetails userDetails) {
        return validateToken(token, userDetails) && isAccessToken(token);
    }

    public Boolean canTokenBeRefreshed(String token) {
        return !isTokenExpired(token);
    }

    public String refreshToken(String token) {
        final Claims claims = extractAllClaims(token);
        // Preserve token_type if present; default to access when refreshing legacy tokens
        String tokenType = (String) claims.get("token_type");
        long exp = ("refresh".equals(tokenType)) ? refreshExpiration : accessExpiration;

        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + exp))
                .signWith(getSigningKey())
                .compact();
    }
}