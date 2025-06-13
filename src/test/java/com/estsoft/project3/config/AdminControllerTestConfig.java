package com.estsoft.project3.config;

import com.estsoft.project3.contact.ContactService;
import com.estsoft.project3.service.AdminService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class AdminControllerTestConfig {

    @Bean
    public AdminService adminService() {
        return Mockito.mock(AdminService.class);
    }

    @Bean
    public ContactService contactService() {
        return Mockito.mock(ContactService.class);
    }
}
