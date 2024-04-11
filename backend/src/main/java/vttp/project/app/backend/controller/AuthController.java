package vttp.project.app.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import vttp.project.app.backend.model.Login;
import vttp.project.app.backend.model.UserRole;
import vttp.project.app.backend.service.AuthService;
import vttp.project.app.backend.service.ClientService;
import vttp.project.app.backend.service.JwtTokenService;

@RestController
@RequestMapping(path = "/api/auth")
public class AuthController {

    @Autowired
    private AuthService authSvc;

    @Autowired
    private JwtTokenService tokenSvc;

    @Autowired
    private ClientService clientSvc;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private PasswordEncoder pwEncoder;

    @PostMapping(path = "/signup")
    public ResponseEntity<String> signup(@RequestBody Login login) {
        String pw = authSvc.generateSecurePassword();
        login.setPw(pwEncoder.encode(pw));
        login.setRole(UserRole.CLIENT);
        JsonObject result = authSvc.signup(login, pw);

        if (result.isEmpty())
            return ResponseEntity.status(HttpStatusCode.valueOf(201)).body(result.toString());
        return ResponseEntity.badRequest().body(result.toString());
    }

    @PostMapping(path = "/login")
    public ResponseEntity<String> postLogin(@RequestBody Login login) {

        try {
            authSvc.loadUserByUsername(login.getEmail());
            Authentication auth = authManager
                    .authenticate(new UsernamePasswordAuthenticationToken(login.getEmail(), login.getPw()));
            return ResponseEntity.ok(tokenSvc.generateToken(auth).toString());

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Json.createObjectBuilder().add("error", e.getMessage()).build().toString());
        }
    }

    @PostMapping(path = "verify")
    public ResponseEntity<String> verifyEmail(@RequestHeader("Authorization") String token, @RequestBody Login login) {
        login.setEmail(clientSvc.getEmail(token));
        try {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(login.getEmail(), login.getPw()));

            if (!authSvc.saveClient(login.getEmail()))
                return ResponseEntity.notFound().build();

            authSvc.putPassword(login.getEmail(), pwEncoder.encode(login.getChange()));
            Authentication auth = authManager
                    .authenticate(new UsernamePasswordAuthenticationToken(login.getEmail(), login.getChange()));
            return ResponseEntity.ok(tokenSvc.generateToken(auth).toString());

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Json.createObjectBuilder().add("error", e.getMessage()).build().toString());
        }
    }

    @GetMapping(path = "/reset")
    public ResponseEntity<String> resetPassword(@RequestParam String email) {

        String pw = authSvc.generateSecurePassword();
        JsonObject result = authSvc.resetPw(email, pwEncoder.encode(pw), pw);

        if (result.isEmpty())
            return ResponseEntity.ok().build();
        return ResponseEntity.badRequest().body(result.toString());
    }

    @PutMapping(path = "/password")
    public ResponseEntity<String> putPassword(@RequestBody Login login) {
        try {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(login.getEmail(), login.getPw()));
            authSvc.putPassword(login.getEmail(), pwEncoder.encode(login.getChange()));
            return ResponseEntity.ok().build();

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Json.createObjectBuilder().add("error", e.getMessage()).build().toString());
        }
    }

    @PostMapping(path = "/kitchen")
    public ResponseEntity<String> newKitchenSignup(@RequestHeader("Authorization") String token) {
        String pw = authSvc.generateSecurePassword();
        JsonObject result = authSvc.kitchenSignup(clientSvc.getId(token), pw, pwEncoder.encode(pw));
        if (result.isEmpty())
            return ResponseEntity.internalServerError().build();
        return ResponseEntity.ok(result.toString());
    }

    @GetMapping(path = "/kitchen")
    public ResponseEntity<String> getKitchenAccount(@RequestHeader("Authorization") String token) {
        JsonObject result = authSvc.getKitchen(clientSvc.getId(token));
        if (result.isEmpty())
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result.toString());
    }
}
