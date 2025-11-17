package br.com.safe_line.safeline.config;

// Imports do Servlet e Lombok
import br.com.safe_line.safeline.modules.auth.service.JwtTokenService;
import br.com.safe_line.safeline.modules.user.repository.AccessTokenRepository;
import br.com.safe_line.safeline.modules.user.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

// Imports do Spring
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component // Diz ao Spring que esta classe é um bean gerenciado e deve ser injetada onde necessário
@Slf4j     // Adiciona automaticamente um logger (log.info, log.warn, etc.)
public class JwtFilter extends OncePerRequestFilter {

    // Service responsável por gerar e validar JWTs (extrair username, datas, etc.)
    @Autowired
    private JwtTokenService jwtTokenService;

    // Service utilitário que contém a lógica de ler tokens dos cookies
    @Autowired
    private CookieService cookieService;

    // Carrega informações de usuários (UserDetailsService customizado)
    @Autowired
    @Lazy  // Evita problema de dependência circular entre JwtFilter e UserService
    private UserService userService;

    // Repositório que armazena tokens e seus status (revogado/ativo)
    @Autowired
    private AccessTokenRepository accessTokenRepository;

    // Nome do cookie onde o token será procurado (vem do application.yaml)
    @Value("${app.jwt.secret}")
    private String accessTokenCookieName;

    // Lista de rotas que não exigem autenticação JWT
    // → qualquer rota que COMEÇA com essas strings será considerada pública
    private final List<String> publicPaths = List.of(
            "api/v1/auth/",         // login, refresh, register
            "/swagger-ui/",
            "/v3/api-docs/",  // documentação backend
            "/actuator/health"
    );

    /**
     * Método principal do filtro.
     * Ele é executado em TODA requisição antes de bater no controller,
     * exceto se já tiver um filtro que finalize a requisição
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // -------------------------------------
        // 1. VERIFICA SE A ROTA É PÚBLICA
        // -------------------------------------

        final String path = request.getServletPath(); // pega a rota atual

        // verifica se a rota atual começa com algum dos paths que são públicos
        boolean isPublicPath = this.publicPaths.stream().anyMatch(path::startsWith);

        if (isPublicPath) {
            // Se a rota for pública, NÃO faz validação de token
            filterChain.doFilter(request, response);
            return; // importante — evita que o código continue
        }


        // Se chegou aqui, a rota é PROTEGIDA e precisa validar JWT
        try {
            // -------------------------------------
            // 2. BUSCA O JWT NO COOKIE
            // -------------------------------------

            // Busca o token dentro do cookie com o nome configurado
            String jwt = this.cookieService.getTokenFromCookie(request, this.accessTokenCookieName);

            // -------------------------------------
            // 3. VERIFICA SE TOKEN EXISTE
            // OU SE USUÁRIO JÁ ESTÁ AUTENTICADO
            // -------------------------------------

            // Se o token é nulo/vazio OU alguém já autenticou o usuário antes deste filtro
            if (!StringUtils.hasText(jwt) ||
                    SecurityContextHolder.getContext().getAuthentication() != null) {

                // Apenas segue para o próximo filtro sem autenticar
                filterChain.doFilter(request, response);
                return;
            }

            // -------------------------------------
            // 4. EXTRAI USERNAME DO TOKEN (subject)
            // -------------------------------------

            // O JwtTokenService extrai do JWT o subject — normalmente email ou id do usuário
            String username = this.jwtTokenService.extractUsername(jwt);

            // -------------------------------------
            // 5. CARREGA Detalhes DO USUÁRIO
            // (UserDetails para uso do Spring Security)
            // -------------------------------------

            var userDetails = this.userService.loadUserByUsername(username);

            // -------------------------------------
            // 6. VERIFICA NO BANCO SE TOKEN ESTÁ REVOGADO
            // -------------------------------------

            // Busca no banco pelo token e verifica se NÃO está revogado
            boolean isTokenValid = this.accessTokenRepository.findByToken(jwt)
                    .map(token -> !token.isRevoked()) // se achou o token, verifica se revogado == false
                    .orElse(false);                    // se não achou no banco → token inválido

            // -------------------------------------
            // 7. SE TOKEN É VÁLIDO, AUTENTICA NO SPRING SECURITY
            // -------------------------------------

            if (isTokenValid) {

                // Cria objeto de autenticação padrão do Spring Security
                var authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, // usuário
                        null,        // credenciais (não coloca senha aqui por segurança)
                        userDetails.getAuthorities() // roles do usuário
                );

                // Preenche detalhes da requisição (IP, navegador, etc.)
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Registra autenticação no contexto global do Spring Security
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

        } catch (Exception e) {

            // -------------------------------------
            // 8. TRATA ERROS (TOKEN INVÁLIDO, USUÁRIO NÃO EXISTE, ETC.)
            // -------------------------------------

            // Faz log do erro sem travar a requisição
            JwtFilter.log.warn(
                    "Falha ao autenticar via JWT (rota protegida): {}. Token ignorado.",
                    e.getMessage()
            );

            // Garante que nenhum usuário seja considerado autenticado
            SecurityContextHolder.clearContext();
        }

        // -------------------------------------
        // 9. SEMPRE CONTINUA PARA O PRÓXIMO FILTRO
        // -------------------------------------

        filterChain.doFilter(request, response);
    }
}
