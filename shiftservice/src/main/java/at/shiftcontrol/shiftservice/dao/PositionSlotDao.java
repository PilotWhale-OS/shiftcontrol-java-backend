package at.shiftcontrol.shiftservice.dao;

import lombok.NonNull;

import at.shiftcontrol.shiftservice.entity.PositionSlot;

public interface PositionSlotDao extends BasicDao<PositionSlot, Long> {
    void setPreference(@NonNull String volunteerId, long positionSlotId, int preference);

    int getPreference(@NonNull String volunteerId, long positionSlotId);
}
