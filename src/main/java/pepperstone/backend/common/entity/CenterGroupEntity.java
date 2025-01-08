package pepperstone.backend.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "centerGroup")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CenterGroupEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // centerGroup : jobGroup = 1 : N
    @OneToMany(mappedBy = "centerGroup")
    private List<JobGroupEntity> jobGroups = new ArrayList<>();

    // centerGroup : boards = 1 : N
    @OneToMany(mappedBy = "centerGroup")
    private List<BoardsEntity> boards = new ArrayList<>();
}
