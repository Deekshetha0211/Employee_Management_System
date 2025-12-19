package com.grootan.ems.department.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
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
}
