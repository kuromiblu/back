package br.com.safe_line.safeline.modules.auth.service;

import br.com.safe_line.safeline.modules.user.model.AccessToken;
import br.com.safe_line.safeline.modules.user.model.RefreshToken;
import br.com.safe_line.safeline.modules.user.model.User;
import br.com.safe_line.safeline.modules.user.repository.AccessTokenRepository;
import br.com.safe_line.safeline.modules.user.repository.RefreshTokenRepository;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtTokenService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.access-expiration-ms}")
    private long accessTokenExpirationMs;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshTokenExpirationMs;


    private final AccessTokenRepository accessTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    // =============================
    //        CHAVE JWT
    // =============================
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // =============================
    //        GERAR ACCESS TOKEN
    // =============================
    public String generateAccessToken(User user, RefreshToken refreshToken) {

        Instant now = Instant.now();
        Instant expires = now.plus(15, ChronoUnit.MINUTES);

        String jwt = Jwts.builder()
                .subject(user.getIdUser().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expires))
                .claim("email", user.getEmail())
                .claim("roles", user.getRoles().stream().map(r -> r.getName()).toList())
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();

        AccessToken accessToken = AccessToken.builder()
                .token(jwt)
                .user(user)
                .refreshToken(refreshToken)
                .expiresAt(expires)
                .build();

        accessTokenRepository.save(accessToken);

        return jwt;
    }

    // =============================
    //       GERAR REFRESH TOKEN
    // =============================
    public RefreshToken generateRefreshToken(User user) {

        String tokenValue = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenValue)
                .user(user)
                .expiresAt(Instant.now().plusMillis(refreshTokenExpirationMs))
                .isRevoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    // =============================
    //       VALIDAR TOKEN JWT
    // =============================
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())     // chave correta
                    .build()
                    .parseSignedClaims(token);       // método correto
            return true;

        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // =============================
    //       REVOGAR TOKEN
    // =============================
    public void revokeAccessToken(String token) {
        accessTokenRepository.findByToken(token).ifPresent(accessToken -> {
            accessToken.setRevoked(true);
            accessTokenRepository.save(accessToken);
        });
    }

    // =============================
    //   RENOVAR / REFRESH TOKEN
    // =============================
    public String refreshAccessToken(String refreshTokenId) {

        RefreshToken refreshToken = refreshTokenRepository.findById(UUID.fromString(refreshTokenId))
                .orElseThrow(() -> new RuntimeException("Refresh token não encontrado."));

        AccessToken lastToken = accessTokenRepository
                .findFirstByRefreshTokenOrderByCreatedAtDesc(refreshToken)
                .orElseThrow(() -> new RuntimeException("Nenhum access token encontrado."));

        if (lastToken.getExpiresAt().isAfter(Instant.now())) {
            throw new RuntimeException("Access Token ainda é válido, não é necessário renovar.");
        }

        return generateAccessToken(lastToken.getUser(), refreshToken);
    }

    public String extractUsername(String jwt) {
        return Jwts.parser()
                .verifyWith(getSigningKey())   // chave correta
                .build()
                .parseSignedClaims(jwt)        // parse seguro
                .getPayload()
                .getSubject();                 // subject = id do usuário
    }

}
