package pepperstone.backend.common.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "fcm")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FcmEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String token;

    // users : fcm = 1 : N
    @ManyToOne(targetEntity = UserEntity.class)
    @JoinColumn(name = "fcm_id")
    private UserEntity users;
}
