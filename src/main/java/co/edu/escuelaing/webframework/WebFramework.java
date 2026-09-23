package co.edu.escuelaing.webframework;

import java.io.IOException;


public final class WebFramework {

    private static final Router router = new Router();
    private static String staticFilesRoot = "/webroot";
    private static HttpServer server;

    private WebFramework() {
    }

    public static void staticfiles(String path) {
        staticFilesRoot = path;
    }

    public static void get(String path, Route route) {
        router.addGetRoute(path, route);
    }

    public static void start() throws IOException {
        start(resolvePort());
    }

    public static void start(int port) throws IOException {
        server = new HttpServer(router, staticFilesRoot, port);
        server.start();
    }

    public static void stop() {
        if (server != null) {
            server.stop();
        }
    }

    private static int resolvePort() {
        String portValue = System.getenv("PORT");
        if (portValue == null || portValue.isBlank()) {
            return 8080;
        }
        return Integer.parseInt(portValue.trim());
    }
}