package com.education.stelar.kernel.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void publish(DomainEvent event) {
        log.debug("Publicando evento de dominio: {} [{}]", event.eventType(), event.eventId());
        applicationEventPublisher.publishEvent(event);
    }
}
