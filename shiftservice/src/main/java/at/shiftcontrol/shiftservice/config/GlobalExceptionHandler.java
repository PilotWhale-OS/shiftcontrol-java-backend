package at.shiftcontrol.shiftservice.config;

import java.lang.invoke.MethodHandles;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.xml.bind.ValidationException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.shiftcontrol.lib.exception.ConflictException;
import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.lib.exception.PartiallyNotFoundException;
import at.shiftcontrol.lib.exception.UnauthorizedException;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Use the @ExceptionHandler annotation to write handler for custom exceptions.
     */
    @ExceptionHandler({NotFoundException.class})
    protected ResponseEntity<Object> handleNotFound(Exception ex, WebRequest request) {
        LOGGER.warn(ex.getMessage());
        return handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(value = {ValidationException.class})
    protected ResponseEntity<Object> handleValidation(Exception ex, WebRequest request) {
        LOGGER.warn(ex.getMessage());
        return handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.UNPROCESSABLE_ENTITY, request);
    }

    @ExceptionHandler(value = {ConflictException.class})
    protected ResponseEntity<Object> handleConflict(Exception ex, WebRequest request) {
        LOGGER.warn(ex.getMessage());
        var conflictException = (ConflictException) ex;


        //Either respond with DTO (if available) or message
        if (conflictException.hasDto()) {
            return handleExceptionInternal(conflictException, conflictException.getDto(), new HttpHeaders(), HttpStatus.CONFLICT, request);
        } else {
            return handleExceptionInternal(conflictException, conflictException.getMessage(), new HttpHeaders(), HttpStatus.CONFLICT, request);
        }
    }

    @ExceptionHandler(value = {ForbiddenException.class})
    protected ResponseEntity<Object> handleForbidden(Exception ex, WebRequest request) {
        LOGGER.warn(ex.getMessage());
        return handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler(value = {UnauthorizedException.class})
    protected ResponseEntity<Object> handleUnauthorized(Exception ex, WebRequest request) {
        LOGGER.warn(ex.getMessage());
        return handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.UNAUTHORIZED, request);
    }

    @ExceptionHandler(value = {PartiallyNotFoundException.class})
    protected ResponseEntity<Object> handlePartiallyNotFoundErrors(Exception ex, WebRequest request) {
        LOGGER.warn(ex.getMessage());
        return handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.CONFLICT, request);
    }

    /**
     * Override methods from ResponseEntityExceptionHandler to send a customized HTTP response for a know exception
     * from e.g. Spring
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        //Get all errors
        List<String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(err -> err.getField() + " " + err.getDefaultMessage())
            .collect(Collectors.toList());
        body.put("Validation errors", errors);

        return new ResponseEntity<>(body.toString(), headers, status);
    }
}
