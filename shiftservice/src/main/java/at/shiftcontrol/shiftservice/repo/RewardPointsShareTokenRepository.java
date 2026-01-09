package at.shiftcontrol.shiftservice.repo;

import java.util.Optional;

import at.shiftcontrol.shiftservice.entity.RewardPointsShareToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RewardPointsShareTokenRepository extends JpaRepository<RewardPointsShareToken, Long> {
    Optional<RewardPointsShareToken> findByToken(String token);

    boolean existsByToken(String token);
}
