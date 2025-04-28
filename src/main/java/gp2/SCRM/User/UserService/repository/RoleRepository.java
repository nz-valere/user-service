package gp2.SCRM.User.UserService.repository;

import gp2.SCRM.User.UserService.model.Role;
import gp2.SCRM.User.UserService.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends CrudRepository<Role, Integer> {
    Optional<Role> findByName(Role.RoleEnum name);
}
