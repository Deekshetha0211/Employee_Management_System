package com.grootan.ems.employee;

import com.grootan.ems.common.ApiException;
import com.grootan.ems.department.Department;
import com.grootan.ems.department.DepartmentRepository;
import com.grootan.ems.employee.dto.EmployeeCreateRequest;
import com.grootan.ems.employee.dto.EmployeeUpdateRequest;
import com.grootan.ems.user.AppUserRepository;
import com.grootan.ems.user.PasswordGenerator;
import com.grootan.ems.employee.EmployeeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepo;
    @Mock
    private DepartmentRepository deptRepo;
    @Mock
    private AppUserRepository appUserRepo;
    @Mock
    private PasswordEncoder encoder;
    @Mock
    private PasswordGenerator passwordGenerator;

    @InjectMocks
    private EmployeeService service;

    private Department department;

    @BeforeEach
    void setUp() {
        department = new Department();
        department.setId(1L);
        department.setCode("ENG");
        department.setName("Engineering");
    }

    @Test
    void create_createsEmployeeAndUser() {
        EmployeeCreateRequest req = new EmployeeCreateRequest();
        req.setFullName("Jane Doe");
        req.setEmail("jane@example.com");
        req.setEmpRole("ADMIN");
        req.setHireDate(LocalDate.of(2020, 1, 1));
        req.setDepartmentId(department.getId());
        req.setStatus("ACTIVE");

        when(employeeRepo.existsByEmail("jane@example.com")).thenReturn(false);
        when(deptRepo.findById(department.getId())).thenReturn(Optional.of(department));
        when(passwordGenerator.generate(12)).thenReturn("RawPass123");
        when(encoder.encode("RawPass123")).thenReturn("EncodedPass");
        when(employeeRepo.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee e = invocation.getArgument(0);
            e.setId(10L);
            e.setDepartment(department);
            e.setCreatedAt(OffsetDateTime.now());
            e.setUpdatedAt(OffsetDateTime.now());
            return e;
        });

        var response = service.create(req);

        assertEquals(10L, response.getId());
        assertEquals("jane@example.com", response.getEmail());
        assertEquals("RawPass123", response.getPassword());
        assertEquals("ENGINEERING", response.getDepartmentName().toUpperCase());

        verify(appUserRepo).save(any());
    }

    @Test
    void create_throwsWhenEmailExists() {
        EmployeeCreateRequest req = new EmployeeCreateRequest();
        req.setFullName("Jane Doe");
        req.setEmail("jane@example.com");
        req.setEmpRole("ADMIN");
        req.setHireDate(LocalDate.now());
        req.setDepartmentId(department.getId());

        when(employeeRepo.existsByEmail("jane@example.com")).thenReturn(true);

        assertThrows(ApiException.class, () -> service.create(req));
        verify(employeeRepo, never()).save(any());
    }

    @Test
    void getById_notFoundThrows() {
        when(employeeRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ApiException.class, () -> service.getById(99L));
    }

    @Test
    void update_updatesEmployee() {
        EmployeeUpdateRequest req = new EmployeeUpdateRequest();
        req.setFullName("Jane Updated");
        req.setEmail("jane.new@example.com");
        req.setHireDate(LocalDate.of(2021, 2, 2));
        req.setDepartmentId(department.getId());
        req.setStatus("INACTIVE");

        Employee existing = new Employee();
        existing.setId(10L);
        existing.setFullName("Old Name");
        existing.setEmail("old@example.com");
        existing.setDepartment(department);
        existing.setHireDate(LocalDate.of(2020, 1, 1));
        existing.setStatus(EmployeeStatus.ACTIVE);

        when(employeeRepo.findById(10L)).thenReturn(Optional.of(existing));
        when(employeeRepo.existsByEmailAndIdNot("jane.new@example.com", 10L)).thenReturn(false);
        when(deptRepo.findById(department.getId())).thenReturn(Optional.of(department));
        when(employeeRepo.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.update(10L, req);

        assertEquals("Jane Updated", response.getFullName());
        assertEquals("INACTIVE", response.getStatus());
        verify(employeeRepo).save(any(Employee.class));
    }

    @Test
    void update_throwsWhenDuplicateEmail() {
        EmployeeUpdateRequest req = new EmployeeUpdateRequest();
        req.setFullName("Jane Updated");
        req.setEmail("dup@example.com");
        req.setHireDate(LocalDate.now());
        req.setDepartmentId(department.getId());
        req.setStatus("ACTIVE");

        Employee existing = new Employee();
        existing.setId(10L);
        existing.setDepartment(department);

        when(employeeRepo.findById(10L)).thenReturn(Optional.of(existing));
        when(employeeRepo.existsByEmailAndIdNot("dup@example.com", 10L)).thenReturn(true);

        assertThrows(ApiException.class, () -> service.update(10L, req));
        verify(employeeRepo, never()).save(any());
    }

    @Test
    void delete_removesEmployee() {
        Employee existing = new Employee();
        existing.setId(10L);
        existing.setDepartment(department);

        when(employeeRepo.findById(10L)).thenReturn(Optional.of(existing));

        service.delete(10L);

        verify(employeeRepo).delete(existing);
    }

    @Test
    void search_returnsPagedResponses() {
        Employee e1 = employeeWith("Alice", "alice@example.com");
        Employee e2 = employeeWith("Bob", "bob@example.com");
        List<Employee> employees = List.of(e1, e2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = new PageImpl<>(employees, pageable, employees.size());

        when(employeeRepo.findAll(Mockito.<Specification<Employee>>any(), eq(pageable))).thenReturn(page);

        Page<?> results = service.search("a", 1L, "ACTIVE", pageable);

        assertThat(results.getTotalElements()).isEqualTo(2);
        assertThat(results.getContent()).extracting("fullName").containsExactly("Alice", "Bob");
    }

    private Employee employeeWith(String name, String email) {
        Employee e = new Employee();
        e.setId(1L);
        e.setFullName(name);
        e.setEmail(email);
        e.setDepartment(department);
        e.setStatus(EmployeeStatus.ACTIVE);
        e.setHireDate(LocalDate.now());
        e.setCreatedAt(OffsetDateTime.now());
        e.setUpdatedAt(OffsetDateTime.now());
        return e;
    }
}
