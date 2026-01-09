package com.example.demo.controller;

import com.example.demo.model.Project;
import com.example.demo.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ProjectController {

    @Autowired
    ProjectService service;

    /**
     * Get all projects (filtered by company for regular users, all for admins)
     */
    @GetMapping
    public List<Project> getAllProjects() {
        return service.getProjects();
    }

    /**
     * Get project by ID (with company authorization check)
     */
    @GetMapping("/{projId}")
    public ResponseEntity<Project> getProjectById(@PathVariable int projId) {
        Project project = service.getProjectById(projId);
        if (project.getProjId() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(project);
    }

    /**
     * Create new project (automatically assigned to user's company)
     */
    @PostMapping
    public Project addProject(@RequestBody Project proj) {
        System.out.println("Received project for creation: " + proj);
        return service.addProject(proj);
    }

    /**
     * Update project (with company authorization check)
     */
    @PutMapping("/{projId}")
    public Project updateProject(@PathVariable int projId, @RequestBody Project proj) {
        proj.setProjId(projId);
        return service.updateProject(proj);
    }

    /**
     * Delete project (with company authorization check)
     */
    @DeleteMapping("/{projId}")
    public ResponseEntity<Void> deleteProject(@PathVariable int projId) {
        service.deleteProject(projId);
        return ResponseEntity.ok().build();
    }
}