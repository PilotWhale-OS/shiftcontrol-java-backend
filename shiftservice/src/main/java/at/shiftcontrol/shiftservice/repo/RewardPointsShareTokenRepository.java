package at.shiftcontrol.shiftservice.repo;

import at.shiftcontrol.shiftservice.entity.RewardPointsShareToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RewardPointsShareTokenRepository extends JpaRepository<RewardPointsShareToken, Long> {
    boolean existsByToken(String token);
}
