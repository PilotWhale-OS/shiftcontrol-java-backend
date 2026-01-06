package at.shiftcontrol.shiftservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ShiftSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShiftSystemApplication.class, args);
    }
}
