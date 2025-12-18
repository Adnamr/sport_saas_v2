package com.sportsaas.common.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Utilitaires pour les chaînes de caractères.
 */
public final class StringUtils {

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    private StringUtils() {
    }

    /**
     * Génère un slug URL-friendly à partir d'une chaîne.
     */
    public static String slugify(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }

        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        String withoutDiacritics = DIACRITICS.matcher(normalized).replaceAll("");
        String withoutWhitespace = WHITESPACE.matcher(withoutDiacritics).replaceAll("-");
        String slug = NON_LATIN.matcher(withoutWhitespace).replaceAll("");

        return slug.toLowerCase(Locale.FRENCH)
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    /**
     * Tronque une chaîne à une longueur maximale.
     */
    public static String truncate(String input, int maxLength) {
        if (input == null) {
            return null;
        }
        if (input.length() <= maxLength) {
            return input;
        }
        return input.substring(0, maxLength);
    }

    /**
     * Tronque avec ellipsis.
     */
    public static String truncateWithEllipsis(String input, int maxLength) {
        if (input == null) {
            return null;
        }
        if (input.length() <= maxLength) {
            return input;
        }
        return input.substring(0, maxLength - 3) + "...";
    }
}
