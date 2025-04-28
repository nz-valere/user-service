package gp2.SCRM.User.UserService.bootstrap;

import gp2.SCRM.User.UserService.model.Role;
import gp2.SCRM.User.UserService.repository.RoleRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@Component
public class RoleSeeder implements ApplicationListener<ContextRefreshedEvent> {
    private final RoleRepository roleRepository;


    public RoleSeeder(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        this.loadRoles();
    }

    private void loadRoles() {
        Role.RoleEnum[] roleNames = new Role.RoleEnum[] {Role.RoleEnum.STUDENT, Role.RoleEnum.TEACHER, Role.RoleEnum.SUPERADMIN };
        Map<Role.RoleEnum, String> roleDescriptionMap = Map.of(
                Role.RoleEnum.STUDENT, "Student role",
                Role.RoleEnum.TEACHER, "Teacher role",
                Role.RoleEnum.SUPERADMIN, "Super Administrator role"
        );

        Arrays.stream(roleNames).forEach((roleName) -> {
            Optional<Role> optionalRole = roleRepository.findByName(roleName);

            optionalRole.ifPresentOrElse(System.out::println, () -> {
                Role roleToCreate = new Role();

                roleToCreate.setName(roleName);

                roleRepository.save(roleToCreate);
            });
        });
    }
}
