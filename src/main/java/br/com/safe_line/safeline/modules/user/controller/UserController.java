package br.com.safe_line.safeline.modules.user.controller;

import br.com.safe_line.safeline.modules.response.BaseResponse;
import br.com.safe_line.safeline.modules.user.dto.UserRequestDTO;
import br.com.safe_line.safeline.modules.user.dto.UserResponseDTO;
import br.com.safe_line.safeline.modules.user.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/user")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/create")
    public ResponseEntity<BaseResponse<UserResponseDTO>> createUserController(@RequestBody UserRequestDTO userRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createdUser(userRequestDTO));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<Set<UserResponseDTO>>> getUserController() {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getAllUsers());
    }

}