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
public class EmployeeUpdateRequest {

    @NotBlank @Size(max = 120)
    private String fullName;

    @NotBlank @Email @Size(max = 180)
    private String email;

    @NotNull
    private LocalDate hireDate;

    @NotNull
    private Long departmentId;

    @NotBlank
    private String status; // must be "ACTIVE" or "INACTIVE"
}
