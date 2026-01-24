package at.shiftcontrol.shiftservice.event.events;

import org.junit.jupiter.api.Test;

import at.shiftcontrol.lib.entity.Activity;
import at.shiftcontrol.lib.event.events.ActivityEvent;
import at.shiftcontrol.lib.event.events.parts.ActivityPart;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class ActivityEventTest {

    @Test
    void ofInternal() {
        Activity activity = mock(Activity.class);
        String routingKey = "routingKey";

        ActivityPart activityPart = mock(ActivityPart.class);
        try (var activityPartMock = org.mockito.Mockito.mockStatic(ActivityPart.class)) {
            activityPartMock.when(() -> ActivityPart.of(activity)).thenReturn(activityPart);

            ActivityEvent activityEvent = ActivityEvent.ofInternal(null, routingKey, activity);

            assertEquals(activityPart, activityEvent.getActivity());
            assertEquals(routingKey, activityEvent.getRoutingKey());
        }
    }
}

