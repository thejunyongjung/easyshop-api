package org.yearup.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler
{
    // When a @Valid check fails (MethodArgumentNotValidException)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException e)
    {
        Map<String, String> errors = new HashMap<>();

        // getField() (ex. "price"), getDefaultMessage() (ex."must be greater than 0"")
        e.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        // returns 400 status code with a { field : reason } map so the client can see exactly what was invalid
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    // When a controller throws ResponseStatusException (e.g. 404 Not Found, 400 empty cart)
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleStatus(ResponseStatusException e)
    {
        Map<String, Object> body = new HashMap<>();
        body.put("status", e.getStatusCode().value());
        body.put("message", e.getReason());

        // returns status with a JSON body instead of the default error page
        return ResponseEntity.status(e.getStatusCode()).body(body);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AuthorizationDeniedException e)
    {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean loggedIn = auth != null && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken);

        // not logged in -> 401; logged in but wrong role -> 403
        HttpStatus status = loggedIn ? HttpStatus.FORBIDDEN : HttpStatus.UNAUTHORIZED;

        Map<String, Object> body = new HashMap<>();
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", loggedIn
                ? "You do not have permission to perform this action."
                : "You must be logged in to do that.");

        return ResponseEntity.status(status).body(body);
    }
    
}
