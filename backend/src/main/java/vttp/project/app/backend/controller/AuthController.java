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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import vttp.project.app.backend.model.Login;
import vttp.project.app.backend.model.UserPrincipal;
import vttp.project.app.backend.service.AuthService;
import vttp.project.app.backend.service.JwtTokenService;

@RestController
@RequestMapping(path = "/api/auth")
public class AuthController {

    @Autowired
    private AuthService authSvc;

    @Autowired
    private JwtTokenService tokenSvc;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private PasswordEncoder pwEncoder;

    @PostMapping(path = "/signup")
    public ResponseEntity<String> signup(@RequestBody Login login) {

        login.setPw(pwEncoder.encode(login.getPw()));
        JsonObject result = authSvc.signup(login);

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

    @PutMapping(path = "/password")
    public ResponseEntity<String> putPassword(@RequestBody Login login) {
        try {
            Authentication auth = authManager
                    .authenticate(new UsernamePasswordAuthenticationToken(login.getEmail(), login.getPw()));
            UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
            authSvc.putPassword(principal.getUsername(), pwEncoder.encode(login.getChange()));
            return ResponseEntity.ok().build();

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Json.createObjectBuilder().add("error", e.getMessage()).build().toString());
        }
    }
}
