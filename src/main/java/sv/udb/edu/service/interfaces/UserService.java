package sv.udb.edu.service.interfaces;

import sv.udb.edu.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    List<UserResponse> getAllUsers();
}
