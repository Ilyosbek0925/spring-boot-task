package epam.springboottask.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaskRequest(
        @NotBlank @Size(max = MAX_TITLE_LENGTH) String title,
        @Size(max = MAX_DESCRIPTION_LENGTH) String description,
        boolean completed
) {

    private static final int MAX_TITLE_LENGTH = 200;
    private static final int MAX_DESCRIPTION_LENGTH = 2000;
}
