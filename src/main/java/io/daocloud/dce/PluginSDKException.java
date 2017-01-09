package io.daocloud.dce;


public class PluginSDKException extends RuntimeException {

    private static final long serialVersionUID = 7667768099261650608L;

    public PluginSDKException(String message) {
        super(message);
    }

    public PluginSDKException(String message, Throwable cause) {
        super(message, cause);
    }
}
