package at.shiftcontrol.shiftservice.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import at.shiftcontrol.lib.entity.TrustAlert;

public interface TrustAlertRepository extends JpaRepository<TrustAlert, Long> {
}
