package pepperstone.backend.home.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pepperstone.backend.common.entity.UserEntity;
import pepperstone.backend.common.repository.UserRepository;
import pepperstone.backend.home.service.HomeService;

import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getHomeData(@AuthenticationPrincipal UserEntity userInfo) {
        try {

            Optional<UserEntity> user = userRepository.findById(userInfo.getId());

            if (user.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("code", 404, "message", "Not Found: 사용자 정보가 존재하지 않습니다."));
            }
            return ResponseEntity.ok(Map.of("code", 200, "data", homeService.getHomeData(user.get())));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("code", 404, "data", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body(Map.of("code", 500, "message", "Internal Server Error: 홈 화면 데이터를 가져오는 도중 오류가 발생했습니다."));
        }
    }
}