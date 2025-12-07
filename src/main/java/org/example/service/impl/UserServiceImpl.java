package org.example.service.impl;

import java.util.List;
import org.example.entity.User;
import org.example.entity.enums.Department;
import org.example.entity.enums.UserRole;
import org.example.repository.UserRepository;
import org.example.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    }

    @Override
    public List<User> findTeachers() {
        return userRepository.findAllByRole(UserRole.TEACHER);
    }

    @Override
    public List<User> findTeachersByDepartment(Department department) {
        return userRepository.findAllByRoleAndDepartment(UserRole.TEACHER, department);
    }
}

