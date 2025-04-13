package com.futuretech.pixelbook.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI pixelbookOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PixelBook API")
                        .description("API pour la gestion de mangas et volumes")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("FutureTech")
                                .url("https://futuretech.com")
                                .email("contact@futuretech.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")));
    }
} 