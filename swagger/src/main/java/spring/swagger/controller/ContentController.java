package spring.swagger.controller;

import io.swagger.v3.oas.annotations.Operation; // 개별 API 메서드에 대한 설명 정의
import io.swagger.v3.oas.annotations.Parameter; // 메서드 파라미터에 대한 상세 설명 정의
import io.swagger.v3.oas.annotations.enums.ParameterIn; // 파라미터 위치(path, query 등) 명시
import io.swagger.v3.oas.annotations.media.Content; // 요청 또는 응답의 콘텐츠 타입 정의
import io.swagger.v3.oas.annotations.media.ExampleObject; // 응답 예시 값 정의
import io.swagger.v3.oas.annotations.media.Schema; // 요청/응답 객체의 스키마 정의
import io.swagger.v3.oas.annotations.responses.ApiResponse; // 응답 코드별 설명 및 스키마 정의
import io.swagger.v3.oas.annotations.tags.Tag; // 컨트롤러 전체에 대한 태그(카테고리) 정의

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import spring.swagger.dto.ContentRequestDTO;
import spring.swagger.dto.ContentResponseDTO;

import java.util.Map;

// Swagger 문서에서 이 컨트롤러를 "ContentAPI"라는 이름으로 분류하고 설명
@Tag(name = "ContentAPI", description = "게시글 도메인 API")
@RestController
@RequestMapping("/api/v1")
public class ContentController {

    /**
     * 게시글 조회 API
     *
     * - summary: 문서 상에서 API 요약으로 출력
     * - description: 상세 설명 출력
     * - parameters: 경로 변수 `id`에 대해 Swagger 문서화
     * - responses:
     *     - 200: 응답 성공 시 JSON 형식으로 `ContentResponseDTO` 형태로 반환된다고 문서화
     *     - 400: 잘못된 요청에 대한 설명 추가
     */
    @Operation(
            summary = "게시글 Read",
            description = "게시글의 ID를 파라미터로 보내면 해당하는 게시글 조회",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "조회할 게시글 ID", // Swagger UI에 표시될 설명
                            required = true, // 필수 여부 명시
                            in = ParameterIn.PATH // 경로 변수(path parameter)로 명시
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공",
                            content = @Content(
                                    mediaType = "application/json", // 응답 콘텐츠 타입
                                    schema = @Schema(implementation = ContentResponseDTO.class) // 응답 스키마 객체
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "실패"
                    )
            }
    )
    @GetMapping("/content/{id}")
    public ResponseEntity<?> contentGet(@PathVariable("id") Long id) {
        // 실제 로직은 생략되어 있으나, Swagger 문서에는 ContentResponseDTO 기준으로 문서화됨
        Map<String, Object> resultBody = Map.of(
                "id", id,
                "title", "제목" + id,
                "content", "내용" + id
        );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(new MediaType("application", "json"));

        return new ResponseEntity<>(resultBody, httpHeaders, HttpStatus.OK);
    }

    /**
     * 게시글 작성 API
     *
     * - summary: API 요약
     * - description: API 상세 설명
     * - requestBody: Swagger 문서에서 요청 바디의 예시 및 스키마 명시
     * - responses:
     *     - 200: 성공적으로 게시글 생성 후 ID 반환 (예시 값 포함)
     *     - 400: 실패 응답
     */
    @Operation(
            summary = "게시글 Create",
            description = "게시글 작성 후 ID 반환",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "게시글 데이터",
                    required = true,
                    content = @Content(
                            mediaType = "application/json", // 요청 바디의 MIME 타입 명시
                            schema = @Schema(implementation = ContentRequestDTO.class) // 요청 데이터 구조 명시
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = "{\"id\":1}" // 성공 응답 예시
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "실패"
                    )
            }
    )
    @PostMapping("/content")
    public ResponseEntity<?> contentPost(@RequestBody ContentRequestDTO dto) {
        // 게시글 등록 로직 생략, ID=1 가정하여 반환
        Map<String, Object> resultBody = Map.of("id", 1L);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(new MediaType("application", "json"));

        return new ResponseEntity<>(resultBody, httpHeaders, HttpStatus.OK);
    }
}
