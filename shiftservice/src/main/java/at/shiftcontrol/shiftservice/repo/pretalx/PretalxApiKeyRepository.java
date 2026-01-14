package at.shiftcontrol.shiftservice.repo.pretalx;

import org.springframework.data.jpa.repository.JpaRepository;

import at.shiftcontrol.lib.entity.PretalxApiKey;

public interface PretalxApiKeyRepository extends JpaRepository<PretalxApiKey, String> {
}
