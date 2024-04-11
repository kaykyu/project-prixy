package vttp.project.app.backend.repository;

import java.time.Duration;

import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import jakarta.annotation.Resource;
import vttp.project.app.backend.model.Login;

@Repository
public class AuthRepository {
    
    @Resource(name = "redisUsers")
    private ValueOperations<String, Login> userValueOps;

    public void signup(Login login) {
        userValueOps.set(login.getEmail(), login, Duration.ofHours(24));
    }

    public Login getLogin(String email) {
        return userValueOps.get(email);
    }

    public Boolean userExist(String email) {
        return userValueOps.get(email) != null;
    }

    public void putPassword(String email, String newPw) {
        Login login = userValueOps.get(email);
        login.setPw(newPw);
        userValueOps.set(email, login);
    }
}
