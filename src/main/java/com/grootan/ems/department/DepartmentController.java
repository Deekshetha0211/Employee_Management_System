package com.grootan.ems.department;

import com.grootan.ems.department.dto.DepartmentCreateRequest;
import com.grootan.ems.department.dto.DepartmentResponse;
import com.grootan.ems.department.dto.DepartmentUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService service;
    private static final Logger log = LoggerFactory.getLogger(DepartmentController.class);

    public DepartmentController(DepartmentService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DepartmentResponse create(@Valid @RequestBody DepartmentCreateRequest req) {
        log.info("Create department request code={}", req.getCode());
        return service.create(req);
    }

    @GetMapping("/{id}")
    public DepartmentResponse get(@PathVariable Long id) {
        log.debug("Fetch department request id={}", id);
        return service.getById(id);
    }

    @GetMapping
    public List<DepartmentResponse> list() {
        log.debug("List departments request");
        return service.list();
    }

    @PutMapping("/{id}")
    public DepartmentResponse update(@PathVariable Long id, @Valid @RequestBody DepartmentUpdateRequest req) {
        log.info("Update department request id={}", id);
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        log.warn("Delete department request id={}", id);
        service.delete(id);
    }
}
