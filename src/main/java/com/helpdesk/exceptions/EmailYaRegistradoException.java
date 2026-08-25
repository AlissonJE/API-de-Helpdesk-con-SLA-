package com.helpdesk.exceptions;

public class EmailYaRegistradoException extends RuntimeException {
    public EmailYaRegistradoException(String email) {
        super("El email '" + email + "' ya esta registrado");
    }
}
