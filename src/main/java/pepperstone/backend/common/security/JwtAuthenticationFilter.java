package pepperstone.backend.common.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
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
                    authenticateUser(claims, request);
                }
            } else {
                log.warn("Token is null");
            }
        } catch (Exception e) {
            log.error("Could not set user authentication in security context", e);
        }

        // 다음 필터로 계속 진행
        filterChain.doFilter(request, response);
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
        String jsonResponse = String.format("{\"success\": false, \"message\": \"%s\"}", message);

        response.getWriter().write(jsonResponse);
    }

    private void authenticateUser(Claims claims, HttpServletRequest request) {
        // 토큰의 유효기간이 지나지 않은 경우
        log.info("insert new user");

        UserEntity user = new UserEntity();
        user.setId(Long.valueOf(claims.getSubject())); // 아이디 할당

        // 인증 완료 -> SecurityContextHolder에 등록되어야 인증된 사용자
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null, AuthorityUtils.NO_AUTHORITIES); // 사용자 정보
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request)); // 사용자 인증 세부 정보 설정

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext(); // 빈 SecurityContext 설정
        securityContext.setAuthentication(authentication); // context에 인증 정보 설정
        SecurityContextHolder.setContext(securityContext); // SecurityContextHolder 저장
    }
}
