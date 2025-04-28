package gp2.SCRM.User.UserService.IntegrationTest;

import gp2.SCRM.User.UserService.dto.RegisterUserDto;
import gp2.SCRM.User.UserService.model.Role;
import gp2.SCRM.User.UserService.model.User;
import gp2.SCRM.User.UserService.repository.RoleRepository;
import gp2.SCRM.User.UserService.repository.UserRepository;
import gp2.SCRM.User.UserService.service.EmailService;
import gp2.SCRM.User.UserService.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")  // Uses application-test.properties
public class UserServiceIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private EmailService emailService;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, roleRepository, emailService, new BCryptPasswordEncoder());

        // Clear data before each test
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Add a role for testing
        Role teacherRole = new Role();
        teacherRole.setName(Role.RoleEnum.TEACHER);
        roleRepository.save(teacherRole);
    }

    // 1. Test allUsers()
    @Test
    void testAllUsers() {
        Role teacherRole = roleRepository.findByName(Role.RoleEnum.TEACHER).orElseThrow();

        User user1 = new User().setName("Alice").setEmail("alice@example.com").setPassword("alice")
                .setRole(teacherRole);
        User user2 = new User().setName("Bob").setEmail("bob@example.com").setPassword("Bob")
                .setRole(teacherRole);

        userRepository.save(user1);
        userRepository.save(user2);

        List<User> users = userService.allUsers();

        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getName).containsExactly("Alice", "Bob");
    }

    // 2. Test getUsersByRole()
    @Test
    void testGetUsersByRole() {
        Role teacherRole = roleRepository.findByName(Role.RoleEnum.TEACHER).orElseThrow();

        User user1 = new User().setName("Teacher A").setEmail("teacherA@example.com").setRole(teacherRole).setPassword("teacherA");
        User user2 = new User().setName("Teacher B").setEmail("teacherB@example.com").setRole(teacherRole).setPassword("teacherB");

        userRepository.saveAll(List.of(user1, user2));

        List<User> teachers = userService.getUsersByRole(Role.RoleEnum.TEACHER);

        assertThat(teachers).hasSize(2);
        assertThat(teachers).extracting(User::getName).containsExactly("Teacher A", "Teacher B");
    }

    // 3. Test createTeacher()
    @Test
    void testCreateTeacher() {
        RegisterUserDto input = new RegisterUserDto();
        input.setFullName("Teacher Name");
        input.setEmail("teacher@example.com");

        User savedUser = userService.createTeacher(input);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getName()).isEqualTo("Teacher Name");
        assertThat(savedUser.getEmail()).isEqualTo("teacher@example.com");
        assertThat(savedUser.getPassword()).isNotNull();
        assertThat(savedUser.getRole().getName()).isEqualTo(Role.RoleEnum.TEACHER);
    }

    @Test
    void testCreateTeacher_EmailAlreadyExists() {
        RegisterUserDto input = new RegisterUserDto();
        input.setEmail("teacher@example.com");

        Role teacherRole = roleRepository.findByName(Role.RoleEnum.TEACHER).orElseThrow();

        User existingUser = new User().setName("Existing Teacher").setEmail("teacher@example.com")
                .setPassword("teacher").setRole(teacherRole);
        userRepository.save(existingUser);

        assertThrows(IllegalArgumentException.class, () -> userService.createTeacher(input));
    }

    // 4. Test deleteTeacher()
    @Test
    void testDeleteTeacher() {
        Role teacherRole = roleRepository.findByName(Role.RoleEnum.TEACHER).orElseThrow();
        User user = new User().setName("To Be Deleted").setEmail("delete@example.com").setPassword("delete")
                .setRole(teacherRole);
        userRepository.save(user);

        userService.deleteTeacher(user.getId());

        Optional<User> deletedUser = userRepository.findById(user.getId());
        assertThat(deletedUser).isEmpty();
    }

    // 5. Test editTeacher()
    @Test
    void testEditTeacher() {
        Role teacherRole = roleRepository.findByName(Role.RoleEnum.TEACHER).orElseThrow();

        User existingUser = new User().setName("Old Name").setEmail("old@example.com").setRole(teacherRole)
                .setPassword("azerty");
        userRepository.save(existingUser);

        User updatedUser = new User().setName("New Name").setEmail("new@example.com").setRole(teacherRole)
                .setPassword("qwerty");

        User editedUser = userService.editTeacher(existingUser.getId(), updatedUser);

        assertThat(editedUser).isNotNull();
        assertThat(editedUser.getName()).isEqualTo("New Name");
        assertThat(editedUser.getEmail()).isEqualTo("new@example.com");
        assertThat(editedUser.getRole().getName()).isEqualTo(Role.RoleEnum.TEACHER);
        assertThat(editedUser.getPassword()).isEqualTo("qwerty");
    }
}
