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
        if (userRepository.count() > 0) {
            return;
        }
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setFullName("科研秘书");
        admin.setEmail("secretary@example.com");
        admin.setDepartment(Department.COMPUTER_SCIENCE);
        admin.setRole(UserRole.ADMIN);
        userRepository.save(admin);

        List<User> teachers = List.of(
                buildTeacher("alice", "Alice 张", Department.COMPUTER_SCIENCE),
                buildTeacher("bob", "Bob 李", Department.MATHEMATICS),
                buildTeacher("cathy", "Cathy 王", Department.PHYSICS),
                buildTeacher("david", "David 赵", Department.CHEMISTRY)
        );
        teachers.forEach(userRepository::save);
    }

    private User buildTeacher(String username, String name, Department department) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("teacher123"));
        user.setFullName(name);
        user.setEmail(username + "@example.com");
        user.setDepartment(department);
        user.setRole(UserRole.TEACHER);
        return user;
    }
}

