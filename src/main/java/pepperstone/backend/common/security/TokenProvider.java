package pepperstone.backend.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pepperstone.backend.common.config.JwtProperties;
import pepperstone.backend.common.entity.UserEntity;
import io.jsonwebtoken.Jwts;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class TokenProvider {
    private final JwtProperties jwtProperties;

    public String createAccessToken(UserEntity user) {
        return createToken(user, 1, ChronoUnit.HOURS);
    }

    public String createRefreshToken(UserEntity user) {
        return createToken(user, 1, ChronoUnit.DAYS);
    }

    private String createToken(UserEntity user, long amount, ChronoUnit unit) {
        Date expiryDate = Date.from(Instant.now().plus(amount, unit));

        return Jwts.builder()
                .signWith(SignatureAlgorithm.HS512, jwtProperties.getSecretKey())
                .setSubject(String.valueOf(user.getId())) // 토큰 제목
                .setIssuer(jwtProperties.getIssuer()) // 토큰 발급자
                .setIssuedAt(new Date()) // 토큰 발급 시간
                .setExpiration(expiryDate) // 토큰 만료 시간
                .claim("id", user.getId()) // 토큰에 사용자 아이디 추가하여 전달
                .compact(); // 토큰 생성
    }

    // 토큰 검증 및 토큰에 포함된 정보를 추출하여 인증 및 권한 부여
    public Claims validateAndGetClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(jwtProperties.getSecretKey())
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("ExpiredJwtException!!");
            return Jwts.claims().setIssuer("Expired");
        } catch (Exception e) {
            log.warn("Exception : {}", e.getMessage());
            return Jwts.claims().setIssuer("Token error");
        }
    }
}
