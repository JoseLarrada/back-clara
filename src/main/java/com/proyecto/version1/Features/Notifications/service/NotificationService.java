package com.proyecto.version1.Features.Notifications.service;

import java.util.UUID;

public interface NotificationService {
    void sendBusinessEvent(String subject, String message, String eventType, UUID tenantId);
}
