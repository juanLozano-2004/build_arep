package co.edu.escuelaing.app;

import static co.edu.escuelaing.webframework.WebFramework.get;
import static co.edu.escuelaing.webframework.WebFramework.start;
import static co.edu.escuelaing.webframework.WebFramework.staticfiles;
import static co.edu.escuelaing.webframework.WebFramework.stop;


public class Application {

    public static void main(String[] args) throws Exception {

        staticfiles("/webroot");

        get("/hello", (req, resp) -> {
            String name = req.getValue("name");
            if (name == null || name.isBlank()) {
                name = "world";
            }
            String greetingPrefix = System.getenv().getOrDefault("GREETING_PREFIX", "Hello");
            return greetingPrefix + " " + name;
        });

        get("/pi", (req, resp) -> String.valueOf(Math.PI));

        get("/greeting-with-language", (req, resp) -> {
            String name = req.getValue("name");
            String language = req.getValue("language");
            if (name == null || name.isBlank()) {
                name = "world";
            }
            if (language == null || language.isBlank()) {
                language = "en";
            }
            return "Hello " + name + " (language=" + language + ")";
        });

        String environment = System.getenv().getOrDefault("APP_ENV", "development");
        if (environment.equals("development")) {
            get("/shutdown", (req, resp) -> {
                stop();
                return "Server will stop after this response.";
            });
        }

        start();
    }
}