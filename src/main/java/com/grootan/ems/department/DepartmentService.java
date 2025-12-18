package com.grootan.ems.department;

import com.grootan.ems.department.dto.DepartmentCreateRequest;
import com.grootan.ems.department.dto.DepartmentResponse;
import com.grootan.ems.department.dto.DepartmentUpdateRequest;
import com.grootan.ems.common.ApiException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DepartmentService {

    private final DepartmentRepository repo;

    public DepartmentService(DepartmentRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public DepartmentResponse create(DepartmentCreateRequest req) {
        String code = req.getCode().trim().toUpperCase();
        String name = req.getName().trim();

        if (repo.existsByCode(code)) {
            throw ApiException.conflict("department_code_exists", "Department code already exists");
        }
        if (repo.existsByName(name)) {
            throw ApiException.conflict("department_name_exists", "Department name already exists");
        }

        Department d = new Department();
        d.setCode(code);
        d.setName(name);

        Department saved = repo.save(d);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public DepartmentResponse getById(Long id) {
        Department d = repo.findById(id)
                .orElseThrow(() -> ApiException.notFound("department_not_found", "Department not found"));
        return toResponse(d);
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> list() {
        return repo.findAll(Sort.by("name").ascending())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public DepartmentResponse update(Long id, DepartmentUpdateRequest req) {
        Department d = repo.findById(id)
                .orElseThrow(() -> ApiException.notFound("department_not_found", "Department not found"));

        String newName = req.getName().trim();
        if (!newName.equals(d.getName()) && repo.existsByName(newName)) {
            throw ApiException.conflict("department_name_exists", "Department name already exists");
        }

        d.setName(newName);
        Department saved = repo.save(d);
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        Department d = repo.findById(id)
                .orElseThrow(() -> ApiException.notFound("department_not_found", "Department not found"));

        // Optional business rule: prevent delete if employees exist
        if (d.getEmployees() != null && !d.getEmployees().isEmpty()) {
            throw ApiException.conflict("department_has_employees", "Cannot delete department with employees");
        }

        repo.delete(d);
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
