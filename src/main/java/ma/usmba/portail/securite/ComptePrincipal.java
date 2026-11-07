package ma.usmba.portail.securite;

import ma.usmba.portail.entite.Compte;
import ma.usmba.portail.entite.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Adapte l'entite Compte au contrat UserDetails attendu par Spring Security.
 */
public class ComptePrincipal implements UserDetails {

    private final Long id;
    private final String identifiant;
    private final String motDePasse;
    private final String nomComplet;
    private final String email;
    private final boolean actif;
    private final LocalDateTime derniereConnexion;
    private final List<GrantedAuthority> autorites;

    public ComptePrincipal(Compte compte) {
        this.id = compte.getId();
        this.identifiant = compte.getIdentifiant();
        this.motDePasse = compte.getMotDePasse();
        this.nomComplet = compte.getNomComplet();
        this.email = compte.getEmail();
        this.actif = compte.isActif();
        this.derniereConnexion = compte.getDerniereConnexion();
        this.autorites = compte.getRoles().stream()
                .map(Role::getCode)
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();
    }

    public Long getId() {
        return id;
    }

    public String getNomComplet() {
        return nomComplet;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getDerniereConnexion() {
        return derniereConnexion;
    }

    public List<String> getCodesRoles() {
        return autorites.stream().map(GrantedAuthority::getAuthority).toList();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return autorites;
    }

    @Override
    public String getPassword() {
        return motDePasse;
    }

    @Override
    public String getUsername() {
        return identifiant;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return actif;
    }
}
