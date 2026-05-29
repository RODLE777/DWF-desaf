package sv.udb.edu.config;


import sv.udb.edu.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuracion de beans de autenticacion.
 * - UserDetailsService: carga el usuario desde la BD por username.
 * - BCryptPasswordEncoder: hashea contrasenas con BCrypt (obligatorio en el enunciado).
 * - DaoAuthenticationProvider: conecta UserDetailsService + PasswordEncoder.
 * - AuthenticationManager: punto de entrada para autenticar credenciales.
 */
@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final UserRepository userRepository;

    /**
     * Carga el usuario desde la BD al autenticar.
     * Spring Security llama a este bean durante el proceso de login.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado: " + username));
    }

    /**
     * DaoAuthenticationProvider une la carga del usuario con la verificacion
     * de contrasena hasheada con BCrypt.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * BCryptPasswordEncoder: algoritmo de hashing seguro para contrasenas.
     * El strength por defecto es 10 rondas (buena relacion seguridad/rendimiento).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager: usado por AuthService para autenticar credenciales
     * durante el login.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
