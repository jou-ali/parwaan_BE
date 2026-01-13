package com.parwaan.portfolio.controller;

import com.parwaan.portfolio.dto.ProjectDto;
import com.parwaan.portfolio.model.Project;
import com.parwaan.portfolio.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "http://localhost:3000")
public class ProjectController {
    private final ProjectService projectService;
    public ProjectController(ProjectService projectService) { this.projectService = projectService; }

    @GetMapping
    public List<Project> all() { return projectService.getAllProjects(); }

    @GetMapping("/{id}")
    public ResponseEntity<Project> get(@PathVariable UUID id) {
        
        return projectService.getProjectById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Project> create(@Valid @RequestBody ProjectDto dto) {
        Project saved = projectService.createProject(dto);
        return ResponseEntity.created(URI.create("/api/projects/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Project> update(@PathVariable UUID id, @Valid @RequestBody ProjectDto dto) {
        return projectService.updateProject(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}
