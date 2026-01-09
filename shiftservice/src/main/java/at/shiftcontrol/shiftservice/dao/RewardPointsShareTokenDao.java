package at.shiftcontrol.shiftservice.dao;

import at.shiftcontrol.shiftservice.entity.RewardPointsShareToken;

public interface RewardPointsShareTokenDao extends BasicDao<RewardPointsShareToken, Long> {
    RewardPointsShareToken getByCode(String code);

    boolean existsByCode(String code);
}
