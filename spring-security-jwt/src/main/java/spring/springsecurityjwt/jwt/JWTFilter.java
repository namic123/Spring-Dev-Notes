package spring.springsecurityjwt.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import spring.springsecurityjwt.dto.CustomUserDetails;
import spring.springsecurityjwt.entity.UserEntity;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * {@code JWTFilter}는 Spring Security의 {@link OncePerRequestFilter}를 확장하여
 * 매 요청마다 JWT 토큰을 검증하고, 유효한 경우 인증 정보를 {@link SecurityContextHolder}에 등록하는 필터
 *
 * 주요 기능:
 *     <li>Authorization 헤더에서 Bearer 토큰 추출</li>
 *     <li>JWT 유효성 및 만료 여부 검증</li>
 *     <li>CustomUserDetails를 기반으로 인증 객체 생성</li>
 *     <li>SecurityContext에 인증 정보 등록</li>
 *     <li>인증 실패 시 401 응답 반환</li>
 *
 * @author 박재성
 * @version 1.0
 * @since 2025-04-23
 */
public class JWTFilter extends OncePerRequestFilter {

    /** JWT 유틸 클래스 */
    private JWTUtil jwtUtil;

    /**
     * {@code JWTFilter} 생성자
     *
     * @param jwtUtil JWT 생성 및 검증 유틸 클래스
     */
    public JWTFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * 매 요청마다 실행되며, Authorization 헤더에서 JWT 토큰을 추출하고 검증한 뒤,
     * 유효한 경우 {@link SecurityContextHolder}에 인증 정보를 설정.
     *
     * @param request     HTTP 요청 객체
     * @param response    HTTP 응답 객체
     * @param filterChain 필터 체인
     * @throws ServletException 예외 발생 시
     * @throws IOException      입출력 오류 발생 시
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // "Authorization" 헤더가 없거나 "Bearer "로 시작하지 않으면 필터 체인 계속 진행
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Bearer 접두사 제거 후 JWT만 추출
        String token = authHeader.substring(7);

        // 토큰 만료 여부 확인
        if (!isTokenValid(token, response)) return;

        // access 토큰인지 카테고리 확인
        if (!"access".equals(jwtUtil.getCategory(token))) {
            setErrorResponse(response, "Invalid access token", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 토큰에서 사용자 정보 추출
        String username = jwtUtil.getUsername(token);
        String role = jwtUtil.getRole(token);

        // UserEntity로부터 CustomUserDetails 생성
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(username);
        userEntity.setRole(role);
        CustomUserDetails userDetails = new CustomUserDetails(userEntity);

        // 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        // 현재 SecurityContext에 인증 정보가 없으면 등록
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * JWT 토큰의 만료 여부를 검증합니다. 만료된 경우 401 응답을 설정합니다.
     *
     * @param token    JWT 토큰
     * @param response HTTP 응답 객체
     * @return 유효한 경우 {@code true}, 만료된 경우 {@code false}
     * @throws IOException 응답 출력 오류 발생 시
     */
    private boolean isTokenValid(String token, HttpServletResponse response) throws IOException {
        try {
            jwtUtil.isExpired(token);
            return true;
        } catch (ExpiredJwtException e) {
            setErrorResponse(response, "Access token expired", HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }

    /**
     * 인증 실패 또는 예외 발생 시 JSON 형태로 응답을 반환합니다.
     *
     * @param response HTTP 응답 객체
     * @param message  에러 메시지
     * @param status   HTTP 상태 코드
     * @throws IOException 응답 출력 오류 발생 시
     */
    private void setErrorResponse(HttpServletResponse response, String message, int status) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(status);
        PrintWriter writer = response.getWriter();
        writer.print("{\"error\": \"" + message + "\"}");
        writer.flush();
    }
}