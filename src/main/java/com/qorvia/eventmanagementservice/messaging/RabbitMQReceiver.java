package com.qorvia.eventmanagementservice.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qorvia.eventmanagementservice.dto.PaymentStatusChangeDTO;
import com.qorvia.eventmanagementservice.dto.message.PaymentRequestMessage;
import com.qorvia.eventmanagementservice.dto.message.PaymentStatusChangeMessage;
import com.qorvia.eventmanagementservice.dto.message.ValidateEventMessage;
import com.qorvia.eventmanagementservice.dto.message.response.ValidateEventMessageResponse;
import com.qorvia.eventmanagementservice.service.BookingService;
import com.qorvia.eventmanagementservice.service.EventService;
import com.qorvia.eventmanagementservice.utils.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQReceiver {

    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;
    private final BookingService bookingService;

    @RabbitListener(queues = {AppConstants.EVENT_MANAGEMENT_SERVICE_RPC_QUEUE})
    public void receiveMessage(Message amqpMessage) {
        try {
            byte[] messageBytes = amqpMessage.getBody();
            log.info("I am getting the message bytes as : ========================================== : {}", new String(messageBytes, StandardCharsets.UTF_8));
            MessageProperties amqpProps = amqpMessage.getMessageProperties();
            String correlationId = amqpProps.getCorrelationId();
            if (correlationId != null) {
                log.info("Received RPC message with correlation ID: {}", correlationId);
            }

            Map<String, Object> messageMap = objectMapper.readValue(messageBytes, Map.class);
            String type = (String) messageMap.get("type");

            switch (type) {
                case "validate-event":
                    ValidateEventMessage validateEventMessage = objectMapper.convertValue(messageMap, ValidateEventMessage.class);
                    ValidateEventMessageResponse validateEventMessageResponse = handleVerifyOtpMessage(validateEventMessage);
                    sendRpcResponse(amqpProps, validateEventMessageResponse);
                    break;
                case "payment-status-change":
                    PaymentStatusChangeMessage paymentStatusChangeMessage = objectMapper.convertValue(messageMap, PaymentStatusChangeMessage.class);
                    handlePaymentStatusChangeMessage(paymentStatusChangeMessage);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown message type: " + type);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize message", e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to process message", e);
        }
    }

    private ValidateEventMessageResponse handleVerifyOtpMessage(ValidateEventMessage message) {
        log.info("Validating event by id : {}", message.getEventId());
        ValidateEventMessageResponse response = new ValidateEventMessageResponse();
        response.setIsValid(true);
        return response;
    }

    private void handlePaymentStatusChangeMessage(PaymentStatusChangeMessage message) {
        PaymentStatusChangeDTO paymentStatusChangeDTO = new PaymentStatusChangeDTO();

        paymentStatusChangeDTO.setPaymentSessionId(message.getPaymentSessionId());
        paymentStatusChangeDTO.setUserEmail(message.getUserEmail());
        paymentStatusChangeDTO.setEventId(message.getEventId());
        paymentStatusChangeDTO.setPaymentStatus(message.getPaymentStatus());

        bookingService.paymentStatusChangeHandle(paymentStatusChangeDTO);
    }




    private void sendRpcResponse(MessageProperties amqpProps, Object response) throws JsonProcessingException {
        byte[] responseBytes = objectMapper.writeValueAsBytes(response);
        MessageProperties responseProperties = new MessageProperties();
        responseProperties.setCorrelationId(amqpProps.getCorrelationId());
        responseProperties.setContentType("application/octet-stream");

        Message responseMessage = new Message(responseBytes, responseProperties);
        rabbitTemplate.send(amqpProps.getReplyTo(), responseMessage);
    }
}
