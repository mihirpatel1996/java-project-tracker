package com.example.demo.controller;

import com.example.demo.model.Project;
import com.example.demo.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class ProjectController {

    @Autowired
    ProjectService service;

    @GetMapping("/projects")
    public List<Project> getAllProjects(){
        return service.getProjects();
    }

    @GetMapping("/project/{id}")
    public Project getProjectById(@PathVariable int projId){
        return service.getProjectById(projId);
    }

    @PostMapping("/project")
    public void addProject(@RequestBody Project proj){
        service.addProject(proj);
    }

    @PutMapping("/project/{id}")
    public void updateProject(@RequestBody Project proj){
        service.updateProject(proj);
    }

    @DeleteMapping("/projects/{id}")
    public void deleteProject(@RequestParam int projId){
        service.deleteProject(projId);
    }
}
