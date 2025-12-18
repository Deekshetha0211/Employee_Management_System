package com.grootan.ems.department.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DepartmentUpdateRequest {

    @NotBlank @Size(max = 120)
    private String name;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
