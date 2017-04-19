package com.forgenz.horses.database;

public class DatabaseConnectException extends Exception {
    private static final long serialVersionUID = -6945815085008439500L;

    public DatabaseConnectException(String message) {
        super(message);
    }
}