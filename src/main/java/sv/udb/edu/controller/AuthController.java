package sv.udb.edu.controller;

import sv.udb.edu.dto.request.LoginRequest;
import sv.udb.edu.dto.request.RegisterRequest;
import sv.udb.edu.dto.response.AuthResponse;
import sv.udb.edu.service.interfaces.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de autenticacion.
 * Endpoints publicos: no requieren JWT.
 * @SecurityRequirements({}) sobreescribe la configuracion global de Swagger
 * para estos endpoints, permitiendo probarlos sin token.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticacion", description = "Endpoints de registro, login y refresh token")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @SecurityRequirements({})   // Endpoint publico: no requiere Bearer token en Swagger
    @Operation(
            summary     = "Registrar nuevo usuario",
            description = "Crea un nuevo usuario con ROLE_USER. Retorna JWT de acceso y refresh token."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos o username ya en uso")
    })
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    @SecurityRequirements({})   // Endpoint publico
    @Operation(
            summary     = "Iniciar sesion",
            description = "Autentica al usuario y retorna access_token (24h) y refresh_token (7 dias)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login exitoso, tokens retornados"),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas")
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh-token")
    @SecurityRequirements({})
    @Operation(
            summary     = "Renovar token de acceso",
            description = "Usa el refresh_token en el header Authorization: Bearer <refresh_token> " +
                    "para generar un nuevo par de tokens."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tokens renovados"),
            @ApiResponse(responseCode = "401", description = "Refresh token invalido o expirado")
    })
    public ResponseEntity<AuthResponse> refreshToken(HttpServletRequest request,
                                                     HttpServletResponse response) {
        return ResponseEntity.ok(authService.refreshToken(request, response));
    }
}
