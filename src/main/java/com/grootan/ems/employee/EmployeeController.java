package com.grootan.ems.employee;

import com.grootan.ems.employee.dto.EmployeeCreateRequest;
import com.grootan.ems.employee.dto.EmployeeResponse;
import com.grootan.ems.employee.dto.EmployeeUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService service;
    private static final Logger log = LoggerFactory.getLogger(EmployeeController.class);

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeResponse create(@Valid @RequestBody EmployeeCreateRequest req) {
        log.info("Create employee request received for email {}", req.getEmail());
        return service.create(req);
    }


    @GetMapping("/{id}")
    public EmployeeResponse get(@PathVariable Long id) {
        log.debug("Fetch employee request id={}", id);
        return service.getById(id);
    }

    /**
     * Search & filter + pagination:
     *   /api/employees?q=ali&departmentId=1&status=ACTIVE&page=0&size=10&sort=fullName,asc
     */

    @GetMapping
    public Page<EmployeeResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String status,
            @PageableDefault(
                    size = 10,
                    sort = "fullName",
                    direction = Sort.Direction.ASC
            ) Pageable pageable
    ) {
        log.debug("Search employees q={} departmentId={} status={} page={} size={}", q, departmentId, status, pageable.getPageNumber(), pageable.getPageSize());
        return service.search(q, departmentId, status, pageable);
    }

    @PutMapping("/{id}")
    public EmployeeResponse update(@PathVariable Long id, @Valid @RequestBody EmployeeUpdateRequest req) {
        log.info("Update employee request id={} email={}", id, req.getEmail());
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        log.warn("Delete employee request id={}", id);
        service.delete(id);
    }
}
