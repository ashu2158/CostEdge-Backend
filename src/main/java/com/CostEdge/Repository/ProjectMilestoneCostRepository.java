package com.costedge.repository;

import com.costedge.model.ProjectMilestoneCost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectMilestoneCostRepository extends JpaRepository<ProjectMilestoneCost, Long> {

    List<ProjectMilestoneCost> findByProjectID(Integer projectID);
    List<ProjectMilestoneCost> findByProjectName(String projectName);
    List<ProjectMilestoneCost> findByMilestone(String milestone);
}
