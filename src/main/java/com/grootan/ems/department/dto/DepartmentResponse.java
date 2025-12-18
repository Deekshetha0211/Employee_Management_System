package com.grootan.ems.department.dto;

import java.time.OffsetDateTime;

public class DepartmentResponse {
    private Long id;
    private String code;
    private String name;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public DepartmentResponse(Long id, String code, String name, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
