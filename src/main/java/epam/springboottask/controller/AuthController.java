package epam.springboottask.controller;

import epam.springboottask.dto.CredentialsRequest;
import epam.springboottask.dto.TokenResponse;
import epam.springboottask.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody CredentialsRequest credentials) {
        authService.register(credentials.username(), credentials.password());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody CredentialsRequest credentials) {
        return ResponseEntity.ok(authService.login(credentials.username(), credentials.password()));
    }
}
