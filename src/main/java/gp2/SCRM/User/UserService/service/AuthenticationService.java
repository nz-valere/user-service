package gp2.SCRM.User.UserService.service;

import gp2.SCRM.User.UserService.dto.LoginUserDto;
import gp2.SCRM.User.UserService.dto.RegisterUserDto;
import gp2.SCRM.User.UserService.model.Role;
import gp2.SCRM.User.UserService.model.User;
import gp2.SCRM.User.UserService.repository.RoleRepository;
import gp2.SCRM.User.UserService.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Optional;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final RoleRepository roleRepository;

    private final EmailService emailService;

    private JwtService jwtService;

    public AuthenticationService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder,
            EmailService emailService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.emailService = emailService;
    }

    public User signup(RegisterUserDto input) {
        Optional<Role> optionalRole = roleRepository.findByName(Role.RoleEnum.STUDENT);
        if (optionalRole.isEmpty()) {
            return null;
        }
        if (userRepository.findByEmail(input.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        String randomPassword = generateRandomPassword(8);

        this.emailService.SendPasswordtoCandidate(input,randomPassword);

        User user = new User()
                .setName(input.getFullName())
                .setEmail(input.getEmail())
                .setPassword(passwordEncoder.encode(randomPassword))
                .setRole(optionalRole.get());

        return userRepository.save(user);
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


    public User authenticate(LoginUserDto input) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getEmail(),
                        input.getPassword()
                )
        );

        return userRepository.findByEmail(input.getEmail())
                .orElseThrow();
    }


}
