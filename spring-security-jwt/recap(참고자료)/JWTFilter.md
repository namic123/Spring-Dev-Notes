JWT Filter extends OncePerRequestFilter
JWT 기반 인증 시스템에서 JWTFilter는 사용자 요청이 들어올 때마다 한 번만 실행되는 필터로써, 클라이언트가 전달한 JWT 토큰을 검증하고, 인증 정보를 Spring Security의 컨텍스트에 저장하는 역할을 합니다.

이 클래스는 OncePerRequestFilter를 상속하여 구현되며, Spring Security의 SecurityFilterChain 중간에 위치하여 작동합니다.

클래스 정의

public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    public JWTFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 구현 내용
    }
}
OncePerRequestFilter란?
javax.servlet.Filter를 상속한 Spring의 추상 클래스
요청당 한 번만 실행되도록 보장
보통 인증, 인가, 로깅 등의 보안 작업을 위해 사용
🔐 doFilterInternal() 메서드의 역할
doFilterInternal()는 필터의 핵심 로직을 처리하는 메서드입니다. JWT 인증 필터에서는 아래 순서대로 동작합니다:

✅ 1. 요청 헤더에서 JWT 추출
String authHeader = request.getHeader("Authorization");
보통 "Authorization: Bearer <token>" 형식
Bearer 접두어를 제거하여 토큰 추출
✅ 2. 토큰 유효성 검증
if (authHeader != null && authHeader.startsWith("Bearer ")) {
String token = authHeader.substring(7);

    if (!jwtUtil.isExpired(token)) {
        String username = jwtUtil.getUsername(token);
        String role = jwtUtil.getRole(token);
만료 여부 확인
getUsername(), getRole() 등을 통해 사용자 정보 파싱
✅ 3. 인증 객체 생성 및 SecurityContext 설정
UsernamePasswordAuthenticationToken authToken =
new UsernamePasswordAuthenticationToken(username, null, authorities);

        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}
UsernamePasswordAuthenticationToken은 Spring Security에서 사용자의 인증 정보를 담는 객체
SecurityContextHolder에 저장하면 이후 컨트롤러에서 인증 정보에 접근 가능
✅ 4. 다음 필터로 요청 전달
filterChain.doFilter(request, response);
현재 요청을 다음 필터로 넘김 (필수)
📌 JWTFilter가 필요한 이유

기능	설명
Stateless 인증 처리	세션 없이 JWT로 사용자 인증을 처리
모든 요청 감시	로그인 이후 클라이언트가 보내는 모든 요청에서 JWT를 확인
보안 일관성 유지	컨트롤러 접근 전에 인증 여부를 검증
SecurityContext 설정	Spring Security가 이후 처리에서 인증된 사용자로 인식하게 함
🧩 전체 흐름 요약
클라이언트 요청
↓
JWTFilter
- Authorization 헤더 추출
- 토큰 유효성 검증
- 인증 객체 생성 및 등록
  ↓
  다음 필터 또는 컨트롤러
  ✅ 커스터마이징 포인트
  만료된 토큰 처리 → 401 반환
  부적절한 토큰 → 로그 출력 또는 예외 발생
  토큰 갱신 로직 추가 (Optional)