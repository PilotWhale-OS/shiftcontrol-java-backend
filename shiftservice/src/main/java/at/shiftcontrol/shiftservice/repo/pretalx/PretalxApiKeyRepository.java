package at.shiftcontrol.shiftservice.repo.pretalx;

import org.springframework.data.jpa.repository.JpaRepository;

import at.shiftcontrol.shiftservice.entity.pretalx.PretalxApiKey;

public interface PretalxApiKeyRepository extends JpaRepository<PretalxApiKey, String> {
}
