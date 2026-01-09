package at.shiftcontrol.shiftservice.dao.impl;

import java.util.Collection;
import java.util.Optional;

import at.shiftcontrol.shiftservice.dao.RewardPointsShareTokenDao;
import at.shiftcontrol.shiftservice.entity.RewardPointsShareToken;
import at.shiftcontrol.shiftservice.repo.RewardPointsShareTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RewardPointsShareTokenDaoImpl implements RewardPointsShareTokenDao {
    private final RewardPointsShareTokenRepository rewardPointsShareTokenRepository;

    @Override
    public String getName() {
        return "ShiftPlanInvite";
    }

    @Override
    public Optional<RewardPointsShareToken> findById(Long id) {
        return rewardPointsShareTokenRepository.findById(id);
    }

    @Override
    public RewardPointsShareToken save(RewardPointsShareToken entity) {
        return rewardPointsShareTokenRepository.save(entity);
    }

    @Override
    public Collection<RewardPointsShareToken> saveAll(Collection<RewardPointsShareToken> entities) {
        return rewardPointsShareTokenRepository.saveAll(entities);
    }

    @Override
    public void delete(RewardPointsShareToken entity) {
        rewardPointsShareTokenRepository.delete(entity);
    }

    @Override
    public Collection<RewardPointsShareToken> findAll() {
        return rewardPointsShareTokenRepository.findAll();
    }

    @Override
    public boolean existsByToken(String token) {
        return rewardPointsShareTokenRepository.existsByToken(token);
    }

    @Override
    public boolean existsByName(String name) {
        return rewardPointsShareTokenRepository.existsByName(name);
    }
}
