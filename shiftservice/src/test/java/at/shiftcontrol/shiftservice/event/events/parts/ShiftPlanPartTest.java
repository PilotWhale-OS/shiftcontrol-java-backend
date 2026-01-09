package at.shiftcontrol.shiftservice.event.events.parts;

import org.junit.jupiter.api.Test;

import at.shiftcontrol.lib.entity.ShiftPlan;
import at.shiftcontrol.lib.event.events.parts.ShiftPlanPart;
import at.shiftcontrol.lib.type.LockStatus;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShiftPlanPartTest {

    @Test
    void of() {
        // Arrange
        ShiftPlan shiftPlan = new ShiftPlan();
        shiftPlan.setId(1L);
        shiftPlan.setName("Test Shift Plan");
        shiftPlan.setShortDescription("Short Description");
        shiftPlan.setLongDescription("Long Description");
        shiftPlan.setLockStatus(LockStatus.LOCKED);

        // Act
        ShiftPlanPart shiftPlanPart = ShiftPlanPart.of(shiftPlan);

        // Assert
        assertEquals(String.valueOf(shiftPlan.getId()), shiftPlanPart.getId());
        assertEquals(shiftPlan.getName(), shiftPlanPart.getName());
        assertEquals(shiftPlan.getShortDescription(), shiftPlanPart.getShortDescription());
        assertEquals(shiftPlan.getLongDescription(), shiftPlanPart.getLongDescription());
        assertEquals(shiftPlan.getLockStatus(), shiftPlanPart.getLockStatus());
    }

    @Test
    void of_throwsNullPointerException_forNullShiftPlan() {
        assertThrows(NullPointerException.class, () -> {
            ShiftPlanPart.of(null);
        });
    }
}

