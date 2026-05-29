package sv.udb.edu.service.impl;

import sv.udb.edu.dto.response.UserResponse;
import sv.udb.edu.repository.UserRepository;
import sv.udb.edu.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> UserResponse.builder()
                        .idUser(user.getIdUser())
                        .username(user.getUsername())
                        .firstname(user.getFirstname())
                        .lastname(user.getLastname())
                        .age(user.getAge())
                        .role(user.getRole())
                        .build())
                .toList();
    }
}

