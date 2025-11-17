package br.com.safe_line.safeline.modules.exception;

import br.com.safe_line.safeline.modules.response.BaseResponse;
import br.com.safe_line.safeline.modules.user.exception.EmailAlreadyExistsException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ============================================
    //   ERROS DE VALIDAÇÃO (@Valid, @NotNull...)
    // ============================================
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<BaseResponse<String>> handleException(ConstraintViolationException ex) {

        String message = ex.getConstraintViolations()
                .stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .orElse("Erro de validação");

        BaseResponse<String> response = BaseResponse.error(message);
        response.setStatusCode(400);

        return ResponseEntity.status(400).body(response);
    }

    // ============================================
    //         EMAIL JÁ EXISTE (409)
    // ============================================
    @ExceptionHandler({EmailAlreadyExistsException.class})
    public ResponseEntity<BaseResponse<String>> handleAlreadyExistException(RuntimeException ex) {

        BaseResponse<String> response = BaseResponse.error(ex.getMessage());
        response.setStatusCode(409);

        return ResponseEntity.status(409).body(response);
    }

    // ============================================
    //     USERNAME NOT FOUND (404)
    // ============================================
    @ExceptionHandler({UsernameNotFoundException.class})
    public ResponseEntity<BaseResponse<String>> handleUsernameNotFoundException(RuntimeException ex) {

        BaseResponse<String> response = BaseResponse.error(ex.getMessage());
        response.setStatusCode(404);

        return ResponseEntity.status(404).body(response);
    }
}
