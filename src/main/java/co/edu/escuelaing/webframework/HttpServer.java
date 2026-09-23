package co.edu.escuelaing.webframework;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


public class HttpServer {

    private final Router router;
    private final StaticFileService staticFileService;
    private final int port;
    private volatile boolean running = false;

    public HttpServer(Router router, String staticFilesRoot, int port) {
        this.router = router;
        this.staticFileService = new StaticFileService(staticFilesRoot);
        this.port = port;
    }

    public void start() throws IOException {
        running = true;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server listening on port " + port);

            while (running) {
                Socket clientSocket;
                try {
                    clientSocket = serverSocket.accept();
                } catch (IOException acceptError) {
                    if (!running) {
                        break;
                    }
                    System.err.println("Accept error: " + acceptError.getMessage());
                    continue;
                }
                try (Socket socket = clientSocket) {
                    handleConnection(socket);
                } catch (IOException handlingError) {
                    // A single malformed/aborted connection must not bring
                    // the whole server down.
                    System.err.println("Error handling connection: " + handlingError.getMessage());
                }
            }
        }
        System.out.println("Server stopped gracefully.");
    }

    public void stop() {
        running = false;
    }

    private void handleConnection(Socket socket) throws IOException {
        socket.setSoTimeout(10_000);
        InputStream rawIn = socket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(rawIn, StandardCharsets.UTF_8));
        OutputStream out = socket.getOutputStream();

        String requestLine = reader.readLine();
        if (requestLine == null || requestLine.isBlank()) {
            return;
        }

        // Drain and ignore headers; this lab does not need them.
        String header;
        while ((header = reader.readLine()) != null && !header.isEmpty()) {
            // no-op
        }

        String[] parts = requestLine.split(" ");
        if (parts.length < 3) {
            sendPlainText(out, 400, "Bad Request", "Malformed request line.");
            return;
        }

        String method = parts[0];
        String rawTarget = parts[1];

        if (!method.equals("GET")) {
            sendPlainText(out, 405, "Method Not Allowed", "Only GET is supported.");
            return;
        }

        String path;
        String query;
        int qIndex = rawTarget.indexOf('?');
        if (qIndex >= 0) {
            path = rawTarget.substring(0, qIndex);
            query = rawTarget.substring(qIndex + 1);
        } else {
            path = rawTarget;
            query = "";
        }

        Map<String, String> params = parseQuery(query);
        Route route = router.resolve(path);

        if (route != null) {
            handleDynamicRoute(out, route, path, params);
        } else {
            handleStaticResource(out, path);
        }
    }

    private void handleDynamicRoute(OutputStream out, Route route, String path, Map<String, String> params)
            throws IOException {
        Request request = new Request(path, params);
        Response response = new Response();
        String body;
        try {
            body = route.handle(request, response);
        } catch (Exception routeError) {
            System.err.println("Error executing route '" + path + "': " + routeError.getMessage());
            sendPlainText(out, 500, "Internal Server Error", "Internal server error.");
            return;
        }
        if (body == null) {
            body = "";
        }
        sendBytes(out, response.getStatus(), statusText(response.getStatus()),
                response.getContentType(), body.getBytes(StandardCharsets.UTF_8));
    }

    private void handleStaticResource(OutputStream out, String path) throws IOException {
        byte[] content = staticFileService.read(path);
        if (content == null) {
            sendPlainText(out, 404, "Not Found", "404 Not Found");
            return;
        }
        String contentType = staticFileService.contentTypeFor(path);
        sendBytes(out, 200, "OK", contentType, content);
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query.isBlank()) {
            return map;
        }
        for (String pair : query.split("&")) {
            int eq = pair.indexOf('=');
            String key = eq >= 0 ? pair.substring(0, eq) : pair;
            String value = eq >= 0 ? pair.substring(eq + 1) : "";
            try {
                key = URLDecoder.decode(key, StandardCharsets.UTF_8);
                value = URLDecoder.decode(value, StandardCharsets.UTF_8);
            } catch (Exception ignored) {
                // keep raw values if decoding fails
            }
            map.put(key, value);
        }
        return map;
    }

    private void sendPlainText(OutputStream out, int status, String reason, String message) throws IOException {
        sendBytes(out, status, reason, "text/plain; charset=utf-8", message.getBytes(StandardCharsets.UTF_8));
    }

    private void sendBytes(OutputStream out, int status, String reason, String contentType, byte[] body)
            throws IOException {
        StringBuilder headers = new StringBuilder();
        headers.append("HTTP/1.1 ").append(status).append(' ').append(reason).append("\r\n");
        headers.append("Content-Type: ").append(contentType).append("\r\n");
        headers.append("Content-Length: ").append(body.length).append("\r\n");
        headers.append("Connection: close\r\n");
        headers.append("\r\n");

        out.write(headers.toString().getBytes(StandardCharsets.UTF_8));
        out.write(body);
        out.flush();
    }

    private String statusText(int status) {
        return switch (status) {
            case 200 -> "OK";
            case 400 -> "Bad Request";
            case 404 -> "Not Found";
            case 405 -> "Method Not Allowed";
            case 500 -> "Internal Server Error";
            default -> "Unknown";
        };
    }
}