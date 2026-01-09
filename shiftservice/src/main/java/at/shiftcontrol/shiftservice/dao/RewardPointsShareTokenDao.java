package at.shiftcontrol.shiftservice.dao;

import java.util.Collection;

import at.shiftcontrol.shiftservice.entity.RewardPointsShareToken;

public interface RewardPointsShareTokenDao extends BasicDao<RewardPointsShareToken, Long> {
    Collection<RewardPointsShareToken> findAll();

    RewardPointsShareToken getByToken(String token);

    boolean existsByToken(String token);
}
