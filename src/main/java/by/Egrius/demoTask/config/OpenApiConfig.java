package by.Egrius.demoTask.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;

public class OpenApiConfig {
    @Bean
    public OpenAPI hotelApiOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Hotel API")
                        .description("RESTful API приложение для работы с отелями")
                );
    }
}
