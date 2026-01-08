package com.example.demo.repository;

import com.example.demo.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepo extends JpaRepository<Project, Integer> {

    // Find all projects belonging to a specific company (by clientCompany)
    List<Project> findByClientCompany(String clientCompany);

    // Find all projects belonging to a specific company, case-insensitive
    List<Project> findByClientCompanyIgnoreCase(String clientCompany);

    // Find all projects created by a specific user
    List<Project> findByCreatedBy(Long userId);
}