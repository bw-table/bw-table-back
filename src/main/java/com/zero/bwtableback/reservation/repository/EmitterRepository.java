package com.zero.bwtableback.reservation.repository;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class EmitterRepository {

    // key: memberId_emitterId (복합키) - value: SseEmitter
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    // 새로운 연결 저장
    public void saveEmitter(String emitterId, SseEmitter emitter, Long memberId) {
        String compositeKey = generateCompositeKey(memberId, emitterId);
        emitters.put(compositeKey, emitter);
    }

    // 특정 사용자의 활성화된 emitterId 리스트 조회
    public List<String> findAllEmitterIdsByMemberId(Long memberId) {
        List<String> emitterIds = new ArrayList<>();
        emitters.keySet().forEach(key -> {
            if (key.startsWith(memberId + "_")) {
                emitterIds.add(key);
            }
        });
        return emitterIds;
    }

    // emitter id로 emitter 객체 조회
    public SseEmitter findById(String emitterId) {
        return emitters.get(emitterId);
    }

    // 특정 emitter 제거
    public void removeEmitter(String emitterId) {
        emitters.remove(emitterId);
    }

    // memberId와 emitterId를 조합하여 복합 키 생성
    private String generateCompositeKey(Long memberId, String emitterId) {
        return memberId + "_" + emitterId;
    }

}
