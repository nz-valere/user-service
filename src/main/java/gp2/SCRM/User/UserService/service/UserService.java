package gp2.SCRM.User.UserService.service;


import gp2.SCRM.User.UserService.dto.RegisterUserDto;
import gp2.SCRM.User.UserService.model.Role;
import gp2.SCRM.User.UserService.model.User;
import gp2.SCRM.User.UserService.repository.RoleRepository;
import gp2.SCRM.User.UserService.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    private final EmailService emailService;

    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, EmailService emailService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> allUsers() {
        List<User> users = new ArrayList<>();
        userRepository.findAll().forEach(users::add);
        return users;
    }

    public List<User> getUsersByRole(Role.RoleEnum roleName) {
        return userRepository.findByRoleName(roleName);
    }

    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%&*!";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }

    public User createTeacher(RegisterUserDto input) {
        Optional<Role> optionalRole = roleRepository.findByName(Role.RoleEnum.TEACHER);
        if (userRepository.findByEmail(input.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }
        if (optionalRole.isEmpty()) {
            return null;
        }

        // Generate random password
        String randomPassword = generateRandomPassword(8);

        this.emailService.SendPasswordtoTeacher(input,randomPassword);

        var user = new User()
                .setName(input.getFullName())
                .setEmail(input.getEmail())
                .setPassword(passwordEncoder.encode(randomPassword))
                .setRole(optionalRole.get());



        return userRepository.save(user);
    }



    public void deleteTeacher(Long id){ userRepository.deleteById(id); }

    public User editTeacher(Long id, User user){
        Optional<Role> optionalRole = roleRepository.findByName(Role.RoleEnum.TEACHER);
        return userRepository.findById(id)
                .map(currentUser -> {
                    currentUser.setName(user.getName());
                    currentUser.setEmail(user.getEmail());
                    currentUser.setRole(optionalRole.get());
                    currentUser.setPassword(user.getPassword());

                    return userRepository.save(currentUser);
                })
                .orElseThrow(() -> new RuntimeException("User is not found"));
    }
}