package com.sportsaas.inventory.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service de gestion des reservations.
 */
public interface ReservationService {

    /**
     * Cree une reservation pour un produit.
     */
    Reservation create(UUID productId, int quantity, Duration duration, String reference, UUID customerId);

    /**
     * Recupere une reservation par ID.
     */
    Optional<Reservation> findById(UUID id);

    /**
     * Liste les reservations d'un produit.
     */
    Page<Reservation> findByProductId(UUID productId, Pageable pageable);

    /**
     * Liste les reservations d'un client.
     */
    List<Reservation> findByCustomerId(UUID customerId);

    /**
     * Liste les reservations actives d'un client.
     */
    List<Reservation> findActiveByCustomerId(UUID customerId);

    /**
     * Liste les reservations par statut.
     */
    Page<Reservation> findByStatus(ReservationStatus status, Pageable pageable);

    /**
     * Confirme une reservation (commande validee).
     */
    Reservation confirm(UUID id);

    /**
     * Annule une reservation et libere le stock.
     */
    Reservation cancel(UUID id);

    /**
     * Prolonge une reservation.
     */
    Reservation extend(UUID id, Duration additionalDuration);

    /**
     * Traite les reservations expirees.
     */
    int processExpiredReservations();

    /**
     * Libere une reservation confirmee (produit livre).
     */
    Reservation release(UUID id);
}
