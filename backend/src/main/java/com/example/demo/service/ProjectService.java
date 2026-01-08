package com.example.demo.service;

import com.example.demo.model.Project;
import com.example.demo.model.User;
import com.example.demo.repository.ProjectRepo;
import com.example.demo.repository.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProjectService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);

    @Autowired
    ProjectRepo repo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    EmailService emailService;

    /**
     * Get projects based on user role:
     * - ADMIN: sees all projects
     * - USER: sees only their company's projects (by clientCompany)
     */
    public List<Project> getProjects() {
        User currentUser = getCurrentUser();

        if (currentUser.isAdmin()) {
            logger.info("Admin user {} fetching all projects", currentUser.getEmail());
            return repo.findAll();
        } else {
            logger.info("User {} fetching projects for company: {}",
                    currentUser.getEmail(), currentUser.getCompanyName());
            return repo.findByClientCompanyIgnoreCase(currentUser.getCompanyName());
        }
    }

    /**
     * Get project by ID with company verification:
     * - ADMIN: can access any project
     * - USER: can only access their company's projects
     */
    public Project getProjectById(int projId) {
        User currentUser = getCurrentUser();
        Project project = repo.findById(projId).orElse(null);

        if (project == null) {
            return new Project();
        }

        // Admin can access any project
        if (currentUser.isAdmin()) {
            return project;
        }

        // Regular user can only access their company's projects
        if (project.getClientCompany() != null &&
                project.getClientCompany().equalsIgnoreCase(currentUser.getCompanyName())) {
            return project;
        }

        logger.warn("User {} attempted to access project {} belonging to different company",
                currentUser.getEmail(), projId);
        return new Project(); // Return empty project if not authorized
    }

    /**
     * Add project with the current user's company as clientCompany,
     * user's email as clientEmail, and set createdBy.
     * Email notifications are enabled by default.
     */
    public Project addProject(Project proj) {
        User currentUser = getCurrentUser();

        // Set the clientCompany to the current user's company
        proj.setClientCompany(currentUser.getCompanyName());
        // Set the clientEmail to the current user's email
        proj.setClientEmail(currentUser.getEmail());
        // Set who created the project
        proj.setCreatedBy(currentUser.getId());
        // Enable email notifications by default
        proj.setEmailNotifications(true);
        proj.setCreatedDate(LocalDateTime.now());
        proj.setUpdatedDate(LocalDateTime.now());

        logger.info("User {} (ID: {}) creating project for company: {} with email notifications enabled",
                currentUser.getEmail(), currentUser.getId(), currentUser.getCompanyName());

        return repo.save(proj);
    }

    /**
     * Update project with company verification:
     * - ADMIN: can update any project
     * - USER: can only update their company's projects
     * Also sends email notification if status changed and notifications are enabled.
     */
    public Project updateProject(Project proj) {
        User currentUser = getCurrentUser();
        Project existingProject = repo.findById(proj.getProjId()).orElse(null);

        if (existingProject == null) {
            logger.warn("Project {} not found for update", proj.getProjId());
            return proj;
        }

        // Check authorization
        if (!currentUser.isAdmin() &&
                (existingProject.getClientCompany() == null ||
                        !existingProject.getClientCompany().equalsIgnoreCase(currentUser.getCompanyName()))) {
            logger.warn("User {} attempted to update project {} belonging to different company",
                    currentUser.getEmail(), proj.getProjId());
            return existingProject; // Return existing project without changes
        }

        // Check if status changed and email notifications are enabled
        String oldStatus = existingProject.getStatus();
        String newStatus = proj.getStatus();
        boolean statusChanged = oldStatus != null && newStatus != null && !oldStatus.equals(newStatus);

        // Use the new emailNotifications value from the update request,
        // or fall back to existing if not provided
        Boolean emailNotificationsEnabled = proj.getEmailNotifications() != null
                ? proj.getEmailNotifications()
                : existingProject.getEmailNotifications();

        // Preserve the original clientCompany, clientEmail, and createdBy
        proj.setClientCompany(existingProject.getClientCompany());
        proj.setClientEmail(existingProject.getClientEmail());
        proj.setCreatedBy(existingProject.getCreatedBy());
        proj.setCreatedDate(existingProject.getCreatedDate());
        proj.setUpdatedDate(LocalDateTime.now());

        logger.info("User {} updating project {}", currentUser.getEmail(), proj.getProjId());

        Project savedProject = repo.save(proj);

        // Send email notification if status changed and notifications are enabled
        if (statusChanged && Boolean.TRUE.equals(emailNotificationsEnabled)) {
            String clientEmail = existingProject.getClientEmail();
            String projectName = existingProject.getProjName();

            if (clientEmail != null && !clientEmail.isEmpty()) {
                logger.info("Status changed from '{}' to '{}' for project '{}'. Sending notification to {}",
                        oldStatus, newStatus, projectName, clientEmail);
                emailService.sendStatusUpdateEmail(clientEmail, projectName, oldStatus, newStatus);
            }
        }

        return savedProject;
    }

    /**
     * Delete project with company verification:
     * - ADMIN: can delete any project
     * - USER: can only delete their company's projects
     */
    public void deleteProject(int projId) {
        User currentUser = getCurrentUser();
        Project project = repo.findById(projId).orElse(null);

        if (project == null) {
            logger.warn("Project {} not found for deletion", projId);
            return;
        }

        // Check authorization
        if (!currentUser.isAdmin() &&
                (project.getClientCompany() == null ||
                        !project.getClientCompany().equalsIgnoreCase(currentUser.getCompanyName()))) {
            logger.warn("User {} attempted to delete project {} belonging to different company",
                    currentUser.getEmail(), projId);
            return; // Don't delete if not authorized
        }

        logger.info("User {} deleting project {}", currentUser.getEmail(), projId);
        repo.deleteById(projId);
    }

    /**
     * Get the currently authenticated user
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }
}