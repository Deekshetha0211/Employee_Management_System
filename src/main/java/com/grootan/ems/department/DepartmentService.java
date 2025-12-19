package com.grootan.ems.department;

import com.grootan.ems.department.dto.DepartmentCreateRequest;
import com.grootan.ems.department.dto.DepartmentResponse;
import com.grootan.ems.department.dto.DepartmentUpdateRequest;
import com.grootan.ems.common.ApiException;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class DepartmentService {

    private final DepartmentRepository repo;
    private static final Logger log = LoggerFactory.getLogger(DepartmentService.class);

    public DepartmentService(DepartmentRepository repo) {
        this.repo = repo;
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "departmentById", key = "#result.id", condition = "#result != null"),
            @CacheEvict(cacheNames = "departments", allEntries = true)
    })
    @Transactional
    public DepartmentResponse create(DepartmentCreateRequest req) {
        String code = req.getCode().trim().toUpperCase();
        String name = req.getName().trim();
        log.info("Creating department code={} name={}", code, name);

        if (repo.existsByCode(code)) {
            log.warn("Attempted to create duplicate department code={}", code);
            throw ApiException.conflict("department_code_exists", "Department code already exists");
        }
        if (repo.existsByName(name)) {
            log.warn("Attempted to create duplicate department name={}", name);
            throw ApiException.conflict("department_name_exists", "Department name already exists");
        }

        Department d = new Department();
        d.setCode(code);
        d.setName(name);

        Department saved = repo.save(d);
        log.info("Created department id={}", saved.getId());
        return toResponse(saved);
    }

    @Cacheable(cacheNames = "departmentById", key = "#id")
    @Transactional(readOnly = true)
    public DepartmentResponse getById(Long id) {
        log.debug("Fetching department id={}", id);
        Department d = repo.findById(id)
                .orElseThrow(() -> ApiException.notFound("department_not_found", "Department not found"));
        log.debug("Fetched department id={}", id);
        return toResponse(d);
    }

    @Cacheable(cacheNames = "departments")
    @Transactional(readOnly = true)
    public List<DepartmentResponse> list() {
        List<DepartmentResponse> departments = repo.findAll(Sort.by("name").ascending())
                .stream()
                .map(this::toResponse)
                .toList();
        log.debug("Returning {} departments", departments.size());
        return departments;
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "departmentById", key = "#id"),
            @CacheEvict(cacheNames = "departments", allEntries = true)
    })
    @Transactional
    public DepartmentResponse update(Long id, DepartmentUpdateRequest req) {
        log.info("Updating department id={}", id);
        Department d = repo.findById(id)
                .orElseThrow(() -> ApiException.notFound("department_not_found", "Department not found"));

        String newName = req.getName().trim();
        if (!newName.equals(d.getName()) && repo.existsByName(newName)) {
            log.warn("Attempted to rename department id={} to existing name={}", id, newName);
            throw ApiException.conflict("department_name_exists", "Department name already exists");
        }

        d.setName(newName);
        Department saved = repo.save(d);
        log.info("Updated department id={}", saved.getId());
        return toResponse(saved);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "departmentById", key = "#id"),
            @CacheEvict(cacheNames = "departments", allEntries = true)
    })
    @Transactional
    public void delete(Long id) {
        log.warn("Deleting department id={}", id);
        Department d = repo.findById(id)
                .orElseThrow(() -> ApiException.notFound("department_not_found", "Department not found"));

        // Optional business rule: prevent delete if employees exist
        if (d.getEmployees() != null && !d.getEmployees().isEmpty()) {
            log.warn("Cannot delete department id={} because it has {} employees", id, d.getEmployees().size());
            throw ApiException.conflict("department_has_employees", "Cannot delete department with employees");
        }

        repo.delete(d);
        log.warn("Deleted department id={}", id);
    }

    private DepartmentResponse toResponse(Department d) {
        return new DepartmentResponse(
                d.getId(),
                d.getCode(),
                d.getName(),
                d.getCreatedAt(),
                d.getUpdatedAt()
        );
    }
}
