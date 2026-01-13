package at.shiftcontrol.shiftservice.sync.pretalx;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import at.shiftcontrol.lib.entity.PretalxApiKey;
import at.shiftcontrol.lib.event.events.PretalxApiKeyInvalidEvent;
import at.shiftcontrol.pretalxclient.model.EventList;
import at.shiftcontrol.shiftservice.repo.pretalx.PretalxApiKeyRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class PretalxApiKeyLoader {
    private final PretalxApiSupplier pretalxApiSupplier;
    private final PretalxApiKeyRepository pretalxApiKeyRepository;
    private final ApplicationEventPublisher eventPublisher;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    // Event slug to API key cache
    private final Map<String, String> apiKeyCache = new HashMap<>();
    private final Set<PretalxApiKeyData> activeApiKeys = new HashSet<>();

    public void refreshApiKeys() {
        lock.writeLock().lock();
        try {
            refreshApiKeysInternal();
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void refreshApiKeysInternal() {
        activeApiKeys.clear();
        apiKeyCache.clear();

        var apiKeys = pretalxApiKeyRepository.findAll();
        apiKeys = List.of(PretalxApiKey.builder().apiKey("hvha5l5clxescv75isqvs875j1gnpfn29r9hvy4r99w2txggv67lcvw3ggusafu4").build());
        for (var apiKey : apiKeys) {
            var eventsApi = pretalxApiSupplier.eventsApi(apiKey);
            var roomsApi = pretalxApiSupplier.roomsApi(apiKey);
            try {
                var allEvents = eventsApi.apiEventsList(null, null, null);

                //Verify access to rooms endpoint for each event
                var accessibleEvents = allEvents.stream().filter(event -> {
                    try {
                        roomsApi.roomsList(event.getSlug(), 1, null, 0, null);
                        return true;
                    } catch (RestClientResponseException e) {
                        if (e.getStatusCode().isSameCodeAs(HttpStatus.UNAUTHORIZED) ||
                            e.getStatusCode().isSameCodeAs(HttpStatus.FORBIDDEN)) {
                            return false;
                        } else {
                            throw e;
                        }
                    }
                }).toList();

                if (accessibleEvents.isEmpty()) {
                    pretalxApiKeyRepository.delete(apiKey);
                    eventPublisher.publishEvent(new PretalxApiKeyInvalidEvent(apiKey.getApiKey()));
                    log.info("Removed invalid Pretalx API key: {}", apiKey.getApiKey());
                    continue;
                }

                var keyData = new PretalxApiKeyData(apiKey.getApiKey(),
                    accessibleEvents.stream().map(EventList::getSlug).toList());
                activeApiKeys.add(keyData);
                //Update cache
                accessibleEvents.forEach(event -> apiKeyCache.put(event.getSlug(), apiKey.getApiKey()));
            } catch (RestClientResponseException e) {
                //Remove invalid API key
                if (e.getStatusCode().isSameCodeAs(HttpStatus.UNAUTHORIZED)) {
                    pretalxApiKeyRepository.delete(apiKey);
                    eventPublisher.publishEvent(new PretalxApiKeyInvalidEvent(apiKey.getApiKey()));
                    log.info("Removed invalid Pretalx API key: {}", apiKey.getApiKey());
                } else {
                    throw e;
                }
            }
        }
    }

    public Set<String> accessibleEventSlugs() {
        lock.readLock().lock();
        try {
            return new HashSet<>(apiKeyCache.keySet());
        } finally {
            lock.readLock().unlock();
        }
    }

    public Set<PretalxApiKeyData> getActiveApiKeys() {
        lock.readLock().lock();
        if (activeApiKeys.isEmpty()) {
            lock.readLock().unlock();
            refreshApiKeys();
            lock.readLock().lock();
        }

        try {
            return new HashSet<>(activeApiKeys);
        } finally {
            lock.readLock().unlock();
        }
    }

    public Optional<String> findApiKeyForEventSlug(String eventSlug) {
        lock.readLock().lock();
        try {
            return Optional.ofNullable(apiKeyCache.get(eventSlug));
        } finally {
            lock.readLock().unlock();
        }
    }

    public String getApiKeyForEventSlug(String eventSlug) {
        return findApiKeyForEventSlug(eventSlug).orElseThrow(() ->
            PretalxMissingApiKeyException.forEventSlug(eventSlug));
    }
}
