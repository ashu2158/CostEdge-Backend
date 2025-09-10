package com.costedge.service.impl;

import com.costedge.model.ProjectMilestoneCost;
import com.costedge.repository.ProjectMilestoneCostRepository;
import com.costedge.service.ProjectMilestoneCostService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectMilestoneCostServiceImpl implements ProjectMilestoneCostService {

    private final ProjectMilestoneCostRepository repository;

    public ProjectMilestoneCostServiceImpl(ProjectMilestoneCostRepository repository) {
        this.repository = repository;
    }

    @Override
    public ProjectMilestoneCost save(ProjectMilestoneCost cost) {
        return repository.save(cost);
    }

    @Override
    public List<ProjectMilestoneCost> getAll() {
        return repository.findAll();
    }

    @Override
    public Optional<ProjectMilestoneCost> getById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<ProjectMilestoneCost> getByProjectID(Integer projectID) {
        return repository.findByProjectID(projectID);
    }

    @Override
    public List<ProjectMilestoneCost> getByProjectName(String projectName) {
        return repository.findByProjectName(projectName);
    }

    @Override
    public List<ProjectMilestoneCost> getByApprovalStatus(String status) {
        return repository.findByApprovalStatus(status);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
