package epam.springboottask.jwt;

import epam.springboottask.config.JwtProperties;
import epam.springboottask.dto.TokenResponse;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService {

    private static final String ISSUER = "spring-boot-task";
    private static final String ROLES_CLAIM = "roles";
    private static final String ROLE_PREFIX_REGEX = "ROLE_";
    private static final String TOKEN_TYPE = "Bearer";

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    public TokenResponse generateToken(Authentication authentication) {
        Instant now = Instant.now();
        List<String> roles = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replaceFirst(ROLE_PREFIX_REGEX, ""))
                .toList();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(jwtProperties.getTtlSeconds()))
                .subject(authentication.getName())
                .claim(ROLES_CLAIM, roles)
                .build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return new TokenResponse(token, TOKEN_TYPE, jwtProperties.getTtlSeconds());
    }
}
