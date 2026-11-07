package ma.usmba.portail.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Isole l'encodeur de mots de passe dans son propre contexte de configuration :
 * CompteService en depend, il ne doit donc pas etre declare dans SecuriteConfig
 * qui depend elle-meme du handler de connexion (donc de CompteService).
 */
@Configuration
public class EncodageConfig {

    @Bean
    public PasswordEncoder encodeurMotDePasse() {
        return new BCryptPasswordEncoder();
    }
}
