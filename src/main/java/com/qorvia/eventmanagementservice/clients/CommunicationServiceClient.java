package com.qorvia.eventmanagementservice.clients;

import com.qorvia.eventmanagementservice.dto.RoomAccessDTO;
import com.qorvia.eventmanagementservice.dto.ScheduleEventDTO;
import com.qorvia.eventmanagementservice.dto.message.RoomAccessMessage;
import com.qorvia.eventmanagementservice.dto.message.ScheduleEventMessage;
import com.qorvia.eventmanagementservice.messaging.RabbitMQSender;
import com.qorvia.eventmanagementservice.utils.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommunicationServiceClient {

    private final RabbitMQSender rabbitMQSender;

    public void allowRoom(RoomAccessDTO roomAccessDTO){
        RoomAccessMessage message = new RoomAccessMessage();
        message.setEventId(roomAccessDTO.getEventId());
        message.setUserEmail(roomAccessDTO.getUserEmail());
        message.setUserId(roomAccessDTO.getUserId());

        rabbitMQSender.sendAsyncMessage(
                AppConstants.COMMUNICATION_SERVICE_ASYNC_QUEUE,
                AppConstants.COMMUNICATION_SERVICE_EXCHANGE,
                AppConstants.COMMUNICATION_SERVICE_ROUTING_KEY,
                message
        );
    }

    public void scheduleEvent(ScheduleEventDTO eventDTO){

        ScheduleEventMessage message = new ScheduleEventMessage();
        message.setEventId(eventDTO.getEventId());
        message.setName(eventDTO.getName());
        message.setImageUrl(eventDTO.getImageUrl());
        message.setOrganizerId(eventDTO.getOrganizerId());
        message.setStartDateAndTime(eventDTO.getStartDateAndTime());
        message.setEndDateAndTime(eventDTO.getEndDateAndTime());

        rabbitMQSender.sendAsyncMessage(
                AppConstants.COMMUNICATION_SERVICE_ASYNC_QUEUE,
                AppConstants.COMMUNICATION_SERVICE_EXCHANGE,
                AppConstants.COMMUNICATION_SERVICE_ROUTING_KEY,
                message
        );
    }

}
