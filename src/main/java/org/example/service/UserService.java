package org.example.service;

import java.util.List;
import org.example.entity.User;
import org.example.entity.enums.Department;

public interface UserService {

    User findByUsername(String username);

    List<User> findTeachers();

    List<User> findTeachersByDepartment(Department department);
}

