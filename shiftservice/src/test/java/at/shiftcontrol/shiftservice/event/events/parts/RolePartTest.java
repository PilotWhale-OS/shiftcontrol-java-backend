package at.shiftcontrol.shiftservice.event.events.parts;

import org.junit.jupiter.api.Test;

import at.shiftcontrol.lib.entity.Role;
import at.shiftcontrol.lib.entity.ShiftPlan;
import at.shiftcontrol.lib.event.events.parts.RolePart;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RolePartTest {

    @Test
    void of() {
        // Arrange
        ShiftPlan shiftPlan = new ShiftPlan();
        shiftPlan.setId(1L);

        Role role = new Role();
        role.setId(1L);
        role.setShiftPlan(shiftPlan);
        role.setName("Test Role");
        role.setDescription("Test Description");
        role.setSelfAssignable(true);

        // Act
        RolePart rolePart = RolePart.of(role);

        // Assert
        assertEquals(String.valueOf(role.getId()), rolePart.getId());
        assertEquals(String.valueOf(role.getShiftPlan().getId()), rolePart.getShiftPlanId());
        assertEquals(role.getName(), rolePart.getName());
        assertEquals(role.getDescription(), rolePart.getDescription());
        assertEquals(role.isSelfAssignable(), rolePart.isSelfAssignable());
    }

    @Test
    void of_throwsNullPointerException_forNullRole() {
        assertThrows(NullPointerException.class, () -> {
            RolePart.of(null);
        });
    }
}

