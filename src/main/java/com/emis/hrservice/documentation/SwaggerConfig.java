package com.emis.hrservice.documentation;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI api() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("EMIS HR Service")
                                .description(
                                        "EMIS HR Service API Documentation For KWARA"
                                                )
                                .version("1.0.0")
                .license(new License().name("Apache 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0")));

    }
}
