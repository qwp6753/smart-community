package com.smartcommunity.server.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI smartCommunityOpenApi() {
        return new OpenAPI().info(new Info()
                .title("智慧小区管理系统 API")
                .version("v1.0.0")
                .description("智慧小区管理系统一期接口文档")
                .contact(new Contact().name("TRAE Code Assistant")));
    }
}
