package org.example.config;

import java.util.List;
import org.example.entity.User;
import org.example.entity.enums.Department;
import org.example.entity.enums.UserRole;
import org.example.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Update or Create Admin
        createOrUpdateUser("admin", "admin123", "科研秘书", "***", Department.COMPUTER_SCIENCE, UserRole.ADMIN);

        // Update or Create Teachers
        createOrUpdateUser("alice", "teacher123", "Alice 张", "***", Department.COMPUTER_SCIENCE, UserRole.TEACHER);
        createOrUpdateUser("bob", "teacher123", "Bob 李", "***", Department.MATHEMATICS, UserRole.TEACHER);
        createOrUpdateUser("cathy", "teacher123", "Cathy 王", "***", Department.PHYSICS, UserRole.TEACHER);
        createOrUpdateUser("david", "teacher123", "David 赵", "", Department.CHEMISTRY, UserRole.TEACHER);
    }

    private void createOrUpdateUser(String username, String password, String fullName, String email, Department department, UserRole role) {
        User user = userRepository.findByUsername(username).orElse(new User());
        user.setUsername(username);
        if (user.getId() == null) {
            user.setPassword(passwordEncoder.encode(password));
        }
        user.setFullName(fullName);
        user.setEmail(email);
        user.setDepartment(department);
        user.setRole(role);
        userRepository.save(user);
    }
}
