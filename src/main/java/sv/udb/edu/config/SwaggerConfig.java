package sv.udb.edu.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracion de Swagger / OpenAPI (Guia 9).
 *
 * - @OpenAPIDefinition: Define metadata de la API y el servidor.
 * - @SecurityScheme: Define el esquema de seguridad "bearerAuth" de tipo HTTP Bearer.
 *   Esto habilita el boton "Authorize" en la UI de Swagger donde el usuario
 *   puede ingresar su JWT para probar endpoints protegidos.
 * - @SecurityRequirement: Aplica el esquema de seguridad globalmente.
 *   Los endpoints de /api/auth/** se excluyen con @SecurityRequirement({}) en su controller.
 *
 * Acceso a la UI: http://localhost:8080/swagger-ui/index.html
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title       = "Sistema de Reservas de Eventos - API",
                description = "API REST para gestion de eventos y reservas. DWF404 - Universidad Don Bosco.",
                version     = "1.0.0",
                contact     = @Contact(
                        name  = "DWF404 - Escuela de Computacion",
                        email = "dwf404@udb.edu.sv"
                ),
                license = @License(name = "UDB Academic License")
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Servidor de Desarrollo")
        },
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name        = "bearerAuth",
        description = "Ingresa el JWT generado en /api/auth/login. Formato: Bearer <token>",
        scheme      = "bearer",
        type        = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in          = SecuritySchemeIn.HEADER
)
public class SwaggerConfig {
    // Esta clase solo tiene anotaciones de configuracion.
    // Spring Boot + springdoc-openapi-starter-webmvc-ui auto-configura el resto.
}
