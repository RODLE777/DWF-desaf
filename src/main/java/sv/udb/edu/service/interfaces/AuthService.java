package sv.udb.edu.service.interfaces;

import sv.udb.edu.dto.request.LoginRequest;
import sv.udb.edu.dto.request.RegisterRequest;
import sv.udb.edu.dto.response.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(HttpServletRequest request, HttpServletResponse response);
}