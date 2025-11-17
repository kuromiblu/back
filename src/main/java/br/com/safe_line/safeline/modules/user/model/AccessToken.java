package br.com.safe_line.safeline.modules.user.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tb_token")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccessToken {

    //id token login instant isrevoged refreshtoken

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID idToken;

    @Column(columnDefinition = "TEXT")
    private String token;

    @ManyToOne(fetch = FetchType.LAZY) //carrega qnd necessário (performance)
    @JoinColumn(name = "user_id")
    private User user;

    @CreationTimestamp
    private Instant createdAt;


    private Instant expiresAt;

    @Builder.Default
    private boolean isRevoked = false;

    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private RefreshToken refreshToken;

}
