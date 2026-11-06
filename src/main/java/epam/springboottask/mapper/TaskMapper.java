package epam.springboottask.mapper;

import epam.springboottask.dto.TaskRequest;
import epam.springboottask.dto.TaskResponse;
import epam.springboottask.entity.TaskItem;
import epam.springboottask.entity.UserAccount;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public TaskItem toEntity(TaskRequest request, UserAccount owner) {
        return TaskItem.builder()
                .title(request.title())
                .description(request.description())
                .completed(request.completed())
                .owner(owner)
                .build();
    }

    public TaskItem updateEntity(TaskItem task, TaskRequest request) {
        return TaskItem.builder()
                .id(task.getId())
                .title(request.title())
                .description(request.description())
                .completed(request.completed())
                .owner(task.getOwner())
                .build();
    }

    public TaskResponse toResponse(TaskItem task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.isCompleted(),
                task.getOwner().getUsername());
    }
}
