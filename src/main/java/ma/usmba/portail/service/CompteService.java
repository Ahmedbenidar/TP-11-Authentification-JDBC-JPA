package ma.usmba.portail.service;

import ma.usmba.portail.depot.CompteDepot;
import ma.usmba.portail.depot.RoleDepot;
import ma.usmba.portail.entite.Compte;
import ma.usmba.portail.entite.Role;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class CompteService {

    private final CompteDepot compteDepot;
    private final RoleDepot roleDepot;
    private final PasswordEncoder encodeur;

    public CompteService(CompteDepot compteDepot, RoleDepot roleDepot, PasswordEncoder encodeur) {
        this.compteDepot = compteDepot;
        this.roleDepot = roleDepot;
        this.encodeur = encodeur;
    }

    public List<Compte> listerComptes() {
        return compteDepot.findAll();
    }

    public Optional<Compte> parIdentifiant(String identifiant) {
        return compteDepot.chargerParIdentifiant(identifiant);
    }

    public long nombreComptesActifs() {
        return compteDepot.findAll().stream().filter(Compte::isActif).count();
    }

    public long nombreComptesTotal() {
        return compteDepot.count();
    }

    @Transactional
    public void enregistrerConnexion(Long idCompte) {
        compteDepot.findById(idCompte).ifPresent(compte -> {
            compte.setDerniereConnexion(LocalDateTime.now());
            compteDepot.save(compte);
        });
    }

    @Transactional
    public Compte creerCompte(String identifiant, String motDePasseClair, String email,
                              String nomComplet, List<String> codesRoles) {
        Compte compte = new Compte();
        compte.setIdentifiant(identifiant.trim());
        compte.setMotDePasse(encodeur.encode(motDePasseClair));
        compte.setEmail(email.trim().toLowerCase());
        compte.setNomComplet(nomComplet.trim());
        compte.setActif(true);
        for (String code : codesRoles) {
            roleDepot.findByCode(code).ifPresent(compte::ajouterRole);
        }
        return compteDepot.save(compte);
    }

    @Transactional
    public Role creerRoleSiAbsent(String code, String libelle) {
        return roleDepot.findByCode(code)
                .orElseGet(() -> roleDepot.save(new Role(code, libelle)));
    }
}
