package br.com.safe_line.safeline.modules.auth.controller;

import br.com.safe_line.safeline.modules.auth.service.AuthService;
import br.com.safe_line.safeline.modules.response.BaseResponse;
import br.com.safe_line.safeline.modules.user.dto.UserRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth") // Rota base: /api/v1/auth
@Tag(name = "Autenticação", description = "Endpoints para Login, Redefinição de Senha, Refresh e Logout")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Operation(summary = "Autentica um usuário",
            description = "Realiza o login, retorna o email e define cookies seguros (access e refresh).")
    @PostMapping("/login")
    public ResponseEntity<BaseResponse<String>> authenticate(
            @Valid @RequestBody AuthRequestDTO authRequestDTO,
            HttpServletResponse response,
            HttpServletRequest request) {

        var serviceResponse = this.authService.authenticate(
                authRequestDTO, response, request);
        return ResponseEntity.status(serviceResponse.getStatusCode()).body(serviceResponse);
    }


    @Operation(summary = "Renova o token de acesso (Access Token)", description = "Usa o 'refresh_token' (lido do cookie HttpOnly).")
    @PostMapping("/refresh") // Rota: /api/v1/auth/refresh
    public ResponseEntity<BaseResponse<String>> refreshAccessToken(
            HttpServletResponse response,
            HttpServletRequest request) {

        var serviceResponse = this.authService.refreshAccessToken(
                response, request);
        return ResponseEntity.status(serviceResponse.getStatusCode()).body(serviceResponse);
    }

    @Operation(summary = "Desloga o usuário",
            description = "Revoga os tokens no banco de dados e limpa os cookies.")
    @PostMapping("/logout") // Rota: /api/v1/auth/logout
    public ResponseEntity<BaseResponse<String>> logout(
            HttpServletResponse response,
            HttpServletRequest request) {

        var serviceResponse = this.authService.logout(request, response);
        return ResponseEntity.status(serviceResponse.getStatusCode()).body(serviceResponse);
    }
}