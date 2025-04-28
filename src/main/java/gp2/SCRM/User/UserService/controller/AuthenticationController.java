package gp2.SCRM.User.UserService.controller;

import gp2.SCRM.User.UserService.dto.LoginUserDto;
import gp2.SCRM.User.UserService.dto.RegisterUserDto;
import gp2.SCRM.User.UserService.model.Role;
import gp2.SCRM.User.UserService.model.User;
import gp2.SCRM.User.UserService.repository.UserRepository;
import gp2.SCRM.User.UserService.response.LoginResponse;
import gp2.SCRM.User.UserService.service.AuthenticationService;
import gp2.SCRM.User.UserService.service.JwtService;
import gp2.SCRM.User.UserService.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Authentication", description = "Endpoints for user authentication and management")
@RequestMapping("/auth")
@RestController
public class AuthenticationController {

    @Autowired
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final AuthenticationService authenticationService;

    public AuthenticationController(JwtService jwtService, UserRepository userRepository,
                                    PasswordEncoder passwordEncoder, UserService userService,
                                    AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.authenticationService = authenticationService;
    }

    @Operation(summary = "Register a new user", description = "Allows a SUPERADMIN to register a new user.")
    @ApiResponse(responseCode = "200", description = "User registered successfully")
    @PreAuthorize("hasRole('SUPERADMIN')")
    @PostMapping("/signup")
    public ResponseEntity<User> register(@RequestBody RegisterUserDto registerUserDto) {
        User registeredUser = authenticationService.signup(registerUserDto);
        return ResponseEntity.ok(registeredUser);
    }

    @Operation(summary = "Create a new teacher", description = "Allows a SUPERADMIN to create a new teacher.")
    @ApiResponse(responseCode = "200", description = "Teacher created successfully")
    @PreAuthorize("hasRole('SUPERADMIN')")
    @PostMapping("/teacher")
    public ResponseEntity<User> createTeacher(@RequestBody RegisterUserDto registerUserDto) {
        User createdAdmin = userService.createTeacher(registerUserDto);
        return ResponseEntity.ok(createdAdmin);
    }

    @Operation(summary = "Delete a user by ID", description = "Allows a SUPERADMIN to delete a user by ID.")
    @ApiResponse(responseCode = "204", description = "User deleted successfully")
    @PreAuthorize("hasRole('SUPERADMIN')")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<User> deleteAdmin(@PathVariable Long id) {
        userService.deleteTeacher(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Authenticate user", description = "Generates a JWT token for authenticated users.")
    @ApiResponse(responseCode = "200", description = "Authentication successful, returns JWT token")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginUserDto loginUserDto) {
        User authenticatedUser = authenticationService.authenticate(loginUserDto);
        String jwtToken = jwtService.generateToken(authenticatedUser);
        LoginResponse loginResponse = new LoginResponse()
                .setToken(jwtToken)
                .setExpiresIn(jwtService.getExpirationTime());
        return ResponseEntity.ok(loginResponse);
    }

    @Operation(summary = "Get all users", description = "Allows a SUPERADMIN to view all users.")
    @ApiResponse(responseCode = "200", description = "List of all users")
    @PreAuthorize("hasRole('SUPERADMIN')")
    @GetMapping("/users")
    public ResponseEntity<List<User>> allUsers() {
        List<User> users = userService.allUsers();
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Get all teachers", description = "Retrieves a list of all teachers.")
    @ApiResponse(responseCode = "200", description = "List of teachers")
    @GetMapping("/teachers")
    public List<User> getteachers() {
        return userService.getUsersByRole(Role.RoleEnum.TEACHER);
    }

    @Operation(summary = "Get all students", description = "Retrieves a list of all students.")
    @ApiResponse(responseCode = "200", description = "List of students")
    @GetMapping("/students")
    public List<User> getstudents() {
        return userService.getUsersByRole(Role.RoleEnum.STUDENT);
    }

    @Operation(summary = "Get authenticated user info", description = "Returns the currently authenticated user's information.")
    @ApiResponse(responseCode = "200", description = "Authenticated user details")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/users/me")
    public ResponseEntity<User> authenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(currentUser);
    }
}
