package com.qorvia.eventmanagementservice.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQSender {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public void sendAsyncMessage(String queue, String exchange, String routingKey, Object message) {
        try {
            byte[] messageBytes = objectMapper.writeValueAsBytes(message);
            rabbitTemplate.convertAndSend(exchange, routingKey, messageBytes, msg -> {
                msg.getMessageProperties().setHeader("queue", queue);
                msg.getMessageProperties().setContentType("application/octet-stream");
                return msg;
            });
            log.info("Sent async message to queue: {}, exchange: {}, routingKey: {}", queue, exchange, routingKey);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize message", e);
            throw new RuntimeException("Failed to serialize message", e);
        }
    }

    public <T> T sendRpcMessage(String queue, String exchange, String routingKey, Object message, Class<T> responseType) throws TimeoutException, IOException {
        try {
            byte[] messageBytes = objectMapper.writeValueAsBytes(message);
            String replyQueue = rabbitTemplate.getConnectionFactory()
                    .createConnection()
                    .createChannel(false)
                    .queueDeclare()
                    .getQueue();
            String correlationId = UUID.randomUUID().toString();

            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setReplyTo(replyQueue);
            messageProperties.setCorrelationId(correlationId);
            messageProperties.setHeader("queue", queue);
            messageProperties.setContentType("application/octet-stream");

            Message msg = new Message(messageBytes, messageProperties);

            Message responseMessage = rabbitTemplate.sendAndReceive(exchange, routingKey, msg);

            if (responseMessage != null) {
                String responseCorrelationId = responseMessage.getMessageProperties().getCorrelationId();
                if (!correlationId.equals(responseCorrelationId)) {
                    throw new IllegalArgumentException("Correlation ID mismatch: expected " + correlationId + " but got " + responseCorrelationId);
                }

                log.info("Received RPC response with correlation ID: {}", correlationId);
                return objectMapper.readValue(responseMessage.getBody(), responseType);
            }

            throw new TimeoutException("No response received from RPC call.");
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize message", e);
            throw new RuntimeException("Failed to serialize message", e);
        } catch (IOException e) {
            log.error("Failed to deserialize response", e);
            throw new RuntimeException("Failed to deserialize response", e);
        }
    }
}
