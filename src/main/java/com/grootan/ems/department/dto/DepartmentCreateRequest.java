package com.grootan.ems.department.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DepartmentCreateRequest {

    @NotBlank @Size(max = 40)
    private String code;

    @NotBlank @Size(max = 120)
    private String name;
}
