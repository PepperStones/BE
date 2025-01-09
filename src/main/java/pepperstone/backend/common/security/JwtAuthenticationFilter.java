package pepperstone.backend.common.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import pepperstone.backend.common.entity.UserEntity;

import java.io.IOException;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final TokenProvider tokenProvider;

    // token을 사용하여 사용자 인증 및 등록
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)  throws ServletException, IOException {
        try {
            log.info("Filter is running...");
            String token = parseBearerToken(request);

            if (StringUtils.hasText(token) && !token.equalsIgnoreCase("null")) {
                Claims claims = tokenProvider.validateAndGetClaims(token);

                if(Objects.equals(claims.getIssuer(), "Token error")) {
                    handleErrorResponse(response, "false(토큰 에러 발생)", "Token error from filter");
                    return;
                } else if (Objects.equals(claims.getIssuer(), "Expired")) {
                    handleErrorResponse(response, "false(토큰 재발급을 받으세요)", "Token expired. Request URL: " + request.getContextPath());
                    return;
                } else {

                }
            }
        }
    }

    private String parseBearerToken(HttpServletRequest request) {
        // 요청의 헤더에서 Bearer Token을 가져옴
        String bearerToken = request.getHeader("Authorization");

        // 토큰 파싱
        return StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ") ? bearerToken.substring(7) : null;
    }

    private void handleErrorResponse(HttpServletResponse response, String message, String logMessage) throws IOException {
        log.error(logMessage);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(message);
    }

    private void authenticateUser(Claims claims, HttpServletRequest request) {
        // 토큰의 유효기간이 지나지 않은 경우
        log.info("insert new user");

        UserEntity user = new UserEntity();
        user.setId(Long.valueOf(claims.getSubject())); // 아이디 할당


    }
}
