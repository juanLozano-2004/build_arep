package co.edu.escuelaing.webframework;

import java.util.HashMap;
import java.util.Map;

public class Router {

    private final Map<String, Route> getRoutes = new HashMap<>();

    public void addGetRoute(String path, Route route) {
        getRoutes.put(path, route);
    }

    public Route resolve(String path) {
        return getRoutes.get(path);
    }
}