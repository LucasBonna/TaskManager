package com.bonnarotec.taskmanager.domain.task;

import com.bonnarotec.taskmanager.domain.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity()
@Table(name = "tasks")
@SQLDelete(sql = "UPDATE tasks SET deleted_at = CURRENT_TIMESTAMP WHERE id=?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    private LocalDateTime dueDate;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    @PrePersist
    private void setDefaults() {
        if (status == null) {
            status = TaskStatus.PENDING;
        }

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

}
