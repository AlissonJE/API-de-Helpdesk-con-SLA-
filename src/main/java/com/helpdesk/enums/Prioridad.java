package com.helpdesk.enums;

public enum Prioridad {
    BAJA(72),
    MEDIA(24),
    ALTA(4);

    private final int horasSla;

    Prioridad(int horasSla) {
        this.horasSla = horasSla;
    }

    public int getHorasSla() {
        return horasSla;
    }
}
