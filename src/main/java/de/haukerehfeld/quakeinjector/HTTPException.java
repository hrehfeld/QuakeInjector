package de.haukerehfeld.quakeinjector;

public class HTTPException extends RuntimeException {
    private final int statusCode;

    public HTTPException(int statusCode) {
        super();
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
