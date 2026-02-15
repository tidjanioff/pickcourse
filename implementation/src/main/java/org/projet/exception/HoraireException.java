package org.projet.exception;

/**
 * Cette classe permet de personnaliser une exception pour les horaires.
 */
public class HoraireException extends RuntimeException {
    /**
     * Constructeur pour la classe
     * @param message message d'erreur à afficher.
     */
    public HoraireException(String message) {
        super(message);
    }
}
