package at.shiftcontrol.shiftservice.entity;

import jakarta.persistence.EmbeddedId;

public class VolunteerShiftPreference {
    @EmbeddedId
    private VolunteerShiftPreferenceId id;

    private int preferenceLevel;
}
