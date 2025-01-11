package pepperstone.backend.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberInfoResponseDTO {
    private Long id;
    private String companyNum;
    private String name;
    private LocalDate joinDate;
    private String centerGroup;
    private String jobGroup;
    private String level;
    private String userId;
    private String initPassword;
    private String password;
}
