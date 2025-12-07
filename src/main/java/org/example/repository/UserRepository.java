package org.example.repository;

import java.util.List;
import java.util.Optional;
import org.example.entity.User;
import org.example.entity.enums.Department;
import org.example.entity.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findAllByRole(UserRole role);
    List<User> findAllByRoleAndDepartment(UserRole role, Department department);
}

