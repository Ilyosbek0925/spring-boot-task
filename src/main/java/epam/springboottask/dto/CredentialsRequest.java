package epam.springboottask.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CredentialsRequest(
        @NotBlank @Size(max = MAX_USERNAME_LENGTH) String username,
        @NotBlank @Size(min = MIN_PASSWORD_LENGTH, max = MAX_PASSWORD_LENGTH) String password) {

    private static final int MAX_USERNAME_LENGTH = 80;
    private static final int MIN_PASSWORD_LENGTH = 12;
    private static final int MAX_PASSWORD_LENGTH = 72;
}
