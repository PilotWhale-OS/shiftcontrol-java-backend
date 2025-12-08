package at.shiftcontrol.shiftservice.entity;

import jakarta.persistence.EmbeddedId;

public class PositionSlotPreference {
    @EmbeddedId
    private PositionSlotPreferenceId id;

    private int preferenceLevel;
}
