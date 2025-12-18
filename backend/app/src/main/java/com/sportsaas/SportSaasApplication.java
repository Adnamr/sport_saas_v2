package com.sportsaas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Point d'entrée principal de l'application Sport SaaS.
 *
 * Plateforme multi-tenant de gestion de matériels sportifs.
 */
@SpringBootApplication
@EnableJpaAuditing
public class SportSaasApplication {

    public static void main(String[] args) {
        SpringApplication.run(SportSaasApplication.class, args);
    }
}
