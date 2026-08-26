package com.example.projectcollab.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Project Collab API",
                version = "v1",
                description = "프로젝트 멤버십과 작업 제안·승인·수행·검토 흐름을 확인하는 협업 서비스 API"
        )
)
public class OpenApiConfiguration {
}
