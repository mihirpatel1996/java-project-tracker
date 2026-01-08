package com.example.demo.controller;

import com.example.demo.model.Project;
import com.example.demo.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ProjectController {

    @Autowired
    ProjectService service;

    @GetMapping
    public List<Project> getAllProjects() {
        return service.getProjects();
    }

    @GetMapping("/{projId}")
    public Project getProjectById(@PathVariable int projId) {
        return service.getProjectById(projId);
    }

    @PostMapping
    public Project addProject(@RequestBody Project proj) {
        System.out.println("Received project for creation: " + proj);
        return service.addProject(proj);
    }

    @PutMapping("/{projId}")
    public Project updateProject(@PathVariable int projId, @RequestBody Project proj) {
        proj.setProjId(projId);
        return service.updateProject(proj);
    }

    @DeleteMapping("/{projId}")
    public void deleteProject(@PathVariable int projId) {
        service.deleteProject(projId);
    }
}