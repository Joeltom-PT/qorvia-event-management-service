package com.qorvia.eventmanagementservice.clients;

import com.qorvia.eventmanagementservice.dto.BookingCompletedNotificationDTO;
import com.qorvia.eventmanagementservice.dto.message.BookingCompletedNotificationMessage;
import com.qorvia.eventmanagementservice.messaging.RabbitMQSender;
import com.qorvia.eventmanagementservice.utils.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceClient {

    private final RabbitMQSender rabbitMQSender;

    public void sendBookingCompletedNotification(BookingCompletedNotificationDTO notificationDTO) {
        BookingCompletedNotificationMessage message = new BookingCompletedNotificationMessage();

        message.setUserName(notificationDTO.getUserName());
        message.setEmail(notificationDTO.getEmail());
        message.setTotalAmount(notificationDTO.getTotalAmount());
        message.setTotalDiscount(notificationDTO.getTotalDiscount());
        message.setPaymentStatus(notificationDTO.getPaymentStatus());
        message.setEventName(notificationDTO.getEventName());
        message.setImageUrl(notificationDTO.getImageUrl());

        rabbitMQSender.sendAsyncMessage(
                AppConstants.NOTIFICATION_SERVICE_ASYNC_QUEUE,
                AppConstants.NOTIFICATION_SERVICE_EXCHANGE,
                AppConstants.NOTIFICATION_SERVICE_ROUTING_KEY,
                message
        );

    }


}
