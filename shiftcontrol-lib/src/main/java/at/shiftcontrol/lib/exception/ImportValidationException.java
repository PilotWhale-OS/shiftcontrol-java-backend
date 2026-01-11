package at.shiftcontrol.lib.exception;

import java.util.List;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
@Getter
public class ImportValidationException extends RuntimeException {
    private final List<String> errors;

    public ImportValidationException(List<String> errors) {
        super(buildMessage(errors));
        this.errors = List.copyOf(errors);
    }

    private static String buildMessage(List<String> errors) {
        if (errors == null || errors.isEmpty()) {
            return "Import validation failed";
        }
        return "Import validation failed (" + errors.size() + " error(s))";
    }
}
