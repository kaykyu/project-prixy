package vttp.project.app.backend.model;

import java.io.Serializable;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Login implements Serializable {
    
    @NotEmpty(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotEmpty(message = "Password is required")
    @Pattern(regexp = "^(?=.*\\\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$", message = "Password must meet requirements")
    private String pw;

    @NotEmpty(message = "Establishment name is required")
    @Size(min = 3, max = 20, message = "Establishment must be between 3 - 20 characters")
    private String estName;

    @Pattern(regexp = "^(?=.*\\\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$", message = "Password must meet requirements")
    private String change;

    private UserRole role;

    public Login(String email, String pw, UserRole role) {
        this.email = email;
        this.pw = pw;
        this.role = role;
    }
}
