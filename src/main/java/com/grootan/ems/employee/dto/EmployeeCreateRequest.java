package com.grootan.ems.employee.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EmployeeCreateRequest {

    @NotBlank @Size(max = 120)
    private String fullName;

    @NotBlank @Email @Size(max = 180)
    private String email;

    @NotBlank
    private String empRole;

    @NotNull
    private LocalDate hireDate;

    @NotNull
    private Long departmentId;

    // optional: if not provided, default ACTIVE in entity
    private String status; // "ACTIVE" or "INACTIVE"
}
