package spring.swagger.config.swagger;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * {@code OpenApiConfig} 클래스는 Swagger(OpenAPI 3) 문서를 설정하기 위한 Spring Configuration 클래스입니다.
 * <p>
 * 이 클래스는 OpenAPI 메타 정보(제목, 설명, 버전)와 서버 주소 등을 정의하여 Swagger UI에서 문서화 및 테스트를 쉽게 할 수 있도록 지원합니다.
 * </p>
 *
 * @author 박재성
 * @version 1.0.0
 * @since 2025-06-16
 */
@Configuration
public class OpenApiConfig {

    /**
     * Swagger(OpenAPI 3) 문서를 구성하는 {@link OpenAPI} Bean을 생성합니다.
     *
     * @return OpenAPI 객체로, API 제목, 설명, 버전 및 서버 정보를 포함합니다.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Swagger Test API 목록")
                        .description("Spring Boot Swagger Test")
                        .version("v1.0.0"))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("개발용 서버")
                ));
    }
}