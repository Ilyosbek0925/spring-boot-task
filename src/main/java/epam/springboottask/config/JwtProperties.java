package epam.springboottask.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

    private static final long DEFAULT_TTL_SECONDS = 3600;

    @NotBlank
    private String secretBase64;

    private long ttlSeconds = DEFAULT_TTL_SECONDS;
}
