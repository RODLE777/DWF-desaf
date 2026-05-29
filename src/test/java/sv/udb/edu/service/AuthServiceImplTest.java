package sv.udb.edu.service;

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
import sv.udb.edu.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService - Tests Unitarios")
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private TokenRepository tokenRepository;
    @Mock private JwtService jwtService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .idUser(1)
                .username("johndoe")
                .firstname("John")
                .lastname("Doe")
                .age(25)
                .password("$2a$10$hashedPassword")
                .role(Role.ROLE_USER)
                .build();

        registerRequest = RegisterRequest.builder()
                .username("johndoe")
                .firstname("John")
                .lastname("Doe")
                .age(25)
                .password("password123")
                .build();

        loginRequest = LoginRequest.builder()
                .username("johndoe")
                .password("password123")
                .build();
    }

    // ============================================================
    // REGISTER
    // ============================================================
    @Nested
    @DisplayName("register()")
    class RegisterTests {

        @Test
        @DisplayName("Registro exitoso retorna access_token y refresh_token")
        void register_Success_ReturnsTokens() {

            when(userRepository.existsByUsername("johndoe")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hashedPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            when(jwtService.generateToken(any(User.class)))
                    .thenReturn("access.jwt.token");

            when(jwtService.generateRefreshToken(any(User.class)))
                    .thenReturn("refresh.jwt.token");

            when(tokenRepository.save(any(Token.class)))
                    .thenReturn(new Token());

            AuthResponse response = authService.register(registerRequest);

            assertThat(response).isNotNull();
            assertThat(response.getAccessToken())
                    .isEqualTo("access.jwt.token");

            assertThat(response.getRefreshToken())
                    .isEqualTo("refresh.jwt.token");

            assertThat(response.getUsername())
                    .isEqualTo("johndoe");

            assertThat(response.getRole())
                    .isEqualTo("ROLE_USER");

            verify(userRepository).save(any(User.class));
            verify(tokenRepository).save(any(Token.class));
        }

        @Test
        @DisplayName("Registro falla si el username ya existe")
        void register_UsernameAlreadyExists_ThrowsBusinessException() {

            when(userRepository.existsByUsername("johndoe"))
                    .thenReturn(true);

            assertThatThrownBy(() -> authService.register(registerRequest))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("johndoe");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("La contrasena se hashea con BCrypt antes de guardar")
        void register_PasswordIsEncodedWithBCrypt() {

            when(userRepository.existsByUsername(anyString()))
                    .thenReturn(false);

            when(passwordEncoder.encode("password123"))
                    .thenReturn("bcrypt_hashed");

            when(userRepository.save(any(User.class)))
                    .thenAnswer(inv -> {
                        User saved = inv.getArgument(0);

                        assertThat(saved.getPassword())
                                .isEqualTo("bcrypt_hashed");

                        assertThat(saved.getPassword())
                                .doesNotContain("password123");

                        return testUser;
                    });

            when(jwtService.generateToken(any()))
                    .thenReturn("token");

            when(jwtService.generateRefreshToken(any()))
                    .thenReturn("refresh");

            when(tokenRepository.save(any()))
                    .thenReturn(new Token());

            authService.register(registerRequest);

            verify(passwordEncoder).encode("password123");
        }

        @Test
        @DisplayName("El usuario registrado tiene ROLE_USER por defecto")
        void register_NewUser_HasRoleUser() {

            when(userRepository.existsByUsername(anyString()))
                    .thenReturn(false);

            when(passwordEncoder.encode(anyString()))
                    .thenReturn("hashed");

            when(userRepository.save(any(User.class)))
                    .thenAnswer(inv -> {
                        User saved = inv.getArgument(0);

                        assertThat(saved.getRole())
                                .isEqualTo(Role.ROLE_USER);

                        return testUser;
                    });

            when(jwtService.generateToken(any()))
                    .thenReturn("token");

            when(jwtService.generateRefreshToken(any()))
                    .thenReturn("refresh");

            when(tokenRepository.save(any()))
                    .thenReturn(new Token());

            authService.register(registerRequest);

            verify(userRepository)
                    .save(argThat(u ->
                            u.getRole() == Role.ROLE_USER
                    ));
        }

        @Test
        @DisplayName("Se guarda exactamente un token de acceso en BD tras el registro")
        void register_SavesExactlyOneToken() {

            when(userRepository.existsByUsername(anyString()))
                    .thenReturn(false);

            when(passwordEncoder.encode(anyString()))
                    .thenReturn("hashed");

            when(userRepository.save(any(User.class)))
                    .thenReturn(testUser);

            when(jwtService.generateToken(any()))
                    .thenReturn("access.token");

            when(jwtService.generateRefreshToken(any()))
                    .thenReturn("refresh.token");

            when(tokenRepository.save(any(Token.class)))
                    .thenReturn(new Token());

            authService.register(registerRequest);

            verify(tokenRepository, times(1))
                    .save(any(Token.class));
        }
    }

    // ============================================================
    // LOGIN
    // ============================================================
    @Nested
    @DisplayName("login()")
    class LoginTests {

        @Test
        @DisplayName("Login exitoso retorna tokens validos")
        void login_ValidCredentials_ReturnsTokens() {

            when(authenticationManager.authenticate(any()))
                    .thenReturn(
                            new UsernamePasswordAuthenticationToken(
                                    "johndoe",
                                    "password123"
                            )
                    );

            when(userRepository.findByUsername("johndoe"))
                    .thenReturn(Optional.of(testUser));

            when(tokenRepository.findAllValidTokensByUser(1))
                    .thenReturn(List.of());

            when(jwtService.generateToken(testUser))
                    .thenReturn("access.jwt.token");

            when(jwtService.generateRefreshToken(testUser))
                    .thenReturn("refresh.jwt.token");

            when(tokenRepository.save(any()))
                    .thenReturn(new Token());

            AuthResponse response = authService.login(loginRequest);

            assertThat(response.getAccessToken())
                    .isEqualTo("access.jwt.token");

            assertThat(response.getRefreshToken())
                    .isEqualTo("refresh.jwt.token");

            assertThat(response.getUsername())
                    .isEqualTo("johndoe");
        }

        @Test
        @DisplayName("Login con credenciales incorrectas lanza BadCredentialsException")
        void login_InvalidCredentials_ThrowsBadCredentialsException() {

            when(authenticationManager.authenticate(any()))
                    .thenThrow(
                            new BadCredentialsException(
                                    "Credenciales incorrectas"
                            )
                    );

            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(BadCredentialsException.class);

            verify(userRepository, never())
                    .findByUsername(anyString());
        }

        @Test
        @DisplayName("Login revoca tokens previos del usuario antes de generar nuevos")
        void login_RevokesOldTokensBeforeGeneratingNew() {

            Token oldToken = Token.builder()
                    .id(1)
                    .token("old.token")
                    .revoked(false)
                    .expired(false)
                    .user(testUser)
                    .build();

            when(authenticationManager.authenticate(any()))
                    .thenReturn(null);

            when(userRepository.findByUsername("johndoe"))
                    .thenReturn(Optional.of(testUser));

            when(tokenRepository.findAllValidTokensByUser(1))
                    .thenReturn(List.of(oldToken));

            when(jwtService.generateToken(any()))
                    .thenReturn("new.token");

            when(jwtService.generateRefreshToken(any()))
                    .thenReturn("new.refresh");

            when(tokenRepository.save(any()))
                    .thenReturn(new Token());

            authService.login(loginRequest);

            assertThat(oldToken.isRevoked()).isTrue();
            assertThat(oldToken.isExpired()).isTrue();

            verify(tokenRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("El token guardado en BD tiene revoked=false y expired=false al crearse")
        void login_NewTokenSaved_IsNotRevokedNorExpired() {

            when(authenticationManager.authenticate(any()))
                    .thenReturn(null);

            when(userRepository.findByUsername("johndoe"))
                    .thenReturn(Optional.of(testUser));

            when(tokenRepository.findAllValidTokensByUser(1))
                    .thenReturn(List.of());

            when(jwtService.generateToken(any()))
                    .thenReturn("fresh.token");

            when(jwtService.generateRefreshToken(any()))
                    .thenReturn("fresh.refresh");

            when(tokenRepository.save(any(Token.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            authService.login(loginRequest);

            verify(tokenRepository).save(argThat(t ->
                    !t.isRevoked()
                            && !t.isExpired()
                            && "fresh.token".equals(t.getToken())
            ));
        }
    }
}
