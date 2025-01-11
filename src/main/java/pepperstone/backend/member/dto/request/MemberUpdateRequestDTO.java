package pepperstone.backend.member.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberUpdateRequestDTO {
    private String companyNum;
    private String name;
    private LocalDate joinDate;
    private String centerGroup;
    private String jobGroup;
    private String level;
}
