package co.edu.escuelaing.webframework;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;

public class StaticFileService {

    private static final Map<String, String> CONTENT_TYPES = Map.ofEntries(
            Map.entry("html", "text/html; charset=utf-8"),
            Map.entry("htm", "text/html; charset=utf-8"),
            Map.entry("js", "application/javascript; charset=utf-8"),
            Map.entry("css", "text/css; charset=utf-8"),
            Map.entry("png", "image/png"),
            Map.entry("jpg", "image/jpeg"),
            Map.entry("jpeg", "image/jpeg"),
            Map.entry("gif", "image/gif"),
            Map.entry("ico", "image/x-icon"),
            Map.entry("svg", "image/svg+xml"),
            Map.entry("txt", "text/plain; charset=utf-8"),
            Map.entry("json", "application/json; charset=utf-8")
    );

    private final String rootFolder;
    public StaticFileService(String rootFolder) {
        String normalized = rootFolder;
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        this.rootFolder = normalized;
    }

    public byte[] read(String requestPath) throws IOException {
        String sanitized = sanitize(requestPath);
        if (sanitized == null) {
            return null;
        }
        String classpathResource = rootFolder + sanitized;

        try (InputStream in = StaticFileService.class.getClassLoader()
                .getResourceAsStream(classpathResource)) {
            if (in == null) {
                return null;
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            in.transferTo(buffer);
            return buffer.toByteArray();
        }
    }

    public String contentTypeFor(String requestPath) {
        String path = requestPath.equals("/") ? "/index.html" : requestPath;
        int dot = path.lastIndexOf('.');
        if (dot < 0 || dot == path.length() - 1) {
            return "application/octet-stream";
        }
        String extension = path.substring(dot + 1).toLowerCase(Locale.ROOT);
        return CONTENT_TYPES.getOrDefault(extension, "application/octet-stream");
    }
    
    private String sanitize(String requestPath) {
        String path = requestPath.equals("/") ? "/index.html" : requestPath;
        if (path.contains("..")) {
            return null;
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return path;
    }
}