package net.sinedkadis.terracompositio.util;

public class BindException extends Exception {

    public static final String emptyMessage = "empty";

    public BindException() {
        super(emptyMessage);
    }

    public BindException(String message) {
        super(message);
    }
}
