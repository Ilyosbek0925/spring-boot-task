package epam.springboottask.health;

import epam.springboottask.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApplicationHealthIndicator implements HealthIndicator {

    private final TaskRepository taskRepository;

    @Override
    public Health health() {
        return Health.up().withDetail("taskCount", taskRepository.count()).build();
    }
}
