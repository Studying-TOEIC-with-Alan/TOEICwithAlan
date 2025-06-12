package com.estsoft.project3.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.estsoft.project3.config.MockS3ClientConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(MockS3ClientConfig.class)
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @WithMockUser
    @Test
    @DisplayName("이미지 업로드 성공")
    void uploadImage_success() throws Exception {
        // given
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.png",
            MediaType.IMAGE_PNG_VALUE,
            "mock image content".getBytes(StandardCharsets.UTF_8)
        );

        // when
        ResultActions result = mockMvc.perform(multipart("/api/files/upload").file(file));

        // then
        String content = result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.filePath").exists())
            .andReturn()
            .getResponse()
            .getContentAsString();

        Map<String, String> response = objectMapper.readValue(content, Map.class);
        assertThat(response.get("filePath")).isNotBlank();
    }

    @WithMockUser
    @Test
    @DisplayName("이미지 삭제 성공")
    void deleteImage_success() throws Exception {
        // given
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "delete.png",
            MediaType.IMAGE_PNG_VALUE,
            "content to delete".getBytes(StandardCharsets.UTF_8)
        );

        ResultActions uploadResult = mockMvc.perform(multipart("/api/files/upload").file(file));
        String uploadResponse = uploadResult
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String filePath = objectMapper.readValue(uploadResponse, Map.class).get("filePath")
            .toString();

        // when
        ResultActions deleteResult = mockMvc.perform(post("/api/files/delete")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(Map.of("filePath", filePath))));

        // then
        deleteResult.andExpect(status().isOk());
    }

    @WithMockUser
    @Test
    @DisplayName("filePath 없이 삭제 요청 시 400 반환")
    void deleteMissingFilePath() throws Exception {
        // given
        Map<String, String> body = Map.of();

        // when
        ResultActions result = mockMvc.perform(post("/api/files/delete")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(body)));

        // then
        result.andExpect(status().isBadRequest())
            .andExpect(content().string("filePath는 필수입니다."));
    }
}