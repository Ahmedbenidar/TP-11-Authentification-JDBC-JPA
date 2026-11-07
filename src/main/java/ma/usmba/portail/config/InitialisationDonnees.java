package ma.usmba.portail.config;

import ma.usmba.portail.depot.CompteDepot;
import ma.usmba.portail.depot.RoleDepot;
import ma.usmba.portail.entite.Compte;
import ma.usmba.portail.entite.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Insere les roles et les comptes de demonstration au premier demarrage.
 * Les mots de passe sont haches avec BCrypt avant persistance.
 */
@Component
public class InitialisationDonnees implements CommandLineRunner {

    private static final Logger journal = LoggerFactory.getLogger(InitialisationDonnees.class);

    private final RoleDepot roleDepot;
    private final CompteDepot compteDepot;
    private final PasswordEncoder encodeur;

    public InitialisationDonnees(RoleDepot roleDepot, CompteDepot compteDepot, PasswordEncoder encodeur) {
        this.roleDepot = roleDepot;
        this.compteDepot = compteDepot;
        this.encodeur = encodeur;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Role administrateur = roleDepot.findByCode("ROLE_ADMIN")
                .orElseGet(() -> roleDepot.save(new Role("ROLE_ADMIN", "Administrateur")));
        Role membre = roleDepot.findByCode("ROLE_MEMBRE")
                .orElseGet(() -> roleDepot.save(new Role("ROLE_MEMBRE", "Membre")));
        Role observateur = roleDepot.findByCode("ROLE_OBSERVATEUR")
                .orElseGet(() -> roleDepot.save(new Role("ROLE_OBSERVATEUR", "Observateur")));

        if (compteDepot.count() > 0) {
            journal.info("Comptes deja presents : {} en base", compteDepot.count());
            return;
        }

        creerCompte("a.benidar", "Portail@2026", "ahmed.benidar@usmba.ac.ma",
                "Ahmed Benidar", LocalDateTime.now().minusDays(1),
                List.of(administrateur, membre));

        creerCompte("s.elidrissi", "Membre@2026", "salma.elidrissi@usmba.ac.ma",
                "Salma El Idrissi", LocalDateTime.now().minusHours(6),
                List.of(membre));

        creerCompte("y.ouazzani", "Membre@2026", "yassine.ouazzani@usmba.ac.ma",
                "Yassine Ouazzani", LocalDateTime.now().minusDays(3),
                List.of(membre, observateur));

        creerCompte("h.alaoui", "Lecteur@2026", "hafsa.alaoui@usmba.ac.ma",
                "Hafsa Alaoui", null,
                List.of(observateur));

        journal.info("Jeu de donnees initialise : {} comptes, {} roles",
                compteDepot.count(), roleDepot.count());
    }

    private void creerCompte(String identifiant, String motDePasseClair, String email,
                             String nomComplet, LocalDateTime derniereConnexion, List<Role> roles) {
        Compte compte = new Compte();
        compte.setIdentifiant(identifiant);
        compte.setMotDePasse(encodeur.encode(motDePasseClair));
        compte.setEmail(email);
        compte.setNomComplet(nomComplet);
        compte.setActif(true);
        compte.setDerniereConnexion(derniereConnexion);
        roles.forEach(compte::ajouterRole);
        compteDepot.save(compte);
    }
}
