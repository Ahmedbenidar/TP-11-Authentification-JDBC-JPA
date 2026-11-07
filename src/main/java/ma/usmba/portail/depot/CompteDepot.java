package ma.usmba.portail.depot;

import ma.usmba.portail.entite.Compte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CompteDepot extends JpaRepository<Compte, Long> {

    @Query("""
            select distinct c from Compte c
            left join fetch c.roles
            where lower(c.identifiant) = lower(:identifiant)
            """)
    Optional<Compte> chargerParIdentifiant(@Param("identifiant") String identifiant);

    boolean existsByIdentifiantIgnoreCase(String identifiant);

    boolean existsByEmailIgnoreCase(String email);
}
