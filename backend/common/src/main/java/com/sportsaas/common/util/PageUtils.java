package com.sportsaas.common.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Utilitaires pour la pagination.
 */
public final class PageUtils {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private PageUtils() {
    }

    /**
     * Crée un Pageable avec des valeurs par défaut sécurisées.
     */
    public static Pageable of(Integer page, Integer size) {
        int pageNum = (page != null && page >= 0) ? page : DEFAULT_PAGE;
        int pageSize = (size != null && size > 0) ? Math.min(size, MAX_SIZE) : DEFAULT_SIZE;
        return PageRequest.of(pageNum, pageSize);
    }

    /**
     * Crée un Pageable avec tri.
     */
    public static Pageable of(Integer page, Integer size, Sort sort) {
        int pageNum = (page != null && page >= 0) ? page : DEFAULT_PAGE;
        int pageSize = (size != null && size > 0) ? Math.min(size, MAX_SIZE) : DEFAULT_SIZE;
        return PageRequest.of(pageNum, pageSize, sort);
    }

    /**
     * Crée un Pageable avec tri par défaut sur createdAt DESC.
     */
    public static Pageable ofCreatedDesc(Integer page, Integer size) {
        return of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    }
}
