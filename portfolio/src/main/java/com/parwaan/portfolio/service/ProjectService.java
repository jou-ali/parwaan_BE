package com.parwaan.portfolio.service;

import com.parwaan.portfolio.dto.ProjectDto;
import com.parwaan.portfolio.model.Project;
import com.parwaan.portfolio.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Project> getProjectById(UUID id) {
        return projectRepository.findById(id);
    }

    @Transactional
    public Project createProject(ProjectDto projectDto) {
        Project newProject = Project.builder()
                .title(projectDto.getTitle())
                .description(projectDto.getDescription())
                .repoUrl(projectDto.getRepoUrl())
                .liveUrl(projectDto.getLiveUrl())
                .featured(projectDto.isFeatured())
                .build();
        return projectRepository.save(newProject);
    }

    @Transactional
    public Optional<Project> updateProject(UUID id, ProjectDto projectDto) {
        return projectRepository.findById(id)
                .map(existingProject -> {
                    existingProject.setTitle(projectDto.getTitle());
                    existingProject.setDescription(projectDto.getDescription());
                    existingProject.setRepoUrl(projectDto.getRepoUrl());
                    existingProject.setLiveUrl(projectDto.getLiveUrl());
                    existingProject.setFeatured(projectDto.isFeatured());
                    return projectRepository.save(existingProject);
                });
    }

    @Transactional
    public void deleteProject(UUID id) {
        projectRepository.deleteById(id);
    }
}