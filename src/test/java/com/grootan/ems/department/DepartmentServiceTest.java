package com.grootan.ems.department;

import com.grootan.ems.common.ApiException;
import com.grootan.ems.department.dto.DepartmentCreateRequest;
import com.grootan.ems.department.dto.DepartmentUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository repository;

    @InjectMocks
    private DepartmentService service;

    private Department department;

    @BeforeEach
    void setUp() {
        department = new Department();
        department.setId(1L);
        department.setCode("ENG");
        department.setName("Engineering");
        department.setCreatedAt(OffsetDateTime.now());
        department.setUpdatedAt(OffsetDateTime.now());
    }

    @Test
    void create_succeeds() {
        DepartmentCreateRequest req = new DepartmentCreateRequest();
        req.setCode("ENG");
        req.setName("Engineering");

        when(repository.existsByCode("ENG")).thenReturn(false);
        when(repository.existsByName("Engineering")).thenReturn(false);
        when(repository.save(any(Department.class))).thenAnswer(invocation -> {
            Department d = invocation.getArgument(0);
            d.setId(2L);
            return d;
        });

        var response = service.create(req);

        assertEquals(2L, response.getId());
        assertEquals("ENG", response.getCode());
        verify(repository).save(any(Department.class));
    }

    @Test
    void create_throwsOnDuplicateCode() {
        DepartmentCreateRequest req = new DepartmentCreateRequest();
        req.setCode("ENG");
        req.setName("Engineering");

        when(repository.existsByCode("ENG")).thenReturn(true);

        assertThrows(ApiException.class, () -> service.create(req));
        verify(repository, never()).save(any());
    }

    @Test
    void getById_returnsDepartment() {
        when(repository.findById(1L)).thenReturn(Optional.of(department));

        var response = service.getById(1L);

        assertEquals("Engineering", response.getName());
    }

    @Test
    void getById_notFoundThrows() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ApiException.class, () -> service.getById(99L));
    }

    @Test
    void list_returnsAllDepartments() {
        when(repository.findAll(Sort.by("name").ascending())).thenReturn(List.of(department));

        var results = service.list();

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getCode()).isEqualTo("ENG");
    }

    @Test
    void update_throwsOnDuplicateName() {
        DepartmentUpdateRequest req = new DepartmentUpdateRequest();
        req.setName("New Name");

        when(repository.findById(1L)).thenReturn(Optional.of(department));
        when(repository.existsByName("New Name")).thenReturn(true);

        assertThrows(ApiException.class, () -> service.update(1L, req));
    }

    @Test
    void delete_throwsWhenHasEmployees() {
        Department deptWithEmployees = new Department();
        deptWithEmployees.setId(5L);
        deptWithEmployees.setCode("HR");
        deptWithEmployees.setName("HR");
        deptWithEmployees.getEmployees().add(null);

        when(repository.findById(5L)).thenReturn(Optional.of(deptWithEmployees));

        assertThrows(ApiException.class, () -> service.delete(5L));
        verify(repository, never()).delete(any());
    }

    @Test
    void delete_removesDepartment() {
        when(repository.findById(1L)).thenReturn(Optional.of(department));

        service.delete(1L);

        verify(repository).delete(department);
    }
}
