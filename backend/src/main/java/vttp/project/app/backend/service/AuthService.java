package vttp.project.app.backend.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import vttp.project.app.backend.model.Login;
import vttp.project.app.backend.model.UserPrincipal;
import vttp.project.app.backend.repository.AuthRepository;
import vttp.project.app.backend.repository.ClientRepository;

@Service
public class AuthService implements UserDetailsService {

    @Autowired
    private AuthRepository authRepo;

    @Autowired
    private ClientRepository clientRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Login user = authRepo.getLogin(username);
        if (user.getPw() == null)
            throw new UsernameNotFoundException("Email does not exist!");
        return new UserPrincipal(user);
    }

    public JsonObject signup(Login login) {
        if (authRepo.userExist(login.getEmail()))
            return Json.createObjectBuilder().add("error", "Email already exists.").build();
        authRepo.signup(login);
        clientRepo.signUp(login, UUID.randomUUID().toString().substring(0, 8));
        return JsonObject.EMPTY_JSON_OBJECT;
    }
    
    public void putPassword(String email, String newPw) {
        authRepo.putPassword(email, newPw);
    }
}
