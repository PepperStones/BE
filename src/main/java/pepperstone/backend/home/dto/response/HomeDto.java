package pepperstone.backend.home.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HomeDto {

    private UserDto user;
    private TeamDto team;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserDto {
        private String name;
        private String level;
        private String centerName;
        private String jobName;
        private int recentExperience;
        private int totalExperienceThisYear;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TeamDto {
        private int count;
        private List<String> levels;
    }
}