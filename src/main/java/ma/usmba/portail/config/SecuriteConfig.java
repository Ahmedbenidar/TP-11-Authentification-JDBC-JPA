package ma.usmba.portail.config;

import ma.usmba.portail.securite.CompteUserDetailsService;
import ma.usmba.portail.securite.SuccesConnexionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecuriteConfig {

    private final CompteUserDetailsService compteUserDetailsService;
    private final SuccesConnexionHandler succesConnexionHandler;
    private final PasswordEncoder encodeurMotDePasse;

    public SecuriteConfig(CompteUserDetailsService compteUserDetailsService,
                          SuccesConnexionHandler succesConnexionHandler,
                          PasswordEncoder encodeurMotDePasse) {
        this.compteUserDetailsService = compteUserDetailsService;
        this.succesConnexionHandler = succesConnexionHandler;
        this.encodeurMotDePasse = encodeurMotDePasse;
    }

    @Bean
    public DaoAuthenticationProvider fournisseurAuthentification() {
        DaoAuthenticationProvider fournisseur = new DaoAuthenticationProvider(compteUserDetailsService);
        fournisseur.setPasswordEncoder(encodeurMotDePasse);
        fournisseur.setHideUserNotFoundExceptions(false);
        return fournisseur;
    }

    @Bean
    public SecurityFilterChain chaineFiltres(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(regles -> regles
                        .requestMatchers("/connexion", "/css/**", "/erreur").permitAll()
                        .requestMatchers("/administration/**").hasRole("ADMIN")
                        .requestMatchers("/espace/**").hasAnyRole("MEMBRE", "ADMIN")
                        .anyRequest().authenticated())
                .formLogin(connexion -> connexion
                        .loginPage("/connexion")
                        .loginProcessingUrl("/connexion")
                        .usernameParameter("identifiant")
                        .passwordParameter("motDePasse")
                        .successHandler(succesConnexionHandler)
                        .failureUrl("/connexion?erreur=true")
                        .permitAll())
                .logout(deconnexion -> deconnexion
                        .logoutRequestMatcher(new AntPathRequestMatcher("/deconnexion", "POST"))
                        .logoutSuccessUrl("/connexion?deconnexion=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())
                .exceptionHandling(gestion -> gestion.accessDeniedPage("/acces-refuse"));
        return http.build();
    }
}
