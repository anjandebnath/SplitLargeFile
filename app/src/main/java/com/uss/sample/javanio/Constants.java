package com.uss.sample.javanio;

public class Constants {

    public static final String INSTANTIATION_NOT_ALLOWED = "Instantiation not allowed";
    public static final long TRANSFER_MAX_SIZE = (1024 * 1024);
    public static final int BUFFER_SIZE = 2048;

    public static final String END_MESSAGE_MARKER = ":END";
    public static final String MESSAGE_DELIMITTER = "#";

    public static final String CONFIRMATION = "OK";

    private Constants() {
        throw new IllegalStateException(INSTANTIATION_NOT_ALLOWED);
    }
}
