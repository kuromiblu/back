package br.com.safe_line.safeline.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    // (Regra 3) Injeção do NOSSO filtro via @Autowired

    @Autowired
    private JwtFilter jwtFilter; // Assumindo que este filtro existe

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // (Regra 8) Desativa proteção CSRF
                .csrf(AbstractHttpConfigurer::disable)

                // (Regra 8) Define a política de sessão como STATELESS
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // (Regra 8) Autorização de rotas
                .authorizeHttpRequests(auth -> auth

                        // --- INÍCIO DA CORREÇÃO (Regra 7) ---
                        // Devemos permitir os caminhos INTERNOS (depois do StripPrefix=2 do Gateway),
                        // que são /auth/login, /auth/login/register, /auth/refresh
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/api/v1/auth/login",
                                "/api/v1/user/create",
                                "/api/v1/report/create")
                        .permitAll()
                        // --- FIM DA CORREÇÃO ---

                        .anyRequest().authenticated())

                // (Regra 3) Adiciona o NOSSO filtro JWT ANTES do padrão
                .addFilterBefore(this.jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     ** (Regra 3) Expõe o AuthenticationManager como Bean
     *
     */

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     ** (Regra 8) Expõe o PasswordEncoder (BCrypt) como Bean
     *
     */

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
