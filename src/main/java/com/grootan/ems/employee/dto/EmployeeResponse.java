package com.grootan.ems.employee.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
public class EmployeeResponse {

    private Long id;
    private String fullName;
    private String email;
    private LocalDate hireDate;
    private String status;

    private Long departmentId;
    private String departmentCode;
    private String departmentName;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String password;

    public EmployeeResponse(Long id, String fullName, String email,LocalDate hireDate, String status,
                            Long departmentId, String departmentCode, String departmentName,
                            OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.hireDate = hireDate;
        this.status = status;
        this.departmentId = departmentId;
        this.departmentCode = departmentCode;
        this.departmentName = departmentName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    public EmployeeResponse(Long id, String fullName, String email, String password,LocalDate hireDate, String status,
                            Long departmentId, String departmentCode, String departmentName,
                            OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.hireDate = hireDate;
        this.status = status;
        this.departmentId = departmentId;
        this.departmentCode = departmentCode;
        this.departmentName = departmentName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
