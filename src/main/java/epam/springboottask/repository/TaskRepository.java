package epam.springboottask.repository;

import epam.springboottask.entity.TaskItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<TaskItem, Long> {

    List<TaskItem> findAllByOwner_Username(String username);

    Optional<TaskItem> findByIdAndOwner_Username(Long id, String username);

    boolean existsByIdAndOwner_Username(Long id, String username);

    void deleteByIdAndOwner_Username(Long id, String username);
}
