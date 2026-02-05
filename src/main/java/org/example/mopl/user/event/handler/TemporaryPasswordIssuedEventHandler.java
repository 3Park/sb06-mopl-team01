package org.example.mopl.user.event.handler;

import lombok.RequiredArgsConstructor;
import org.example.mopl.auth.event.TemporaryPasswordIssuedEvent;
import org.example.mopl.user.service.TemporaryPasswordService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
@Component
public class TemporaryPasswordIssuedEventHandler {

    private final TemporaryPasswordService service;

    @Async
    @EventListener(TemporaryPasswordIssuedEvent.class)
    public void TemporaryPasswordIssuedEvent(TemporaryPasswordIssuedEvent event) {
        if(event == null
            || StringUtils.hasText(event.getTemporaryPassword()) == false
            ||  StringUtils.hasText(event.getReceiverEmail()) == false)
            return;

        service.save(event.getReceiverEmail(), event.getTemporaryPassword());
    }
}
