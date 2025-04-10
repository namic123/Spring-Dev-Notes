package security.springsecuritybasic.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


/**
 * 요청당 단 한 번만 실행되는 필터.
 * <p>
 * Spring Security의 OncePerRequestFilter를 상속받아 구현되며,
 * 동일 요청에서 여러 번 필터 체인을 타더라도 한 번만 동작한다.
 * </p>
 *
 * @author 박재성
 * @version 1.0
 * @since 2025-04-10
 */
public class CustomOnceFilter extends OncePerRequestFilter {

    /**
     * 필터의 핵심 로직. 요청이 들어왔을 때 한 번만 실행되며,
     * 이후 체인의 다음 필터로 요청을 전달한다.
     *
     * @param request     HttpServletRequest 객체
     * @param response    HttpServletResponse 객체
     * @param filterChain 필터 체인
     * @throws ServletException 예외 발생 시
     * @throws IOException      입출력 예외 발생 시
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println("CustomOnceFilter");

        filterChain.doFilter(request, response);
    }
}
