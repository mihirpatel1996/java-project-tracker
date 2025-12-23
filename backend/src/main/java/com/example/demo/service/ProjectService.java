package com.example.demo.service;

import com.example.demo.model.Project;
import com.example.demo.repository.ProjectRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {

    @Autowired
    ProjectRepo repo;

    public List<Project> getProjects() {
        return repo.findAll();
    }

    public Project getProjectById(int projId){
        return repo.findById(projId).orElse(new Project());
    }

    public void addProject(Project proj){
        repo.save(proj);
    }

    public void updateProject(Project proj){
        repo.save(proj);
    }

    public void deleteProject(int projId){
        repo.deleteById(projId);
    }

}
