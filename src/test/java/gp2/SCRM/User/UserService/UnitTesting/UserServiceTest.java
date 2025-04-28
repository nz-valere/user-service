package gp2.SCRM.User.UserService.UnitTesting;

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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        // Setup common mocks or data here if needed
    }

    // 1. Test allUsers()
    @Test
    void testAllUsers() {
        User user1 = new User().setName("Alice").setEmail("alice@example.com");
        User user2 = new User().setName("Bob").setEmail("bob@example.com");

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        List<User> users = userService.allUsers();

        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getName).containsExactly("Alice", "Bob");

        verify(userRepository, times(1)).findAll();
    }

    // 2. Test getUsersByRole()
    @Test
    void testGetUsersByRole() {
        Role.RoleEnum roleEnum = Role.RoleEnum.TEACHER;
        User user = new User().setName("John").setEmail("john@example.com");

        when(userRepository.findByRoleName(roleEnum)).thenReturn(List.of(user));

        List<User> users = userService.getUsersByRole(roleEnum);

        assertThat(users).hasSize(1);
        assertThat(users.get(0).getName()).isEqualTo("John");

        verify(userRepository, times(1)).findByRoleName(roleEnum);
    }

    // 3. Test createTeacher()
    @Test
    void testCreateTeacher() {
        RegisterUserDto input = new RegisterUserDto();
        input.setFullName("Teacher Name");
        input.setEmail("teacher@example.com");

        Role role = new Role().setName(Role.RoleEnum.TEACHER);

        when(userRepository.findByEmail(input.getEmail())).thenReturn(Optional.empty());
        when(roleRepository.findByName(Role.RoleEnum.TEACHER)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(any(String.class))).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User savedUser = userService.createTeacher(input);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getName()).isEqualTo("Teacher Name");
        assertThat(savedUser.getEmail()).isEqualTo("teacher@example.com");
        assertThat(savedUser.getPassword()).isEqualTo("encodedPassword");
        assertThat(savedUser.getRole().getName()).isEqualTo(Role.RoleEnum.TEACHER);

        verify(emailService, times(1)).SendPasswordtoTeacher(eq(input), any(String.class));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testCreateTeacher_EmailAlreadyExists() {
        RegisterUserDto input = new RegisterUserDto();
        input.setEmail("teacher@example.com");

        when(userRepository.findByEmail(input.getEmail())).thenReturn(Optional.of(new User()));

        assertThrows(IllegalArgumentException.class, () -> userService.createTeacher(input));

        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).SendPasswordtoTeacher(any(), any());
    }

    // 4. Test deleteTeacher()
    @Test
    void testDeleteTeacher() {
        Long teacherId = 1L;

        userService.deleteTeacher(teacherId);

        verify(userRepository, times(1)).deleteById(teacherId);
    }

    // 5. Test editTeacher()
    @Test
    void testEditTeacher() {
        Long teacherId = 1L;
        User existingUser = new User()
                .setId(teacherId)
                .setName("Old Name")
                .setEmail("old@example.com")
                .setPassword("oldPassword");

        User updatedUser = new User()
                .setName("New Name")
                .setEmail("new@example.com")
                .setPassword("newPassword");

        Role role = new Role().setName(Role.RoleEnum.TEACHER);

        when(userRepository.findById(teacherId)).thenReturn(Optional.of(existingUser));
        when(roleRepository.findByName(Role.RoleEnum.TEACHER)).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User editedUser = userService.editTeacher(teacherId, updatedUser);

        assertThat(editedUser).isNotNull();
        assertThat(editedUser.getName()).isEqualTo("New Name");
        assertThat(editedUser.getEmail()).isEqualTo("new@example.com");
        assertThat(editedUser.getPassword()).isEqualTo("newPassword");
        assertThat(editedUser.getRole().getName()).isEqualTo(Role.RoleEnum.TEACHER);

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testEditTeacher_UserNotFound() {
        Long teacherId = 1L;
        User updatedUser = new User().setName("New Name");

        when(userRepository.findById(teacherId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.editTeacher(teacherId, updatedUser));

        verify(userRepository, never()).save(any(User.class));
    }
}