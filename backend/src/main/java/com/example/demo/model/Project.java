package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="projects")
public class Project {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer projId;
    private String projName;
    private String clientCompany;
    private String clientEmail;
    private String projType; // Drug Discovery, Clinical Trial, Manufacturing, etc.
    private String projTitle;
    private String currPhase; // Preclinical, Phase I, II, III, Approval
    private String status; // Active, On Hold, Completed, Cancelled
    private String projDetails;
    private LocalDateTime startDate;
    private LocalDateTime estCompDate;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

}
