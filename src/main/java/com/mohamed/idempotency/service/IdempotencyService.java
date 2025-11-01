package com.mohamed.idempotency.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mohamed.idempotency.entity.IdempotencyRecord;
import com.mohamed.idempotency.repository.IdempotencyRecordRepository;
import com.mohamed.idempotency.utils.InMemoryCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotencyService {
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final ObjectMapper objectMapper;
    private final InMemoryCache<String, IdempotencyRecord> cache;

    @Transactional(readOnly = true)
    public Optional<IdempotencyRecord> findExistingRecord(String idempotencyKey, String requestHash) {
        log.debug("Checking for existing idempotency record with key: {}", idempotencyKey);

        IdempotencyRecord cachedRecord = cache.get(idempotencyKey);
        if (cachedRecord != null && !cachedRecord.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.debug("Cache hit for idempotency key: {}", idempotencyKey);
            return Optional.of(cachedRecord);
        }

        if (cachedRecord != null && cachedRecord.getExpiresAt().isBefore(LocalDateTime.now())) {
            cache.remove(idempotencyKey);
            log.debug("Removed invalid cached record for key: {}", idempotencyKey);
        }

        Optional<IdempotencyRecord> existingRecord = idempotencyRecordRepository.findByIdempotencyKey(idempotencyKey);

        if (existingRecord.isEmpty()) {
            log.debug("No existing idempotency record found with key: {}", idempotencyKey);
            return Optional.empty();
        }

        IdempotencyRecord record = existingRecord.get();

        if (!record.getRequestHash().equals(requestHash)) {
            log.warn("Idempotency key reused with different request body: {}", idempotencyKey);
            throw new IllegalArgumentException("Idempotency key used with different request");
        }

        if (record.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.info("Idempotency record expired for key: {}", idempotencyKey);
            cache.remove(idempotencyKey);
            return Optional.empty();
        }

        return Optional.of(record);
    }

    @Transactional
    public void saveRecord(String idempotencyKey, String requestHash, Object response, int httpStatus) {
        try {
            String responseBody = objectMapper.writeValueAsString(response);

            var record = IdempotencyRecord.builder()
                    .idempotencyKey(idempotencyKey)
                    .requestHash(requestHash)
                    .responseBody(responseBody)
                    .httpStatus(httpStatus)
                    .build();

            IdempotencyRecord savedRecord = idempotencyRecordRepository.save(record);

            cache.put(idempotencyKey, savedRecord);
            log.debug("Cached newly saved idempotency record with key: {}", idempotencyKey);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize response object", e);
            throw new RuntimeException("Failed to serialize save record", e);
        }
    }

    public String generateHash(String idempotencyKey, Object request) {
        try {
            String requestJson = objectMapper.writeValueAsString(request);
            String combined = idempotencyKey + ":" + requestJson;

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(combined.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);
        } catch (JsonProcessingException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate request hash", e);
        }
    }

    public <T> T deserializeResponse(String responseBody, Class<T> responseType) {
        try {
            return objectMapper.readValue(responseBody, responseType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize response", e);
        }
    }
}
