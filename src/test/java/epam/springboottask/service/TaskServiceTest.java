package epam.springboottask.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import epam.springboottask.dto.TaskRequest;
import epam.springboottask.dto.TaskResponse;
import epam.springboottask.entity.TaskItem;
import epam.springboottask.entity.UserAccount;
import epam.springboottask.exception.ResourceNotFoundException;
import epam.springboottask.mapper.TaskMapper;
import epam.springboottask.metrics.TaskMetrics;
import epam.springboottask.repository.TaskRepository;
import epam.springboottask.repository.UserAccountRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
class TaskServiceTest {

    private final TaskRepository taskRepository = mock(TaskRepository.class);
    private final UserAccountRepository userAccountRepository = mock(UserAccountRepository.class);
    private final TaskMetrics taskMetrics = mock(TaskMetrics.class);
    private final TaskService taskService =
            new TaskService(taskRepository, userAccountRepository, taskMetrics, new TaskMapper());
    private final UserAccount owner =
            UserAccount.builder().username("owner").passwordHash("encoded").role("USER").build();

    @Test
    void createsTaskForOwnerAndRecordsMetric() {
        when(userAccountRepository.findByUsername("owner")).thenReturn(Optional.of(owner));
        when(taskRepository.save(any(TaskItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse created =
                taskService.create(new TaskRequest("Write tests", "Verify task service", false), "owner");

        assertEquals("Write tests", created.title());
        assertEquals("Verify task service", created.description());
        assertEquals("owner", created.ownerUsername());
        verify(taskMetrics).recordCreated();
    }

    @Test
    void returnsNotFoundForMissingTask() {
        when(taskRepository.findByIdAndOwner_Username(42L, "owner")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.findById(42L, "owner"));
    }

    @Test
    void rejectsTaskCreationWhenOwnerDoesNotExist() {
        when(userAccountRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> taskService.create(new TaskRequest("Write tests", null, false), "missing"));
    }
}
