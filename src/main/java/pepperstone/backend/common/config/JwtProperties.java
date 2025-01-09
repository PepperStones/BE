package pepperstone.backend.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt") // 자바 클래스에 프로퍼티 값을 가져와서 사용하는 어노테이션, “jwt.issuer”, “jwt.secretKey”와 같은 형식으로 정의
public class JwtProperties {
    private String issuer;
    private String secretKey;
}