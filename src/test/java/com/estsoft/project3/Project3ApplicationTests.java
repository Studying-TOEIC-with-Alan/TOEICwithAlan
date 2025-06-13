package com.estsoft.project3;

import com.estsoft.project3.config.MockS3ClientConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@AutoConfigureMockMvc
@Import(MockS3ClientConfig.class)
@ActiveProfiles("test")
class Project3ApplicationTests {

    @Test
    void contextLoads() {
    }

}
