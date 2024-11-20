package com.zero.bwtableback.reservation.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
public class EmitterRepository {

    // 활성화된 emitter를 관리하는 map
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    // 고유한 emitterId를 키로 사용하여 저장
    public void saveEmitter(String emitterId, SseEmitter emitter) {
        emitters.put(emitterId, emitter);
    }

    // 특정 사용자의 emitterId 리스트 조회
    public List<String> findAllEmitterIdsByMemberId(Long memberId) {
        List<String> emitterIds = new ArrayList<>();
        emitters.keySet().forEach(key -> {
            if (key.startsWith(memberId + "_")) {
                emitterIds.add(key);
            }
        });
        return emitterIds;
    }

    public SseEmitter findById(String emitterId) {
        return emitters.get(emitterId);
    }

    public void removeEmitter(String emitterId) {
        emitters.remove(emitterId);
    }

}
