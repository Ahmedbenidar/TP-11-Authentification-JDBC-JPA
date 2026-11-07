package ma.usmba.portail.securite;

import ma.usmba.portail.depot.CompteDepot;
import ma.usmba.portail.entite.Compte;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Charge un compte depuis MySQL au lieu du stockage en memoire.
 */
@Service
public class CompteUserDetailsService implements UserDetailsService {

    private final CompteDepot compteDepot;

    public CompteUserDetailsService(CompteDepot compteDepot) {
        this.compteDepot = compteDepot;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String identifiant) throws UsernameNotFoundException {
        Compte compte = compteDepot.chargerParIdentifiant(identifiant)
                .orElseThrow(() -> new UsernameNotFoundException("Aucun compte ne porte l'identifiant : " + identifiant));
        return new ComptePrincipal(compte);
    }
}
