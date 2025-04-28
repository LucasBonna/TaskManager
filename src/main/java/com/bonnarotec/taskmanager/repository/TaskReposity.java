package com.bonnarotec.taskmanager.repository;

import com.bonnarotec.taskmanager.domain.task.Task;
import com.bonnarotec.taskmanager.domain.task.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskReposity extends JpaRepository<Task, UUID> {
    Optional<List<Task>> findByStatus(TaskStatus status);
}
