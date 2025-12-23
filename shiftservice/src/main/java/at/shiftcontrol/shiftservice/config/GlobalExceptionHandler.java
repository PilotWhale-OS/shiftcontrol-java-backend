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
import at.shiftcontrol.lib.exception.NotificationSettingAlreadyExistsException;
import at.shiftcontrol.lib.exception.PartiallyNotFoundException;
import at.shiftcontrol.lib.exception.UnauthorizedException;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Use the @ExceptionHandler annotation to write handler for custom exceptions.
     */
    @ExceptionHandler({NotFoundException.class})
    protected ResponseEntity<Object> handleNotFound(Exception ex, WebRequest request) {
        return handleInternal(ex, request, NOT_FOUND);
    }

    @ExceptionHandler(value = {ValidationException.class})
    protected ResponseEntity<Object> handleValidation(Exception ex, WebRequest request) {
        return handleInternal(ex, request, UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(value = {ConflictException.class})
    protected ResponseEntity<Object> handleConflict(Exception ex, WebRequest request) {
        LOGGER.warn(ex.getMessage());
        var conflictException = (ConflictException) ex;


        //Either respond with DTO (if available) or message
        if (conflictException.hasDto()) {
            return handleExceptionInternal(conflictException, conflictException.getDto(), new HttpHeaders(), CONFLICT, request);
        } else {
            return handleExceptionInternal(conflictException, conflictException.getMessage(), new HttpHeaders(), CONFLICT, request);
        }
    }

    @ExceptionHandler(value = {ForbiddenException.class})
    protected ResponseEntity<Object> handleForbidden(Exception ex, WebRequest request) {
        return handleInternal(ex, request, FORBIDDEN);
    }

    @ExceptionHandler(value = {UnauthorizedException.class})
    protected ResponseEntity<Object> handleUnauthorized(Exception ex, WebRequest request) {
        return handleInternal(ex, request, UNAUTHORIZED);
    }

    @ExceptionHandler(value = {PartiallyNotFoundException.class})
    protected ResponseEntity<Object> handlePartiallyNotFoundErrors(Exception ex, WebRequest request) {
        return handleInternal(ex, request, CONFLICT);
    }

    @ExceptionHandler(NotificationSettingAlreadyExistsException.class)
    public ResponseEntity<Object> handleNotificationSettingAlreadyExistsException(Exception ex, WebRequest request) {
        return handleInternal(ex, request, CONFLICT);
    }

    private ResponseEntity<Object> handleInternal(Exception ex, WebRequest request, HttpStatus status) {
        LOGGER.warn(ex.getMessage());
        return handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), status, request);
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
