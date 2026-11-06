package epam.springboottask.service;

import epam.springboottask.dto.TokenResponse;
import epam.springboottask.entity.UserAccount;
import epam.springboottask.exception.DuplicateResourceException;
import epam.springboottask.jwt.JwtService;
import epam.springboottask.repository.UserAccountRepository;
import epam.springboottask.util.Roles;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public void register(String username, String password) {
        if (userAccountRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("Username is already registered");
        }
        userAccountRepository.save(UserAccount.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(password))
                .role(Roles.USER)
                .build());
    }

    public TokenResponse login(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(username, password));
        return jwtService.generateToken(authentication);
    }
}
