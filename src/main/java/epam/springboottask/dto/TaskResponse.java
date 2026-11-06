package epam.springboottask.dto;

public record TaskResponse(Long id, String title, String description, boolean completed, String ownerUsername) {
}
