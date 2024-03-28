package vttp.project.app.backend.repository;

import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import jakarta.annotation.Resource;
import vttp.project.app.backend.model.Login;

@Repository
public class AuthRepository {
    
    @Resource(name = "redisUsers")
    private ValueOperations<String, String> userValueOps;

    public void signup(Login login) {
        userValueOps.set(login.getEmail(), login.getPw());
    }

    public Login getLogin(String email) {
        return new Login(email, userValueOps.get(email));
    }

    public Boolean userExist(String email) {
        return userValueOps.get(email) != null;
    }

    public void putPassword(String email, String newPw) {
        userValueOps.set(email, newPw);
    }
}
