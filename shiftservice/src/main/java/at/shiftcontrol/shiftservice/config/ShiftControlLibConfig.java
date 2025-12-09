package at.shiftcontrol.shiftservice.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "at.shiftcontrol.lib.auth")
public class ShiftControlLibConfig {
}
