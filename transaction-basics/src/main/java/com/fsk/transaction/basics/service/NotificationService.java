package com.fsk.transaction.basics.service;

import com.fsk.transaction.basics.entity.Notification;
import com.fsk.transaction.basics.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

/**
 * @TransactionalEventListener
 * 
 * AFTER_COMMIT: Gerçekten DB commit olduktan sonra çalışır
 * Mesaj gönderme, cache invalidation için ideal
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    /**
     * Event yayınlama - Transaction commit olduktan sonra dinlenecek
     */
    @Transactional
    public void sendNotification(String message, String recipient) {
        log.info("sendNotification - Transaction içinde");
        
        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setRecipient(recipient);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setSent(false);
        
        Notification saved = notificationRepository.save(notification);
        log.info("Notification kaydedildi: {}", saved.getId());
        
        // Event yayınla - Transaction commit olduktan sonra dinlenecek
        eventPublisher.publishEvent(new NotificationCreatedEvent(saved.getId(), message, recipient));
    }
    
    /**
     * @TransactionalEventListener - AFTER_COMMIT
     * Gerçekten DB commit olduktan sonra çalışır
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNotificationCreated(NotificationCreatedEvent event) {
        log.info("handleNotificationCreated - AFTER_COMMIT - Transaction commit olduktan sonra");
        log.info("Notification ID: {}, Message: {}, Recipient: {}", event.notificationId(), event.message(), event.recipient());
        
        // Burada gerçek notification gönderme işlemi yapılabilir
        // Email, SMS, push notification vs.
        
        // Notification'ı sent olarak işaretle
        notificationRepository.findById(event.notificationId())
            .ifPresent(notification -> {
                notification.setSent(true);
                notificationRepository.save(notification);
            });
    }
    
    /**
     * AFTER_ROLLBACK - Transaction rollback olduktan sonra
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void handleNotificationRollback(NotificationCreatedEvent event) {
        log.info("handleNotificationRollback - AFTER_ROLLBACK - Transaction rollback oldu");
    }
    
    // Event class
    public record NotificationCreatedEvent(Long notificationId, String message, String recipient) { }
}



