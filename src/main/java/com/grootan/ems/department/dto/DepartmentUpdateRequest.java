package com.grootan.ems.department.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DepartmentUpdateRequest {

    @NotBlank @Size(max = 120)
    private String name;
}
