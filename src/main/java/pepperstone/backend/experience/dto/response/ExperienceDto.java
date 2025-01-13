package pepperstone.backend.experience.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExperienceDto {
    private UserDto user;
    private ExperienceInfoDto experience;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserDto {
        private String companyNum;
        private String centerName;
        private String jobName;
        private String name;
        private String level;
        private String skin;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ExperienceInfoDto {
        private int accumulatedExperienceLastYear;
        private int totalExperienceThisYear;
    }
}