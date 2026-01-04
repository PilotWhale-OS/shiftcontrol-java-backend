package at.shiftcontrol.shiftservice.event;

import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoutingKeysTest {

    @Test
    void formatStrict_replacesPlaceholders() {
        var template = "hello.{name}.end";
        var result = RoutingKeys.formatStrict(template, Map.of("name", "world"));
        assertEquals("hello.world.end", result);
    }

    @Test
    void formatStrict_missingValue_throws() {
        var template = "{a}.{b}";
        var ex = assertThrows(IllegalArgumentException.class, () ->
                RoutingKeys.formatStrict(template, Map.of("a", "1")));
        assertTrue(ex.getMessage().contains("Missing value for {b}"));
    }

    @Test
    void formatStrict_handlesSpecialCharactersInValues() {
        // Values with $ and backslashes must be quoted correctly by the implementation
        var template = "pref.{val}.suf";
        var value = "$5\\path"; // literal value contains a dollar and a backslash
        var result = RoutingKeys.formatStrict(template, Map.of("val", value));
        assertEquals("pref.$5\\path.suf", result);
    }

    @Test
    void formatStrict_repeatedPlaceholder() {
        var template = "{x}.{x}";
        var result = RoutingKeys.formatStrict(template, Map.of("x", "1"));
        assertEquals("1.1", result);
    }

    @Test
    void formatStrict_noPlaceholders_returnsSame() {
        var template = "static.routing.key";
        var result = RoutingKeys.formatStrict(template, Map.of());
        assertEquals("static.routing.key", result);
    }

    @Test
    void formatStrict_usesAssignmentSwitchCompletedConstant() {
        var template = RoutingKeys.ASSIGNMENT_SWITCH_COMPLETED;
        var result = RoutingKeys.formatStrict(template, Map.of(
                "requestedVolunteerId", "req",
                "offeringVolunteerId", "off"));
        assertEquals("assignment.switch.completed.req.off", result);
    }

}
