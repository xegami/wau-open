package com.xegami.wau.api.service.exception;

public enum Exceptions {

    WRONG_PASSWORD("Contraseña incorrecta."),
    NICKNAME_EXISTS("Tu nombre ya existe en la sala."),
    NO_AVAILABLE_ROOMS("No hay salas disponibles."),
    UNKNOWN_ROOM("La sala no existe."),
    MIN_LOBBY_SIZE("No se puede jugar sin al menos 3 jugadores."),
    MAX_LOBBY_SIZE("La sala está llena."),
    INVALID_LINK("El link no es válido o ha expirado.");

    private String value;

    Exceptions(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}