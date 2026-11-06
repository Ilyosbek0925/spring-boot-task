package epam.springboottask.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskMetrics {

    private static final String CREATED_TASKS_METRIC = "tasks.created";

    private final MeterRegistry meterRegistry;

    public void recordCreated() {
        meterRegistry.counter(CREATED_TASKS_METRIC).increment();
    }
}
