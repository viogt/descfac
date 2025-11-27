package com.proto;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SSE {

    private static final Set<OutputStream> clients =
            Collections.newSetFromMap(new ConcurrentHashMap<>());

    public static void addClient(OutputStream out) {
        clients.add(out);
    }

    public static void removeClient(OutputStream out) {
        clients.remove(out);
    }

    public static void broadcast(String message) {
        String sseMessage = "data: " + message.replaceFirst(" "," <font color='green'>&#x2714;</font>&nbsp;") + "\n\n";
        byte[] data = sseMessage.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        clients.forEach(out -> {
            try {
                out.write(data);
                out.flush();
            } catch (IOException e) {
                // client closed connection → remove
                removeClient(out);
                try { out.close(); } catch (Exception ignore) {}
            }
        });
    }
}
