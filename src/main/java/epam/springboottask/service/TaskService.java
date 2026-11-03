package epam.springboottask.service;

import epam.springboottask.dto.TaskRequest;
import epam.springboottask.dto.TaskResponse;
import epam.springboottask.entity.TaskItem;
import epam.springboottask.entity.UserAccount;
import epam.springboottask.exception.ResourceNotFoundException;
import epam.springboottask.mapper.TaskMapper;
import epam.springboottask.metrics.TaskMetrics;
import epam.springboottask.repository.TaskRepository;
import epam.springboottask.repository.UserAccountRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserAccountRepository userAccountRepository;
    private final TaskMetrics taskMetrics;
    private final TaskMapper taskMapper;

    public TaskResponse create(TaskRequest request, String ownerUsername) {
        UserAccount owner = getOwner(ownerUsername);
        TaskItem task = taskRepository.save(taskMapper.toEntity(request, owner));
        taskMetrics.recordCreated();
        return taskMapper.toResponse(task);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> findAll(String ownerUsername) {
        return taskRepository.findAllByOwner_Username(ownerUsername).stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse findById(Long id, String ownerUsername) {
        return taskMapper.toResponse(getTask(id, ownerUsername));
    }

    public TaskResponse update(Long id, TaskRequest request, String ownerUsername) {
        TaskItem task = getTask(id, ownerUsername);
        TaskItem updatedTask = taskMapper.updateEntity(task, request);
        return taskMapper.toResponse(taskRepository.save(updatedTask));
    }

    public void delete(Long id, String ownerUsername) {
        if (!taskRepository.existsByIdAndOwner_Username(id, ownerUsername)) {
            throw new ResourceNotFoundException("Task not found");
        }
        taskRepository.deleteByIdAndOwner_Username(id, ownerUsername);
    }

    private TaskItem getTask(Long id, String ownerUsername) {
        return taskRepository.findByIdAndOwner_Username(id, ownerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }

    private UserAccount getOwner(String username) {
        return userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}