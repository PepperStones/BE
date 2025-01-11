package pepperstone.backend.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MembersResponseDTO {
    private Long id;
    private String name;
    private String companyNum;
    private String centerGroup;
    private String jobGroup;
}
