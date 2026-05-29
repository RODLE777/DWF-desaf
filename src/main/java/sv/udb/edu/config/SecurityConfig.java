package sv.udb.edu.config;


import sv.udb.edu.filter.JwtAuthenticationFilter;
import sv.udb.edu.security.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuracion central de Spring Security.
 *
 * - CSRF deshabilitado: Las APIs REST son stateless y no usan sesiones de navegador,
 *   por lo que CSRF no aplica. Los tokens JWT ya protegen contra este tipo de ataques.
 *
 * - STATELESS: No se crea ni usa HttpSession. Cada request debe incluir el JWT.
 *   Esto escala mejor y es coherente con arquitecturas REST.
 *
 * - @EnableMethodSecurity: Activa @PreAuthorize en los controllers para control
 *   por roles (Punto Extra: ROLE_ADMIN / ROLE_USER).
 *
 * - JwtAuthenticationFilter se ejecuta ANTES del filtro de autenticacion estandar
 *   para interceptar el token y autenticar al usuario en el SecurityContext.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // Habilita @PreAuthorize para control por roles
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final OAuth2AuthenticationSuccessHandler oAuth2SuccessHandler;

    /**
     * Rutas publicas que no requieren autenticacion.
     */
    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/auth/**",
            "/swagger-ui/**",
            "/swagger-ui/index.html",
            "/api-docs/**",
            "/api-docs",
            "/v3/api-docs/**",
            "/error"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CORS global: permite peticiones desde el frontend en otro puerto
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // CSRF deshabilitado: API REST stateless con JWT, no necesita proteccion CSRF
                .csrf(AbstractHttpConfigurer::disable)

                // Reglas de autorizacion por URL
                .authorizeHttpRequests(auth -> auth
                        // Endpoints publicos: registro, login, Swagger
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        // OAuth2 callback
                        .requestMatchers("/login/oauth2/**", "/oauth2/**").permitAll()
                        // Punto Extra: solo ADMIN puede crear/editar/eliminar eventos
                        .requestMatchers(HttpMethod.POST,   "/api/events").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/events/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/events/**").hasAuthority("ROLE_ADMIN")
                        // Todos los demas endpoints requieren JWT valido
                        .anyRequest().authenticated()
                )

                // STATELESS: no se crean ni usan sesiones HTTP
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Proveedor de autenticacion (DaoAuthenticationProvider con BCrypt)
                .authenticationProvider(authenticationProvider)

                // Punto Extra: OAuth2 con GitHub
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2SuccessHandler)
                )

                // El filtro JWT se ejecuta antes del filtro de usuario/password estandar
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configuracion CORS global para evitar bloqueos del navegador.
     * En produccion, reemplazar "*" por los dominios especificos del frontend.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}