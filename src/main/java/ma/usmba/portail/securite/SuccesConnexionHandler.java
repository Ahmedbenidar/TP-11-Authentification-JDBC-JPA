package ma.usmba.portail.securite;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ma.usmba.portail.service.CompteService;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Redirige vers l'espace correspondant au role du compte et horodate la connexion.
 */
@Component
public class SuccesConnexionHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final CompteService compteService;

    public SuccesConnexionHandler(CompteService compteService) {
        this.compteService = compteService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest requete,
                                        HttpServletResponse reponse,
                                        Authentication authentification) throws IOException, ServletException {
        if (authentification.getPrincipal() instanceof ComptePrincipal principal) {
            compteService.enregistrerConnexion(principal.getId());
            boolean administrateur = principal.getCodesRoles().contains("ROLE_ADMIN");
            setDefaultTargetUrl(administrateur ? "/administration/tableau-de-bord" : "/espace/accueil");
        }
        super.onAuthenticationSuccess(requete, reponse, authentification);
    }
}
