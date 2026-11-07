package ma.usmba.portail.depot;

import ma.usmba.portail.entite.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleDepot extends JpaRepository<Role, Long> {

    Optional<Role> findByCode(String code);
}
