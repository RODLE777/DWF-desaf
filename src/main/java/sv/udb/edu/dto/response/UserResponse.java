package sv.udb.edu.dto.response;

import sv.udb.edu.enums.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Integer idUser;
    private String username;
    private String firstname;
    private String lastname;
    private Integer age;
    private Role role;
}
