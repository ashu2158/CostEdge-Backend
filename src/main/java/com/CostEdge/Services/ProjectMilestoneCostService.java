package com.costedge.service;

import com.costedge.model.ProjectMilestoneCost;

import java.util.List;
import java.util.Optional;

public interface ProjectMilestoneCostService {
    ProjectMilestoneCost save(ProjectMilestoneCost cost);
    List<ProjectMilestoneCost> getAll();
    Optional<ProjectMilestoneCost> getById(Long id);
    List<ProjectMilestoneCost> getByProjectID(Integer projectID);
    List<ProjectMilestoneCost> getByProjectName(String projectName);
    void deleteById(Long id);

    List<com.costedge.model.ProjectMilestoneCost> getByApprovalStatus(String status);
}
