package vttp.project.app.backend.service;

import java.io.IOException;
import java.util.UUID;

import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import vttp.project.app.backend.model.Client;
import vttp.project.app.backend.model.Login;
import vttp.project.app.backend.model.UserPrincipal;
import vttp.project.app.backend.model.UserRole;
import vttp.project.app.backend.repository.AuthRepository;
import vttp.project.app.backend.repository.ClientRepository;

@Service
public class AuthService implements UserDetailsService {

    @Autowired
    private AuthRepository authRepo;

    @Autowired
    private ClientRepository clientRepo;

    @Autowired
    private EmailService emailSvc;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Login user = authRepo.getLogin(username);
        if (user.getPw() == null)
            throw new UsernameNotFoundException("Email does not exist!");
        return new UserPrincipal(user);
    }

    public JsonObject signup(Login login, String pw) {

        if (authRepo.userExist(login.getEmail()))
            return Json.createObjectBuilder().add("error", "Email already exists.").build();
        authRepo.signup(login);

        try {
            emailSvc.sendPw(new Client(login.getEmail(), login.getEstName()), pw, UserRole.CLIENT);
        } catch (IOException e) {
            return Json.createObjectBuilder().add("error", e.getMessage()).build();
        }
        return JsonObject.EMPTY_JSON_OBJECT;
    }

    public Boolean saveClient(String email) {
        Login login = authRepo.getLogin(email);
        return clientRepo.signUp(login, UUID.randomUUID().toString().substring(0, 8));
    }

    public JsonObject resetPw(String email, String encode, String pw) {

        if (!authRepo.userExist(email))
            return Json.createObjectBuilder().add("error", "Email does not exist").build();

        try {
            emailSvc.sendResetPw(email, pw);
            authRepo.putPassword(email, encode);
            return JsonObject.EMPTY_JSON_OBJECT;

        } catch (IOException e) {
            return Json.createObjectBuilder().add("error", e.getMessage()).build();
        }
    }

    public void putPassword(String email, String newPw) {
        authRepo.putPassword(email, newPw);
    }

    public JsonObject kitchenSignup(String id, String pw, String encoded) {
        try {
            emailSvc.sendPw(clientRepo.getClient(id), pw, UserRole.KITCHEN);
        } catch (IOException e) {
            e.printStackTrace();
            return JsonObject.EMPTY_JSON_OBJECT;
        }
        authRepo.signup(new Login(id, encoded, UserRole.KITCHEN));
        return Json.createObjectBuilder().add("username", id).build();
    }

    public JsonObject getKitchen(String id) {
        if (authRepo.userExist(id))
            return Json.createObjectBuilder().add("username", id).build();
        return JsonObject.EMPTY_JSON_OBJECT;
    }

    public String generateSecurePassword() {

        CharacterRule LCR = new CharacterRule(EnglishCharacterData.LowerCase);
        LCR.setNumberOfCharacters(3);
        CharacterRule UCR = new CharacterRule(EnglishCharacterData.UpperCase);
        UCR.setNumberOfCharacters(3);
        CharacterRule DR = new CharacterRule(EnglishCharacterData.Digit);
        DR.setNumberOfCharacters(2);

        PasswordGenerator passGen = new PasswordGenerator();
        return passGen.generatePassword(8, LCR, UCR, DR);
    }
}
