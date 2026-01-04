package at.shiftcontrol.shiftservice.event;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface EventClassifier {
    EventType value();
}
