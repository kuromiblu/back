package br.com.safe_line.safeline.modules.user.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "tb_role")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Role {

    @Id@GeneratedValue(strategy = GenerationType.UUID)
    private UUID idRole;

    @Size(min = 2, max = 50, message = "o nome precisa ter de 2 a 50 caracteres")
    @Column(length = 60)
    private String name;


}
