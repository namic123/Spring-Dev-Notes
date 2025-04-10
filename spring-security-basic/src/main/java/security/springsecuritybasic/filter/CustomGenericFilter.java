package security.springsecuritybasic.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
/**
 * 필터 체인을 통과할 때마다 매번 실행되는 필터.
 * <p>
 * Spring Security의 GenericFilterBean을 상속받아 구현되며,
 * 요청이 forward되는 경우에도 반복적으로 실행된다.
 * </p>
 *
 * @author 박재성
 * @version 1.0
 * @since 2025-04-10
 */
public class CustomGenericFilter extends GenericFilterBean {
    /**
     * 필터의 핵심 로직. 요청이 들어올 때마다 실행되며,
     * 이후 체인의 다음 필터로 요청을 전달한다.
     *
     * @param servletRequest  서블릿 요청 객체
     * @param servletResponse 서블릿 응답 객체
     * @param filterChain    필터 체인
     * @throws IOException      입출력 예외 발생 시
     * @throws ServletException 예외 발생 시
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        System.out.println("CustomGenericFilter.doFilter");

        filterChain.doFilter(servletRequest, servletResponse);
    }
}
