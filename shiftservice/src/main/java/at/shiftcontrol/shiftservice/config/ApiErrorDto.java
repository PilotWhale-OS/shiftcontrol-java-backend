package at.shiftcontrol.shiftservice.config;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ApiErrorDto {
    String message;
}
