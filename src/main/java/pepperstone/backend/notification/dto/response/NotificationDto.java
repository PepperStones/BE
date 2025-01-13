package pepperstone.backend.notification.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDto {
    private Long pushId;
    private String title;
    private String content;
    private LocalDate createdAt;
    private Boolean open;
}
