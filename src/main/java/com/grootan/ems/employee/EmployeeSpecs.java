package com.grootan.ems.employee;

import org.springframework.data.jpa.domain.Specification;

public class EmployeeSpecs {

    public static Specification<Employee> nameContains(String q) {
        return (root, query, cb) -> {
            if (q == null || q.trim().isEmpty()) return cb.conjunction();
            String like = "%" + q.trim().toLowerCase() + "%";
            return cb.like(cb.lower(root.get("fullName")), like);
        };
    }

    public static Specification<Employee> departmentIdEquals(Long departmentId) {
        return (root, query, cb) -> {
            if (departmentId == null) return cb.conjunction();
            return cb.equal(root.get("department").get("id"), departmentId);
        };
    }

    public static Specification<Employee> statusEquals(EmployeeStatus status) {
        return (root, query, cb) -> {
            if (status == null) return cb.conjunction();
            return cb.equal(root.get("status"), status);
        };
    }
}
