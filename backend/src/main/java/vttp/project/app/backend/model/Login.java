package vttp.project.app.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Login {
    
    private String email;
    private String pw;
    private String estName;
    private String change;

    public Login(String email, String pw) {
        this.email = email;
        this.pw = pw;
    }
}
