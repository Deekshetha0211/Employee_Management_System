package com.grootan.ems.employee;

import com.grootan.ems.common.ApiException;
import com.grootan.ems.department.Department;
import com.grootan.ems.department.DepartmentRepository;
import com.grootan.ems.employee.dto.EmployeeCreateRequest;
import com.grootan.ems.employee.dto.EmployeeResponse;
import com.grootan.ems.employee.dto.EmployeeUpdateRequest;
import com.grootan.ems.user.AppUser;
import com.grootan.ems.user.AppUserRepository;
import com.grootan.ems.user.PasswordGenerator;
import com.grootan.ems.user.Role;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepo;
    private final DepartmentRepository deptRepo;
    private final AppUserRepository appUserRepo;
    private final PasswordEncoder encoder;
    private final PasswordGenerator passwordGenerator;
    private static final Logger log = LoggerFactory.getLogger(EmployeeService.class);

    public EmployeeService(EmployeeRepository employeeRepo, DepartmentRepository deptRepo, AppUserRepository appUserRepo, PasswordEncoder encoder, PasswordGenerator passwordGenerator) {
        this.employeeRepo = employeeRepo;
        this.deptRepo = deptRepo;
        this.appUserRepo = appUserRepo;
        this.encoder = encoder;
        this.passwordGenerator = passwordGenerator;
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "employeeById", key = "#result.id", condition = "#result != null"),
            @CacheEvict(cacheNames = "employeeSearch", allEntries = true)
    })
    @Transactional
    public EmployeeResponse create(EmployeeCreateRequest req) {
        String fullName = req.getFullName().trim();
        String email = req.getEmail().trim().toLowerCase();
        String empRole = req.getEmpRole().trim().toUpperCase();
        Role userRole = Role.valueOf(empRole);
        log.info("Creating employee fullName={} email={} role={}", fullName, email, userRole);

        if (employeeRepo.existsByEmail(email)) {
            log.warn("Attempted to create employee with duplicate email {}", email);
            throw ApiException.conflict("employee_email_exists", "Employee email already exists");
        }

        Department dept = deptRepo.findById(req.getDepartmentId())
                .orElseThrow(() -> ApiException.notFound("department_not_found", "Department not found"));

        Employee e = new Employee();
        e.setFullName(fullName);
        e.setEmail(email);
        e.setEmpRole(empRole);
        e.setHireDate(req.getHireDate());
        e.setDepartment(dept);

        if (req.getStatus() != null && !req.getStatus().isBlank()) {
            e.setStatus(parseStatus(req.getStatus()));
        }

        Employee saved = employeeRepo.save(e);

        AppUser u = new AppUser();
        u.setEmail(email);
        String password = passwordGenerator.generate(12);
        u.setPasswordHash(encoder.encode(password));
        u.setRole(userRole);
        u.setEnabled(true);
        u.setEmployee(saved);

        appUserRepo.save(u);
        log.info("Created employee id={} and linked user account", saved.getId());
        return toResponse(saved,password);
    }

    @Cacheable(cacheNames = "employeeById", key = "#id")
    @Transactional(readOnly = true)
    public EmployeeResponse getById(Long id) {
        log.debug("Fetching employee id={}", id);
        Employee e = employeeRepo.findById(id)
                .orElseThrow(() -> ApiException.notFound("employee_not_found", "Employee not found"));
        log.debug("Employee id={} fetched successfully", id);
        return toResponse(e);
    }

    @Cacheable(
            cacheNames = "employeeSearch",
            key = "T(java.lang.String).format('q=%s|dept=%s|st=%s|p=%d|s=%d', " +
                    "(#q == null ? '' : #q.toLowerCase()), " +
                    "(#departmentId == null ? '' : #departmentId), " +
                    "(#status == null ? '' : #status.toUpperCase()), " +
                    "#pageable.pageNumber, #pageable.pageSize)"
    )
    @Transactional(readOnly = true)
    public Page<EmployeeResponse> search(String q, Long departmentId, String status,
                                         Pageable pageable) {
        log.debug("Searching employees with q={} departmentId={} status={}", q, departmentId, status);
        EmployeeStatus st = (status == null || status.isBlank()) ? null : parseStatus(status);

        Specification<Employee> spec =
                EmployeeSpecs.nameContains(q)
                        .and(EmployeeSpecs.departmentIdEquals(departmentId))
                        .and(EmployeeSpecs.statusEquals(st));

        Page<EmployeeResponse> results = employeeRepo.findAll(spec, pageable).map(this::toResponse);
        log.debug("Employee search returned {} results", results.getTotalElements());
        return results;
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "employeeById", key = "#id"),
            @CacheEvict(cacheNames = "employeeSearch", allEntries = true)
    })
    @Transactional
    public EmployeeResponse update(Long id, EmployeeUpdateRequest req) {
        log.info("Updating employee id={}", id);
        Employee e = employeeRepo.findById(id)
                .orElseThrow(() -> ApiException.notFound("employee_not_found", "Employee not found"));

        String fullName = req.getFullName().trim();
        String email = req.getEmail().trim().toLowerCase();

        if (employeeRepo.existsByEmailAndIdNot(email, id)) {
            log.warn("Attempted to update employee id={} with duplicate email {}", id, email);
            throw ApiException.conflict("employee_email_exists", "Employee email already exists");
        }

        Department dept = deptRepo.findById(req.getDepartmentId())
                .orElseThrow(() -> ApiException.notFound("department_not_found", "Department not found"));

        e.setFullName(fullName);
        e.setEmail(email);
        e.setHireDate(req.getHireDate());
        if (req.getStatus() != null && !req.getStatus().isBlank()) {
            e.setStatus(parseStatus(req.getStatus()));
        }
        e.setDepartment(dept);

        Employee saved = employeeRepo.save(e);
        log.info("Updated employee id={}", saved.getId());
        return toResponse(saved);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "employeeById", key = "#id"),
            @CacheEvict(cacheNames = "employeeSearch", allEntries = true)
    })
    @Transactional
    public void delete(Long id) {
        log.warn("Deleting employee id={}", id);
        Employee e = employeeRepo.findById(id)
                .orElseThrow(() -> ApiException.notFound("employee_not_found", "Employee not found"));
        employeeRepo.delete(e);
        log.warn("Deleted employee id={}", id);
    }

    private EmployeeStatus parseStatus(String s) {
        try {
            return EmployeeStatus.valueOf(s.trim().toUpperCase());
        } catch (Exception ex) {
            log.warn("Invalid employee status provided: {}", s);
            throw ApiException.conflict("invalid_status", "Status must be ACTIVE or INACTIVE");
        }
    }

    private EmployeeResponse toResponse(Employee e) {
        // NOTE: department is LAZY, but in a transaction it’s fine to read its fields.
        Department d = e.getDepartment();
        return new EmployeeResponse(
                e.getId(),
                e.getFullName(),
                e.getEmail(),
                e.getHireDate(),
                e.getStatus() == null ? null : e.getStatus().name(),
                d.getId(),
                d.getCode(),
                d.getName(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }

    private EmployeeResponse toResponse(Employee e, String password) {
        // NOTE: department is LAZY, but in a transaction it’s fine to read its fields.
        Department d = e.getDepartment();
        return new EmployeeResponse(
                e.getId(),
                e.getFullName(),
                e.getEmail(),
                password,
                e.getHireDate(),
                e.getStatus() == null ? null : e.getStatus().name(),
                d.getId(),
                d.getCode(),
                d.getName(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}
