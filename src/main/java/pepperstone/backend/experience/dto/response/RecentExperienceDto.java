package pepperstone.backend.experience.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecentExperienceDto {
    private List<JobDto> job;
    private List<LeaderDto> leader;
    private List<ProjectDto> project;
    private List<EvaluationDto> evaluation;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class JobDto {
        private int experience;
        private LocalDate date;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LeaderDto {
        private int experience;
        private LocalDate date;
        private String questName;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProjectDto {
        private int experience;
        private LocalDate date;
        private String projectName;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EvaluationDto {
        private int experience;
        private LocalDate date;
    }
}