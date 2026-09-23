package co.edu.escuelaing.webframework;

public class Response {

    private int status = 200;
    private String contentType = "text/plain; charset=utf-8";

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}