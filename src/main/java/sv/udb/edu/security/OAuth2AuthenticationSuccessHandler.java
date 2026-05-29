package sv.udb.edu.security;

import sv.udb.edu.entity.Token;
import sv.udb.edu.entity.User;
import sv.udb.edu.enums.Role;
import sv.udb.edu.repository.TokenRepository;
import sv.udb.edu.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Punto Extra: OAuth2 con GitHub.
 * Handler ejecutado cuando el login con GitHub es exitoso.
 * - Crea el usuario en BD si es su primer acceso.
 * - Genera un JWT propio de la aplicacion.
 * - Redirige al frontend con el token en el query param.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // GitHub provee 'login' como nombre de usuario
        String githubLogin = oAuth2User.getAttribute("login");
        String githubName  = oAuth2User.getAttribute("name");

        log.info("OAuth2 login exitoso para GitHub user: {}", githubLogin);

        // Buscar o crear el usuario en BD
        User user = userRepository.findByUsername(githubLogin)
                .orElseGet(() -> {
                    log.info("Primer acceso de usuario OAuth2: {}. Creando en BD...", githubLogin);
                    String[] nameParts = githubName != null ? githubName.split(" ", 2) : new String[]{"", ""};
                    return userRepository.save(User.builder()
                            .username(githubLogin)
                            .firstname(nameParts[0])
                            .lastname(nameParts.length > 1 ? nameParts[1] : "")
                            .age(0)
                            // Contrasena aleatoria porque usa OAuth2, no login directo
                            .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                            .role(Role.ROLE_USER)
                            .build());
                });

        // Revocar tokens anteriores
        revokeAllUserTokens(user);

        // Generar JWT propio de la aplicacion
        String jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        saveUserToken(user, jwtToken);

        // Redirigir al frontend con el token (ajustar URL segun el frontend)
        String redirectUrl = "http://localhost:3000/oauth2/callback" +
                "?access_token=" + jwtToken +
                "&refresh_token=" + refreshToken +
                "&username=" + user.getUsername();

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private void saveUserToken(User user, String jwtToken) {
        tokenRepository.save(Token.builder()
                .user(user)
                .token(jwtToken)
                .revoked(false)
                .expired(false)
                .build());
    }

    private void revokeAllUserTokens(User user) {
        var validTokens = tokenRepository.findAllValidTokensByUser(user.getIdUser());
        if (validTokens.isEmpty()) return;
        validTokens.forEach(t -> {
            t.setExpired(true);
            t.setRevoked(true);
        });
        tokenRepository.saveAll(validTokens);
    }
}