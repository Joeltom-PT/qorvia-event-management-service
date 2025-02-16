package com.qorvia.eventmanagementservice.clients;

import com.qorvia.eventmanagementservice.dto.*;
import com.qorvia.eventmanagementservice.dto.message.BookingCompletedNotificationMessage;
import com.qorvia.eventmanagementservice.dto.message.PaymentRequestMessage;
import com.qorvia.eventmanagementservice.dto.message.RefundRequestMessage;
import com.qorvia.eventmanagementservice.dto.message.response.PaymentInfoMessageResponse;
import com.qorvia.eventmanagementservice.dto.message.response.RefundMessageResponse;
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
public class PaymentServiceClient {

    private final RabbitMQSender rabbitMQSender;

    public PaymentInfoDTO getPaymentInfo(PaymentRequestDTO paymentRequestDTO) {

        PaymentRequestMessage message = new PaymentRequestMessage();
        message.setCurrency(paymentRequestDTO.getCurrency());
        message.setAmount(paymentRequestDTO.getAmount());
        message.setEmail(paymentRequestDTO.getEmail());

        PaymentRequestDTO.EventData eventData = paymentRequestDTO.getEventData();
        if (eventData != null) {
            message.setEventId(eventData.getEventId());
            message.setEventOrganizerId(eventData.getEventOrganizerId());
            message.setEventName(eventData.getEventName());
            message.setImgUrl(eventData.getImgUrl());
        }

        try {
            PaymentInfoMessageResponse messageResponse = rabbitMQSender.sendRpcMessage(
                    AppConstants.PAYMENT_SERVICE_RPC_QUEUE,
                    AppConstants.PAYMENT_SERVICE_RPC_EXCHANGE,
                    AppConstants.PAYMENT_SERVICE_RPC_ROUTING_KEY,
                    message,
                    PaymentInfoMessageResponse.class);

            PaymentInfoDTO paymentInfoDTO = PaymentInfoDTO.builder()
                    .paymentUrl(messageResponse.getPaymentUrl())
                    .sessionId(messageResponse.getSessionId())
                    .tempSessionId(messageResponse.getTempSessionId())
                    .build();

            return paymentInfoDTO;

        } catch (IOException | TimeoutException e) {
            return null;
        }
    }


    public RefundDTO paymentRefundRequest(RefundRequestDTO refundRequestDTO) {

        RefundRequestMessage message = new RefundRequestMessage();
        message.setSessionId(refundRequestDTO.getSessionId());
        message.setRefundPercentage(refundRequestDTO.getRefundPercentage());

        try {
            RefundMessageResponse messageResponse = rabbitMQSender.sendRpcMessage(
                    AppConstants.PAYMENT_SERVICE_RPC_QUEUE,
                    AppConstants.PAYMENT_SERVICE_RPC_EXCHANGE,
                    AppConstants.PAYMENT_SERVICE_RPC_ROUTING_KEY,
                    message,
                    RefundMessageResponse.class);

            RefundDTO refundDTO = RefundDTO.builder()
                    .sessionId(messageResponse.getSessionId())
                    .refundAmount(messageResponse.getRefundAmount())
                    .refundStatus(messageResponse.getRefundStatus())
                    .errorMessage(messageResponse.getErrorMessage())
                    .build();

            return refundDTO;

        } catch (IOException | TimeoutException e) {
            return null;
        }

    }





}
