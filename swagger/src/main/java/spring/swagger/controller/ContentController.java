package spring.swagger.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import spring.swagger.dto.ContentRequestDTO;
import spring.swagger.dto.ContentResponseDTO;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Content API", description = "게시글 도메인 API")
public class ContentController {
    @Operation(
            summary = "게시글 Read",
            description = "게시글의 ID를 파라미터로 보내면 해당하는 게시글 조회",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "조회할 게시글 ID",
                            required = true,
                            in = ParameterIn.PATH
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ContentResponseDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "실패"
                    )
            }
    )
    @GetMapping("/content/{id}")
    public ResponseEntity<?> contentGet(@PathVariable("id") String id) {
        Map<String, Object> responseBody = Map.of(
                "id", id,
                "title", "제목" + id,
                "content", "내용" + id
        );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(new MediaType(MediaType.APPLICATION_JSON));

        return new ResponseEntity<>(responseBody, httpHeaders, HttpStatus.OK);
    }
    @PostMapping("/content")
    public ResponseEntity<?> contentPost(@RequestBody ContentRequestDTO dto) {

        Map<String, Object> resultBody = Map.of("id", 1L);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(new MediaType(MediaType.APPLICATION_JSON));

        return new ResponseEntity<>(resultBody, httpHeaders, HttpStatus.OK);
    }

    @DeleteMapping("/content/{id}")
    public ResponseEntity<?> contentDelete(@PathVariable("id") Long id) {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(new MediaType(MediaType.APPLICATION_JSON));

        return new ResponseEntity<>(httpHeaders, HttpStatus.OK);
    }
}
