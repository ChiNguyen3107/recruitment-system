package com.recruitment.system.service;

import com.recruitment.system.config.JwtUtil;
import com.recruitment.system.entity.RefreshToken;
import com.recruitment.system.entity.User;
import com.recruitment.system.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    @Value("${jwt.refresh.expiration:2592000000}")
    private long refreshExpirationMs;

    @Transactional
    public RefreshToken issueRefreshToken(User user) {
        // Optionally revoke existing active tokens (rotation policy)
        List<RefreshToken> existing = refreshTokenRepository.findAllByUserAndRevokedFalse(user);
        existing.forEach(t -> t.setRevoked(true));
        refreshTokenRepository.saveAll(existing);

        String token = jwtUtil.generateRefreshToken(user);
        LocalDateTime expiresAt = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(System.currentTimeMillis() + refreshExpirationMs), ZoneId.systemDefault());

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(token);
        refreshToken.setExpiresAt(expiresAt);
        refreshToken.setRevoked(false);
        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional(readOnly = true)
    public RefreshToken validateActiveToken(String token) {
        RefreshToken stored = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token không tồn tại"));
        if (stored.isRevoked()) {
            throw new RuntimeException("Refresh token đã bị thu hồi");
        }
        if (stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token đã hết hạn");
        }
        return stored;
    }

    @Transactional
    public void revokeToken(RefreshToken token, String replacedBy) {
        token.setRevoked(true);
        token.setReplacedByToken(replacedBy);
        refreshTokenRepository.save(token);
    }

    @Transactional
    public void revokeAll(User user) {
        List<RefreshToken> existing = refreshTokenRepository.findAllByUserAndRevokedFalse(user);
        existing.forEach(t -> t.setRevoked(true));
        refreshTokenRepository.saveAll(existing);
    }
}












