package com.mohamed.idempotency.utils.utils;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryCache<K, V> {
    private final Map<K, V> cache = new ConcurrentHashMap<>();

    public void put(K key, V value) {
        cache.put(key, value);
    }

    public V get(K key) {
        return cache.get(key);
    }

    public void remove(K key) {
        cache.remove(key);
    }

    public void clear() {
        cache.clear();
    }

    public boolean containsKey(K key) {
        return cache.containsKey(key);
    }

    public int size() {
        return cache.size();
    }

    public boolean isEmpty() {
        return cache.isEmpty();
    }

    public Set<K> keySet() {
        return cache.keySet();
    }

    public Map<K, V> getAll() {
        return new ConcurrentHashMap<>(cache);
    }
}