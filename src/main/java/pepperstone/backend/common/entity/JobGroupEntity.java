package pepperstone.backend.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "jobGroup")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobGroupEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // centerGroup : jobGroup = 1 : N
    @ManyToOne(targetEntity = CenterGroupEntity.class)
    @JoinColumn(name = "centerGroup_id")
    private CenterGroupEntity centerGroup;

    // users : jobGroup = 1 : 1
    @OneToOne
    @JoinColumn(name = "user_id")
    private UserEntity users;

    // jobGroup : boards = 1 : N
    @OneToMany(mappedBy = "jobGroup")
    private List<BoardsEntity> boards = new ArrayList<>();
}
