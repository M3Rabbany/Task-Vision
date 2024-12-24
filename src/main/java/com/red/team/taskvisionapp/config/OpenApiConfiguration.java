package com.red.team.taskvisionapp.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;


@OpenAPIDefinition(
        info = @Info(
                title = "Task Vision App",
                version = "1.0",
                description = "Task Vision App API",
                contact = @Contact(
                        name = "Red Team Dev",
                        url = "https://enigmacamp.com"
                )
        )
)
public class OpenApiConfiguration {
}
