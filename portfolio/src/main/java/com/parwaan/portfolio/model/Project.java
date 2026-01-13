package com.parwaan.portfolio.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "projects")
@Getter
@Setter
@ToString
@NoArgsConstructor         // generates empty constructor
@AllArgsConstructor        // generates constructor with all fields
@Builder                   // enables Project.builder()
public class Project {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(length = 4000)
    private String description;

    private String repoUrl;
    private String liveUrl;

    @Builder.Default
    private boolean featured = false;

    private Instant createdAt;

    @PrePersist
    private void onCreate() {
        createdAt = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return id != null && Objects.equals(id, project.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}