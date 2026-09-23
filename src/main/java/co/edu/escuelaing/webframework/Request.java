package co.edu.escuelaing.webframework;

import java.util.Collections;
import java.util.Map;


public class Request {

    private final String path;
    private final Map<String, String> queryParams;

    public Request(String path, Map<String, String> queryParams) {
        this.path = path;
        this.queryParams = Collections.unmodifiableMap(queryParams);
    }

    public String getValue(String key) {
        return queryParams.get(key);
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public String getPath() {
        return path;
    }
}