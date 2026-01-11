package at.shiftcontrol.shiftservice.dao;

import java.util.Collection;

import at.shiftcontrol.lib.entity.TrustAlert;

public interface TrustAlertDao {

    Collection<TrustAlert> findAllByEventId(long eventId);
}
