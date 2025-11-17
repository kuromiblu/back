package br.com.safe_line.safeline.modules.user.repository;

import br.com.safe_line.safeline.modules.user.model.RefreshToken;
import br.com.safe_line.safeline.modules.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

}
