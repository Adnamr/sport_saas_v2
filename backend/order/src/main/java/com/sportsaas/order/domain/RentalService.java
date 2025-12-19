package com.sportsaas.order.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service de gestion des locations.
 */
public interface RentalService {

    /**
     * Cree une nouvelle location.
     */
    Rental create(Rental rental);

    /**
     * Recupere une location par ID.
     */
    Optional<Rental> findById(UUID id);

    /**
     * Recupere une location par numero.
     */
    Optional<Rental> findByRentalNumber(String rentalNumber);

    /**
     * Liste les locations d'un client.
     */
    Page<Rental> findByCustomerId(UUID customerId, Pageable pageable);

    /**
     * Liste les locations par statut.
     */
    Page<Rental> findByStatus(RentalStatus status, Pageable pageable);

    /**
     * Liste les locations d'un produit.
     */
    Page<Rental> findByProductId(UUID productId, Pageable pageable);

    /**
     * Confirme une location.
     */
    Rental confirm(UUID id);

    /**
     * Demarre une location (depart du materiel).
     */
    Rental start(UUID id, String conditionAtStart);

    /**
     * Termine une location (retour du materiel).
     */
    Rental returnRental(UUID id, String conditionAtReturn);

    /**
     * Annule une location.
     */
    Rental cancel(UUID id, String reason);

    /**
     * Prolonge une location.
     */
    Rental extend(UUID id, LocalDateTime newEndDate);

    /**
     * Ajoute des frais supplementaires.
     */
    Rental addExtraCharges(UUID id, BigDecimal amount, String reason);

    /**
     * Traite les locations en retard.
     */
    int processOverdueRentals();

    /**
     * Liste les locations en retard.
     */
    List<Rental> findOverdue();

    /**
     * Verifie la disponibilite d'un produit pour une periode.
     */
    boolean isProductAvailable(UUID productId, LocalDateTime startDate, LocalDateTime endDate, int quantity);
}
