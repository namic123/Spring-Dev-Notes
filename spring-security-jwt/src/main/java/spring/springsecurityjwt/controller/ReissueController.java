package spring.springsecurityjwt.controller;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import spring.springsecurityjwt.entity.RefreshEntity;
import spring.springsecurityjwt.jwt.JWTUtil;
import spring.springsecurityjwt.repository.RefreshRepository;

import java.util.Date;

/**
 * Refresh Token을 기반으로 Access Token을 재발급하는 컨트롤러입니다.
 *
 * @author 박재성
 * @version 1.0
 * @since 2025-04-23
 */
@RestController
@RequiredArgsConstructor
public class ReissueController {

    private final JWTUtil jwtUtil;

    private RefreshRepository refreshRepository;

    /**
     * 클라이언트의 쿠키에서 refresh 토큰을 추출하여 유효성을 검사한 후, Access 토큰을 재발급합니다.
     *
     * @param request  HTTP 요청
     * @param response HTTP 응답
     * @return 응답 본문 및 상태 코드
     */
    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {

        // 토큰 추출
        String refreshToken = extractRefreshToken(request);

        // 추출된 토큰이 없는 경우
        if (refreshToken == null) {
            return ResponseEntity.badRequest().body("refresh token is missing");
        }

        // 추출된 토큰이 만료된 경우거나 카테고리가 refresh가 아닌 경우
        if (!validateRefreshToken(refreshToken)) {
            return ResponseEntity.badRequest().body("refresh token is expired");
        }
        // DB에 저장된 Refresh인지 확인
        Boolean isExist = refreshRepository.existsByRefresh(refreshToken);
        if (!isExist) {
            //response body
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }



        String username = jwtUtil.getUsername(refreshToken);
        String role = jwtUtil.getRole(refreshToken);

        // 새로운 Access, refresh 토큰 발급
        String newAccess = jwtUtil.createJwt("access", username, role, 600000L);
        String newRefresh = jwtUtil.createJwt("refresh", username, role, 86400000L);

        //Refresh 토큰 저장 DB에 기존의 Refresh 토큰 삭제 후 새 Refresh 토큰 저장
        refreshRepository.deleteByRefresh(refreshToken);
        addRefreshEntity(username, newRefresh, 86400000L);

        // 표준 Authorization 헤더에 Bearer 토큰으로 설정
        response.setHeader("Authorization", "Bearer " + newAccess);
        response.addCookie(jwtUtil.createCookie("refresh", newRefresh));

        return ResponseEntity.ok().build();
    }

    /**
     * 클라이언트 쿠키에서 refresh 토큰을 추출합니다.
     */
    private String extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        for (Cookie cookie : cookies) {
            if ("refresh".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    /**
     * refresh 토큰의 유효성 및 카테고리를 검증합니다.
     */
    private boolean validateRefreshToken(String refreshToken) {
        try {
            jwtUtil.isExpired(refreshToken);
        } catch (ExpiredJwtException e) {
            return false;
        }

        return "refresh".equals(jwtUtil.getCategory(refreshToken));
    }

    private void addRefreshEntity(String username, String refresh, Long expiredMs) {

        Date date = new Date(System.currentTimeMillis() + expiredMs);

        RefreshEntity refreshEntity = new RefreshEntity();
        refreshEntity.setUsername(username);
        refreshEntity.setRefresh(refresh);
        refreshEntity.setExpiration(date.toString());

        refreshRepository.save(refreshEntity);
    }
}