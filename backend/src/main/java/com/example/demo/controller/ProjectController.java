package com.example.demo.controller;

import com.example.demo.model.Project;
import com.example.demo.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class ProjectController {

    @Autowired
    ProjectService service;

    @GetMapping("/projects")
    public List<Project> getAllProjects(){
        return service.getProjects();
    }

    @GetMapping("/projects/{projId}")
    public Project getProjectById(@PathVariable int projId){
        return service.getProjectById(projId);
    }

    @PostMapping("/projects")
    public Project addProject(@RequestBody Project proj){
        System.out.println("Received project for creation: " + proj);
        return service.addProject(proj);
    }

    @PutMapping("/projects/{projId}")
    public void updateProject(@RequestBody Project proj){
        service.updateProject(proj);
    }

    @DeleteMapping("/projects/{projId}")
    public void deleteProject(@PathVariable int projId){
        service.deleteProject(projId);
    }
}
