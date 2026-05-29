package sv.udb.edu.filter;

import sv.udb.edu.repository.TokenRepository;
import sv.udb.edu.security.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro JWT que se ejecuta una vez por request.
 * Extrae el token del header Authorization: Bearer <token>,
 * lo valida y autentica al usuario en el SecurityContext.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenRepository tokenRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getServletPath();

        // =========================================
        // PERMITIR SWAGGER / OPENAPI SIN JWT
        // =========================================
        if (
                path.startsWith("/swagger-ui") ||
                        path.startsWith("/v3/api-docs") ||
                        path.startsWith("/swagger-resources") ||
                        path.startsWith("/webjars")
        ) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");

        // Si no hay header Authorization o no es Bearer
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extraer token
        final String jwt = authHeader.substring(7);
        final String username;

        try {
            // Obtener username desde el token
            username = jwtService.extractUsername(jwt);

        } catch (Exception e) {

            // Token invalido o malformado
            filterChain.doFilter(request, response);
            return;
        }

        // Solo autenticar si aun no hay autenticacion
        if (
                username != null &&
                        SecurityContextHolder.getContext().getAuthentication() == null
        ) {

            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(username);

            // Validar token en base de datos
            boolean isTokenValid = tokenRepository.findByToken(jwt)
                    .map(token ->
                            !token.isExpired() &&
                                    !token.isRevoked()
                    )
                    .orElse(false);

            // Validar JWT
            if (
                    jwtService.isTokenValid(jwt, userDetails) &&
                            isTokenValid
            ) {

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                // Guardar autenticacion
                SecurityContextHolder.getContext()
                        .setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}