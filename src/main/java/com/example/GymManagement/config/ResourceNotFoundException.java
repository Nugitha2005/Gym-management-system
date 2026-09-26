package com.example.GymManagement.config;

// Thrown when a requested record does not exist.
// GlobalExceptionHandler turns it into an HTTP 404 Not Found response.
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}