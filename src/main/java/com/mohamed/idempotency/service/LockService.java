package com.mohamed.idempotency.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Slf4j
public class LockService {
    private final ConcurrentHashMap<String, Lock> locks = new ConcurrentHashMap<>();

    public Lock acquireLock(String lockKey) {
        Lock lock = locks.computeIfAbsent(lockKey, k -> new ReentrantLock());

        lock.lock();
        return lock;
    }

    public void releaseLock(String lockKey, Lock lock) {
        try {
            lock.unlock();
            log.info("released lock for key: {}", lockKey);
        } catch (Exception e) {
            locks.remove(lockKey);
        }
    }
}
