package sv.udb.edu.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios para JwtService.
 */
@DisplayName("JwtService - Tests Unitarios")
class JwtServiceTest {

    private static final String SECRET =
            "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    private JwtService jwtService;

    @BeforeEach
    void setUp() throws Exception {
        jwtService = new JwtService();

        setField("secretKey", SECRET);
        setField("jwtExpiration", 86400000L);
        setField("refreshExpiration", 604800000L);
    }

    private void setField(String fieldName, Object value) throws Exception {
        var field = JwtService.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(jwtService, value);
    }

    private User buildUser(String username) {
        return new User(
                username,
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    // ============================================================
    // generateToken()
    // ============================================================

    @Nested
    @DisplayName("generateToken()")
    class GenerateTokenTests {

        @Test
        @DisplayName("JWT generado tiene 3 segmentos")
        void generateToken_ReturnsValidJwt() {
            User user = buildUser("johndoe");

            String token = jwtService.generateToken(user);

            assertThat(token).isNotBlank();
            assertThat(token.split("\\.")).hasSize(3);
        }

        @Test
        @DisplayName("Subject corresponde al username")
        void generateToken_SubjectIsUsername() {
            User user = buildUser("alice");

            String token = jwtService.generateToken(user);

            assertThat(jwtService.extractUsername(token))
                    .isEqualTo("alice");
        }

        @Test
        @DisplayName("Dos tokens separados por >1 segundo son distintos")
        void generateToken_TwoTokens_AreDifferent() throws Exception {
            User user = buildUser("johndoe");

            String token1 = jwtService.generateToken(user);

            Thread.sleep(1100);

            String token2 = jwtService.generateToken(user);

            assertThat(token1).isNotEqualTo(token2);
        }

        @Test
        @DisplayName("Refresh token tambien es valido")
        void generateRefreshToken_ReturnsValidJwt() {
            User user = buildUser("johndoe");

            String token = jwtService.generateRefreshToken(user);

            assertThat(token.split("\\.")).hasSize(3);
        }
    }

    // ============================================================
    // isTokenValid()
    // ============================================================

    @Nested
    @DisplayName("isTokenValid()")
    class IsTokenValidTests {

        @Test
        @DisplayName("Token valido retorna true")
        void isTokenValid_ValidToken_ReturnsTrue() {
            User user = buildUser("johndoe");

            String token = jwtService.generateToken(user);

            assertThat(jwtService.isTokenValid(token, user))
                    .isTrue();
        }

        @Test
        @DisplayName("Token de otro usuario retorna false")
        void isTokenValid_DifferentUser_ReturnsFalse() {
            User alice = buildUser("alice");
            User bob = buildUser("bob");

            String token = jwtService.generateToken(alice);

            assertThat(jwtService.isTokenValid(token, bob))
                    .isFalse();
        }

        @Test
        @DisplayName("Token expirado retorna false")
        void isTokenValid_ExpiredToken_ReturnsFalse() {
            User user = buildUser("johndoe");

            byte[] keyBytes = Decoders.BASE64.decode(SECRET);
            SecretKey signingKey = Keys.hmacShaKeyFor(keyBytes);

            String expiredToken = Jwts.builder()
                    .subject(user.getUsername())
                    .issuedAt(new Date(System.currentTimeMillis() - 10000))
                    .expiration(new Date(System.currentTimeMillis() - 5000))
                    .signWith(signingKey)
                    .compact();

            boolean result = jwtService.isTokenValid(expiredToken, user);

            assertThat(result).isFalse();
        }
    }

    // ============================================================
    // extractUsername()
    // ============================================================

    @Nested
    @DisplayName("extractUsername()")
    class ExtractUsernameTests {

        @Test
        @DisplayName("Extrae username correctamente")
        void extractUsername_ReturnsCorrectUsername() {
            User user = buildUser("testuser");

            String token = jwtService.generateToken(user);

            assertThat(jwtService.extractUsername(token))
                    .isEqualTo("testuser");
        }
    }
}