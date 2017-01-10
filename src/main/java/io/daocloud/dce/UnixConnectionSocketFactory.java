package io.daocloud.dce;


import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;

import jnr.unixsocket.UnixSocketAddress;

import org.apache.http.HttpHost;
import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.protocol.HttpContext;

/**
 * Provides a ConnectionSocketFactory for connecting Apache HTTP clients to Unix sockets.
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class UnixConnectionSocketFactory implements ConnectionSocketFactory {

    private File socketFile;

    public UnixConnectionSocketFactory(final URI socketUri) {
        super();

        final String filename = socketUri.toString()
                .replaceAll("^unix:///", "unix://localhost/")
                .replaceAll("^unix://localhost", "");

        this.socketFile = new File(filename);
    }

    public static URI sanitizeUri(final URI uri) {
        if (uri.getScheme().equals("unix")) {
            return URI.create("unix://localhost:80");
        } else {
            return uri;
        }
    }

    public Socket createSocket(final HttpContext context) throws IOException {
        return new ApacheUnixSocket();
    }

    public Socket connectSocket(final int connectTimeout,
                                final Socket socket,
                                final HttpHost host,
                                final InetSocketAddress remoteAddress,
                                final InetSocketAddress localAddress,
                                final HttpContext context) throws IOException {
        try {
            socket.connect(new UnixSocketAddress(socketFile), connectTimeout);
        } catch (SocketTimeoutException e) {
            throw new ConnectTimeoutException(e, null, remoteAddress.getAddress());
        }

        return socket;
    }
}