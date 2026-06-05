package com.proyecto.version1.Features.Notifications.service.impl;

import com.proyecto.version1.Features.Notifications.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final SnsClient snsClient;

    @Value("${aws.sns.topic-arn}")
    private String topicArn;

    @Override
    public void sendBusinessEvent(String subject, String message, String eventType, UUID tenantId) {
        try {
            Map<String, MessageAttributeValue> attributes = new HashMap<>();
            attributes.put("eventType", MessageAttributeValue.builder().dataType("String").stringValue(eventType).build());
            attributes.put("tenantId", MessageAttributeValue.builder().dataType("String").stringValue(tenantId.toString()).build());

            PublishRequest request = PublishRequest.builder()
                    .topicArn(topicArn)
                    .subject(subject)
                    .message(message)
                    .messageAttributes(attributes)
                    .build();

            snsClient.publish(request);
            log.info("Evento SNS enviado: {} para tenant: {}", eventType, tenantId);
        } catch (Exception e) {
            log.error("Error al enviar notificación SNS", e);
            // No bloqueamos el flujo principal por un error de notificación
        }
    }
}
