package br.com.safe_line.safeline.modules.auth.service;

import br.com.safe_line.safeline.config.CookieService;
import br.com.safe_line.safeline.modules.auth.controller.AuthRequestDTO;
import br.com.safe_line.safeline.modules.response.BaseResponse;
import br.com.safe_line.safeline.modules.user.exception.UsernameNotFoundException;
import br.com.safe_line.safeline.modules.user.model.AccessToken;
import br.com.safe_line.safeline.modules.user.model.User;
import br.com.safe_line.safeline.modules.user.repository.AccessTokenRepository;
import br.com.safe_line.safeline.modules.user.repository.RefreshTokenRepository;
import br.com.safe_line.safeline.modules.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CookieService cookieService;

    @Autowired
    private AccessTokenRepository accessTokenRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Value("${app.jwt.access-expiration-ms}")
    private long accessTokenExpirationMs;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshTokenExpirationMs;

    @Value("${app.jwt.cookie-name}")
    private String accessTokenCookieName;

    @Value("${app.jwt.refresh-cookie-name}")
    private String refreshTokenCookieName;

    public BaseResponse<String> authenticate(
            AuthRequestDTO authRequestDTO,
            HttpServletResponse response,
            HttpServletRequest request) {

        String email = authRequestDTO.email();
        String rawPassword = authRequestDTO.password();

        try {
            User user = this.userRepository.findByEmail(email)
                    .orElseThrow(UsernameNotFoundException::new);

            if (!this.passwordEncoder.matches(rawPassword, user.getPassword())) {
                throw new BadCredentialsException("Credenciais incorretas.");
            }

            if (!user.isCredentialsNonExpired()) {
                throw new CredentialsExpiredException("Credenciais expiradas, redefina sua senha.");
            }

            if (!user.isAccountNonLocked()) {
                throw new BadCredentialsException("A conta está bloqueada temporariamente.");
            }

            // =============================
            //     GERA REFRESH TOKEN
            // =============================
            var refreshToken = jwtTokenService.generateRefreshToken(user);

            // =============================
            //     GERA ACCESS TOKEN
            // =============================
            String accessToken = jwtTokenService.generateAccessToken(user, refreshToken);

            int accessTokenMaxAge = (int) (this.accessTokenExpirationMs / 1000);
            int refreshTokenMaxAge = (int) (this.refreshTokenExpirationMs / 1000);

            // =============================
            //  GRAVA COOKIES HTTP-ONLY
            // =============================
            cookieService.addSecureCookie(
                    response,
                    this.accessTokenCookieName,
                    accessToken,
                    accessTokenMaxAge
            );

            cookieService.addSecureCookie(
                    response,
                    this.refreshTokenCookieName,
                    refreshToken.getIdRefreshToken().toString(),
                    refreshTokenMaxAge
            );

            return BaseResponse.<String>success(
                    user.getEmail(),
                    "Authentication Successful!",
                    HttpStatus.OK.value()
            );

        } catch (AuthenticationException e) {
            return BaseResponse.error(e.getMessage());
        }
    }

    public BaseResponse<String> refreshAccessToken(HttpServletResponse response, HttpServletRequest request) {

        try {
            // 1. Pega o refresh token do cookie
            String refreshTokenId = cookieService.getTokenFromCookie(request, refreshTokenCookieName);

            if (refreshTokenId == null || refreshTokenId.isEmpty()) {
                return BaseResponse.error("Refresh token não encontrado no cookie.");
            }

            // 2. Gera um NOVO access token usando o refresh token salvo no banco
            String newAccessToken = jwtTokenService.refreshAccessToken(refreshTokenId);

            // 3. Define o novo access token no cookie seguro novamente
            int accessTokenMaxAge = (int) (this.accessTokenExpirationMs / 1000);

            cookieService.addSecureCookie(
                    response,
                    accessTokenCookieName,
                    newAccessToken,
                    accessTokenMaxAge
            );

            // 4. Sucesso → retorna o novo token
            return BaseResponse.success(
                    "Novo Access Token gerado com sucesso.",
                    newAccessToken,
                    200
            );

        } catch (Exception e) {
            return BaseResponse.error("Erro ao renovar Access Token: " + e.getMessage());
        }
    }

    @Transactional
    public BaseResponse<String> logout(HttpServletRequest request, HttpServletResponse response) {

        try {
            String accessTokenJwt = this.cookieService.getTokenFromCookie(request, this.accessTokenCookieName);

            if (accessTokenJwt != null) {
                Optional<AccessToken> accessTokenOpt = this.accessTokenRepository.findByToken(accessTokenJwt);

                if (accessTokenOpt.isPresent()) {
                    var token = accessTokenOpt.get();
                    var refreshToken = token.getRefreshToken();

                    token.setRevoked(true);
                    refreshToken.setRevoked(true);

                    this.accessTokenRepository.save(token);
                    this.refreshTokenRepository.save(refreshToken);
                }
            }

            this.cookieService.clearCookie(response, this.accessTokenCookieName);
            this.cookieService.clearCookie(response, this.refreshTokenCookieName);

            return BaseResponse.<String>success(
                    null,
                    "Logout Successful!",
                    HttpStatus.OK.value());

        } catch (Exception e) {

            return BaseResponse.error(e.getMessage());
        }
    }

}