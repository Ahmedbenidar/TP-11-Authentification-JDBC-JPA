package ma.usmba.portail.controleur;

import ma.usmba.portail.entite.Compte;
import ma.usmba.portail.securite.ComptePrincipal;
import ma.usmba.portail.service.CompteService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Controller
public class PortailControleur {

    private static final DateTimeFormatter FORMAT_DATE =
            DateTimeFormatter.ofPattern("dd MMMM yyyy 'à' HH:mm", Locale.FRENCH);

    private final CompteService compteService;

    public PortailControleur(CompteService compteService) {
        this.compteService = compteService;
    }

    @GetMapping("/")
    public String racine() {
        return "redirect:/espace/accueil";
    }

    @GetMapping("/connexion")
    public String pageConnexion(@RequestParam(name = "erreur", required = false) String erreur,
                                @RequestParam(name = "deconnexion", required = false) String deconnexion,
                                Model modele) {
        modele.addAttribute("erreurIdentifiants", erreur != null);
        modele.addAttribute("deconnexionEffectuee", deconnexion != null);
        return "connexion";
    }

    @GetMapping("/acces-refuse")
    public String accesRefuse(@AuthenticationPrincipal ComptePrincipal principal, Model modele) {
        modele.addAttribute("principal", principal);
        modele.addAttribute("initiales", initiales(principal == null ? null : principal.getNomComplet()));
        return "acces-refuse";
    }

    @GetMapping("/espace/accueil")
    public String espaceMembre(@AuthenticationPrincipal ComptePrincipal principal, Model modele) {
        modele.addAttribute("principal", principal);
        modele.addAttribute("initiales", initiales(principal.getNomComplet()));
        modele.addAttribute("roles", principal.getCodesRoles());
        modele.addAttribute("derniereConnexion", principal.getDerniereConnexion() == null
                ? "Première connexion à ce portail"
                : principal.getDerniereConnexion().format(FORMAT_DATE));
        return "espace-accueil";
    }

    @GetMapping("/espace/profil")
    public String profil(@AuthenticationPrincipal ComptePrincipal principal, Model modele) {
        modele.addAttribute("principal", principal);
        modele.addAttribute("initiales", initiales(principal.getNomComplet()));
        modele.addAttribute("roles", principal.getCodesRoles());
        modele.addAttribute("dateNaissanceCompte", principal.getDerniereConnexion() == null
                ? "—"
                : principal.getDerniereConnexion().format(FORMAT_DATE));
        return "profil";
    }

    @GetMapping("/administration/tableau-de-bord")
    public String tableauDeBord(@AuthenticationPrincipal ComptePrincipal principal, Model modele) {
        List<Compte> comptes = compteService.listerComptes();
        modele.addAttribute("principal", principal);
        modele.addAttribute("comptes", comptes);
        modele.addAttribute("nombreTotal", compteService.nombreComptesTotal());
        modele.addAttribute("nombreActifs", compteService.nombreComptesActifs());
        modele.addAttribute("formatDate", FORMAT_DATE);
        return "administration-tableau-de-bord";
    }

    private String initiales(String nomComplet) {
        if (nomComplet == null || nomComplet.isBlank()) {
            return "?";
        }
        String[] morceaux = nomComplet.trim().split("\\s+");
        String premiere = morceaux[0].substring(0, 1);
        String derniere = morceaux.length > 1
                ? morceaux[morceaux.length - 1].substring(0, 1)
                : "";
        return (premiere + derniere).toUpperCase(Locale.FRENCH);
    }
}
