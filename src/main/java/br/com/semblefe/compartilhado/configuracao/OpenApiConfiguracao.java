package br.com.semblefe.compartilhado.configuracao;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguracao {

    @Bean
    OpenAPI semBlefeOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Sem Blefe API")
                .description("API da rede social e plataforma profissional da comunidade musical Sem Blefe.")
                .version("v1")
                .contact(new Contact().name("Sem Blefe")));
    }
}
