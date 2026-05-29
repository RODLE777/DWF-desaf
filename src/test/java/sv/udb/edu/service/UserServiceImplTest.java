package sv.udb.edu.service;


import sv.udb.edu.dto.response.UserResponse;
import sv.udb.edu.entity.User;
import sv.udb.edu.enums.Role;
import sv.udb.edu.repository.UserRepository;
import sv.udb.edu.service.impl.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService - Tests Unitarios")
class UserServiceImplTest {

    @Mock  private UserRepository   userRepository;
    @InjectMocks private UserServiceImpl userService;

    @Test
    @DisplayName("getAllUsers retorna lista mapeada correctamente a UserResponse")
    void getAllUsers_ReturnsMappedList() {
        User u1 = User.builder().idUser(1).username("alice").firstname("Alice")
                .lastname("Smith").age(25).password("hashed").role(Role.ROLE_USER).build();
        User u2 = User.builder().idUser(2).username("bob").firstname("Bob")
                .lastname("Jones").age(30).password("hashed").role(Role.ROLE_ADMIN).build();

        when(userRepository.findAll()).thenReturn(List.of(u1, u2));

        List<UserResponse> result = userService.getAllUsers();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUsername()).isEqualTo("alice");
        assertThat(result.get(0).getRole()).isEqualTo(Role.ROLE_USER);
        assertThat(result.get(1).getUsername()).isEqualTo("bob");
        assertThat(result.get(1).getRole()).isEqualTo(Role.ROLE_ADMIN);
    }

    @Test
    @DisplayName("getAllUsers retorna lista vacia si no hay usuarios en BD")
    void getAllUsers_EmptyRepository_ReturnsEmptyList() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserResponse> result = userService.getAllUsers();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("UserResponse NO expone el campo password (no existe en el DTO por diseno)")
    void getAllUsers_UserResponse_DoesNotExposePassword() {
        User u = User.builder().idUser(1).username("alice").firstname("Alice")
                .lastname("Smith").age(25).password("$2a$10$secretHash").role(Role.ROLE_USER).build();

        when(userRepository.findAll()).thenReturn(List.of(u));

        List<UserResponse> result = userService.getAllUsers();

        // UserResponse no tiene campo password por diseno de seguridad.
        // Verificamos que el mapeo incluye los campos esperados
        UserResponse r = result.get(0);
        assertThat(r.getIdUser()).isEqualTo(1);
        assertThat(r.getUsername()).isEqualTo("alice");
        assertThat(r.getFirstname()).isEqualTo("Alice");
        assertThat(r.getLastname()).isEqualTo("Smith");
        assertThat(r.getAge()).isEqualTo(25);
        assertThat(r.getRole()).isEqualTo(Role.ROLE_USER);
    }

    @Test
    @DisplayName("Se llama a findAll exactamente una vez en getAllUsers")
    void getAllUsers_CallsFindAllOnce() {
        when(userRepository.findAll()).thenReturn(List.of());

        userService.getAllUsers();

        verify(userRepository, times(1)).findAll();
    }
}
