package br.com.safe_line.safeline.config;

import br.com.safe_line.safeline.modules.user.model.Role;
import br.com.safe_line.safeline.modules.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {

        createRoleIfNotExists("ROLE_ADMIN");
        createRoleIfNotExists("ROLE_USER");
    }

    private void createRoleIfNotExists(String roleName) {
        if (!roleRepository.existsByName(roleName)) {
            Role role = Role.builder()
                    .name(roleName)
                    .build();

            roleRepository.save(role);
            System.out.println("✔ Papel criado: " + roleName);
        }
    }
}
