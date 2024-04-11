package vttp.project.app.backend.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import vttp.project.app.backend.model.UserPrincipal;
import vttp.project.app.backend.model.UserRole;
import vttp.project.app.backend.repository.ClientRepository;

@Service
public class JwtTokenService {

    @Value("${jwt.secret.key}")
    private String secretKey;

    @Autowired
    private ClientRepository clientRepo;

    public JsonObject generateToken(Authentication auth) {

        UserPrincipal client = (UserPrincipal) auth.getPrincipal();
        String id = client.getUsername();

        if (client.getUser().getRole().equals(UserRole.CLIENT))
           id = clientRepo.getClientId(client.getUsername());
        
        Instant now = Instant.now();
        return Json.createObjectBuilder()
                .add("token", JWT.create()
                        .withIssuer("Prixy")
                        .withIssuedAt(now)
                        .withSubject(id)
                        .withExpiresAt(now.plus(8, ChronoUnit.HOURS))
                        .withClaim("email", client.getUsername())
                        .withClaim("role", client.getUser().getRole().toString())
                        .sign(Algorithm.HMAC256(secretKey)))
                .build();
    }

    public JsonObject generateOrderLink(String client, String table) {
        
        Instant now = Instant.now();
        return Json.createObjectBuilder()
                .add("token", JWT.create()
                        .withIssuer("Prixy")
                        .withIssuedAt(now)
                        .withSubject(client)
                        .withExpiresAt(now.plus(4, ChronoUnit.HOURS))
                        .withClaim("client", clientRepo.getClient(client).getEstName())
                        .withClaim("table", table)
                        .sign(Algorithm.HMAC256(secretKey))
                        .split("\\.")[1])                        
                .build();
    }
}
