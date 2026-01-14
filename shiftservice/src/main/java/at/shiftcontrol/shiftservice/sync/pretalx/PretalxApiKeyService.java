package at.shiftcontrol.shiftservice.sync.pretalx;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.entity.PretalxApiKey;
import at.shiftcontrol.lib.exception.ConflictException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.lib.exception.ValidationException;
import at.shiftcontrol.shiftservice.dto.PretalxApiKeyDetailsDto;
import at.shiftcontrol.shiftservice.dto.PretalxApiKeyDto;
import at.shiftcontrol.shiftservice.repo.pretalx.PretalxApiKeyRepository;

@RequiredArgsConstructor
@Service
public class PretalxApiKeyService {
    private final PretalxApiKeyRepository pretalxApiKeyRepository;
    private final PretalxApiKeyLoader pretalxApiKeyLoader;

    public List<PretalxApiKeyDetailsDto> getAllKeys() {
        pretalxApiKeyLoader.refreshApiKeys();
        var apiKeys = pretalxApiKeyLoader.getActiveApiKeys();

        return apiKeys.stream().map(PretalxApiKeyDetailsDto::of).toList();
    }

    public PretalxApiKeyDetailsDto addKey(PretalxApiKeyDto apiKeyDto) {
        if (pretalxApiKeyRepository.existsById(apiKeyDto.getApiKey())) {
            throw new ConflictException("API key already exists");
        }

        var apiKey = PretalxApiKey.builder()
            .apiKey(apiKeyDto.getApiKey())
            .pretalxHost(apiKeyDto.getPretalxHost())
            .build();
        pretalxApiKeyRepository.save(apiKey);

        pretalxApiKeyLoader.refreshApiKeys();
        var addedKey = pretalxApiKeyLoader.getActiveApiKeys().stream()
            .filter(key -> key.getApiKey().equals(apiKeyDto.getApiKey()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Added API key not found in active keys"));

        if (addedKey == null) {
            throw new ValidationException("Added API key is not valid or has no accessible events");
        }

        return PretalxApiKeyDetailsDto.of(addedKey);
    }

    public void removeKey(String apiKey) {
        if (!pretalxApiKeyRepository.existsById(apiKey)) {
            throw new NotFoundException("API key does not exist");
        }

        pretalxApiKeyRepository.deleteById(apiKey);
        pretalxApiKeyLoader.refreshApiKeys();
    }
}
