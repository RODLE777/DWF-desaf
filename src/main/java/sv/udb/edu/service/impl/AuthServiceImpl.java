package sv.udb.edu.service.impl;

import sv.udb.edu.dto.request.LoginRequest;
import sv.udb.edu.dto.request.RegisterRequest;
import sv.udb.edu.dto.response.AuthResponse;
import sv.udb.edu.entity.Token;
import sv.udb.edu.entity.User;
import sv.udb.edu.enums.Role;
import sv.udb.edu.exception.BusinessException;
import sv.udb.edu.repository.TokenRepository;
import sv.udb.edu.repository.UserRepository;
import sv.udb.edu.security.JwtService;
import sv.udb.edu.service.interfaces.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementacion del servicio de autenticacion.
 *
 * Flujo de autenticacion JWT:
 * 1. Cliente envia credenciales a POST /api/auth/login
 * 2. AuthenticationManager verifica usuario + contrasena hasheada con BCrypt
 * 3. Se generan access_token (24h) y refresh_token (7 dias)
 * 4. Tokens guardados en BD para poder revocarlos
 * 5. Cliente incluye access_token en header: Authorization: Bearer <token>
 * 6. JwtAuthenticationFilter valida el token en cada request protegido
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    // ============================================================
    // Registro de usuario
    // ============================================================

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("El username '" + request.getUsername() + "' ya esta en uso");
        }

        User user = User.builder()
                .username(request.getUsername())
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .age(request.getAge())
                .password(passwordEncoder.encode(request.getPassword()))  // BCrypt hash
                .role(Role.ROLE_USER)  // Por defecto ROLE_USER
                .build();

        User savedUser = userRepository.save(user);
        log.info("Usuario registrado: {}", savedUser.getUsername());

        String accessToken = jwtService.generateToken(savedUser);
        String refreshToken = jwtService.generateRefreshToken(savedUser);

        saveUserToken(savedUser, accessToken);

        return buildAuthResponse(accessToken, refreshToken, savedUser);
    }

    // ============================================================
    // Login
    // ============================================================

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        // AuthenticationManager valida credenciales con BCrypt
        // Lanza BadCredentialsException si son incorrectas
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        // Revocar todos los tokens anteriores del usuario
        revokeAllUserTokens(user);

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        saveUserToken(user, accessToken);

        log.info("Login exitoso para usuario: {}", user.getUsername());
        return buildAuthResponse(accessToken, refreshToken, user);
    }

    // ============================================================
    // Refresh Token
    // ============================================================

    @Override
    @Transactional
    public AuthResponse refreshToken(HttpServletRequest request, HttpServletResponse response) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException("Refresh token no proporcionado");
        }

        final String refreshToken = authHeader.substring(7);
        final String username = jwtService.extractUsername(refreshToken);

        if (username == null) {
            throw new BusinessException("Token invalido");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new BusinessException("Refresh token expirado o invalido");
        }

        // Revocar tokens anteriores y emitir nuevos
        revokeAllUserTokens(user);
        String newAccessToken = jwtService.generateToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);
        saveUserToken(user, newAccessToken);

        log.info("Tokens renovados para usuario: {}", user.getUsername());
        return buildAuthResponse(newAccessToken, newRefreshToken, user);
    }

    // ============================================================
    // Helpers privados
    // ============================================================

    private void saveUserToken(User user, String jwtToken) {
        Token token = Token.builder()
                .user(user)
                .token(jwtToken)
                .revoked(false)
                .expired(false)
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(User user) {
        List<Token> validTokens = tokenRepository.findAllValidTokensByUser(user.getIdUser());
        if (validTokens.isEmpty()) return;
        validTokens.forEach(t -> {
            t.setExpired(true);
            t.setRevoked(true);
        });
        tokenRepository.saveAll(validTokens);
    }

    private AuthResponse buildAuthResponse(String accessToken, String refreshToken, User user) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }
}
