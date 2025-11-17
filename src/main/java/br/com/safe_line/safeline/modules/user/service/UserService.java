package br.com.safe_line.safeline.modules.user.service;

import br.com.safe_line.safeline.modules.response.BaseResponse;
import br.com.safe_line.safeline.modules.user.dto.UserRequestDTO;
import br.com.safe_line.safeline.modules.user.dto.UserResponseDTO;
import br.com.safe_line.safeline.modules.user.exception.EmailAlreadyExistsException;
import br.com.safe_line.safeline.modules.user.model.User;
import br.com.safe_line.safeline.modules.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    //metodo para criar usuarios
    public BaseResponse<UserResponseDTO> createdUser(UserRequestDTO userRequestDTO) {

        //verificar se o usuário ja existe no banco de dados
        userRepository.findByEmail(userRequestDTO.email()).ifPresent(user -> {
            throw new EmailAlreadyExistsException();
        });
        var encoded = passwordEncoder.encode(userRequestDTO.password());

        var userSaved = userRepository.save(User.builder().name(userRequestDTO.name()).email(userRequestDTO.email())
                .password(encoded).build());

        return BaseResponse.success("usuário cadastrado com sucesso!", UserResponseDTO.builder()
                .name(userSaved.getName()).email(userSaved.getEmail()).createdAt(userSaved.getCreatedAt())
                        .build(), HttpStatus.CREATED.value());
    }

    //metodo para retornar usuários
    public BaseResponse<Set<UserResponseDTO>> getAllUsers(){
        var users = userRepository.findAll().stream().map(user ->  UserResponseDTO.builder()
                .name(user.getName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt()).build()).collect(Collectors.toSet());
        return BaseResponse.success("usuários encontrados com sucesso", users, HttpStatus.OK.value());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        return userRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("user not found"));
    }
}