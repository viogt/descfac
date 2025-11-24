package com.proto;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import org.apache.commons.fileupload.RequestContext;

public class App {

    public static byte[] zipBytes;

    public static void main(String[] args) throws Exception {
        String portEnv = System.getenv("PORT");
        int port = (portEnv != null) ? Integer.parseInt(portEnv) : 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", ex -> {
            serve(ex, "public/index.html");
        });
        server.createContext("/public", ex -> {
            serve(ex, ex.getRequestURI().getPath().substring(1));
        });
        server.createContext("/upload", ex -> {
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
                App.say(ex, "Use POST");
                return;
            }

            String contentType = ex.getRequestHeaders().getFirst("Content-Type");
            if (contentType == null || !contentType.contains("multipart/form-data")) {
                App.say(ex, "Expected multipart/form-data");
                return;
            }

            // Extract boundary
            String boundary = "--" + contentType.split("boundary=")[1];

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ex.getRequestBody().transferTo(baos);
            String body = baos.toString("ISO-8859-1"); // keep raw bytes intact

            // Split parts
            String[] parts = body.split(boundary);

            for (String part : parts) {
                part = part.trim();
                if (part.isEmpty() || part.equals("--")) continue;

                // Find filename
                String filename = null;
                for (String line : part.split("\r\n")) {
                    if (line.startsWith("Content-Disposition")) {
                        int idx = line.indexOf("filename=\"");
                        if (idx != -1) {
                            filename = line.substring(idx + 10, line.indexOf("\"", idx + 10));
                        }
                        break;
                    }
                }

                if (filename != null && !filename.isEmpty()) {
                    // Extract file content after empty line
                    int pos = part.indexOf("\r\n\r\n");
                    if (pos != -1) {
                        byte[] fileData = part.substring(pos + 4).getBytes("ISO-8859-1");

                        checkZip.replaceFileInArchive(new ByteArrayInputStream(fileData));

                        /*
                         * File uploads = new File("uploads");
                         * if (!uploads.exists())
                         * uploads.mkdirs();
                         * Files.write(new File(uploads, filename).toPath(), fileData);
                         * fileSaved = true;
                         * App.say(ex, "Uploaded: " + filename);
                         */
                        break;
                    }
                }
            }

            App.say(ex, checkZip.Rprt);
        });
        server.createContext("/download", ex -> {
            Headers responseHeaders = ex.getResponseHeaders();
            responseHeaders.set("Content-Type", "application/octet-stream");
            responseHeaders.set("Content-Disposition", "attachment; filename=\"unlocked.xlsx\"");
            int contentLength = App.zipBytes.length;
            responseHeaders.set("Content-Length", String.valueOf(contentLength));
            ex.sendResponseHeaders(200, contentLength);
            try (OutputStream os = ex.getResponseBody()) {
                os.write(App.zipBytes);
                os.close();
            }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("Server started at http://localhost:" + port);
    }

    static void say(HttpExchange ex, String message) throws IOException {
        System.out.println(message);
        byte[] resp = message.getBytes();
        ex.sendResponseHeaders(200, resp.length);
        OutputStream os = ex.getResponseBody();
        os.write(resp);
        os.close();
    }

    static void serve(HttpExchange ex, String file) throws IOException {
        InputStream is = App.class.getClassLoader().getResourceAsStream(file);
        if (is != null) {
            ex.sendResponseHeaders(200, 0);
            is.transferTo(ex.getResponseBody());
            is.close();
            ex.getResponseBody().close();
        } else {
            System.out.println("Resource not found in classpath: " + file);
            ex.sendResponseHeaders(404, 0);
            OutputStream out = ex.getResponseBody();
            out.write("404 Not Found".getBytes());
            out.close();
        }
    }
}

class HttpExchangeRequestContext implements RequestContext {
    private final HttpExchange exchange;

    public HttpExchangeRequestContext(HttpExchange exchange) {
        this.exchange = exchange;
    }

    @Override
    public String getCharacterEncoding() {
        return "UTF-8";
    }

    @Override
    public String getContentType() {
        return exchange.getRequestHeaders().getFirst("Content-Type");
    }

    @Override
    public int getContentLength() {
        return 0; // unknown
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return exchange.getRequestBody();
    }
}
