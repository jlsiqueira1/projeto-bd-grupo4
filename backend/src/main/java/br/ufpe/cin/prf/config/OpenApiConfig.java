package br.ufpe.cin.prf.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI prfOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Sistema de Avaliação PRF — API")
                .version("1.0.0")
                .description("Backend da Etapa 3 (Grupo 4 — Banco de Dados, CIn/UFPE): "
                        + "CRUD de todas as tabelas do modelo relacional."));
    }
}
