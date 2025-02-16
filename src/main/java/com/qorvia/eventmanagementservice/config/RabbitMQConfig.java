package com.qorvia.eventmanagementservice.config;

import com.qorvia.eventmanagementservice.utils.AppConstants;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

@Configuration
@EnableRabbit
public class RabbitMQConfig {

    // --- Event Management Service RPC Queue, Exchange, and Routing ---

    @Bean
    public Queue eventManagementServiceRpcQueue() {
        return new Queue(AppConstants.EVENT_MANAGEMENT_SERVICE_RPC_QUEUE, true);
    }

    @Bean
    public Exchange eventManagementServiceRpcExchange() {
        return new DirectExchange(AppConstants.EVENT_MANAGEMENT_SERVICE_RPC_EXCHANGE, true, false);
    }

    @Bean
    public Binding eventManagementServiceRpcBinding() {
        return BindingBuilder
                .bind(eventManagementServiceRpcQueue())
                .to(eventManagementServiceRpcExchange())
                .with(AppConstants.EVENT_MANAGEMENT_SERVICE_RPC_ROUTING_KEY)
                .noargs();
    }


    // --- Communication Service Async Queue, Exchange, and Routing ---

    @Bean
    public Queue communicationServiceAsyncQueue() {
        return new Queue(AppConstants.COMMUNICATION_SERVICE_ASYNC_QUEUE, true);
    }

    @Bean
    public Exchange communicationServiceAsyncExchange() {
        return new DirectExchange(AppConstants.COMMUNICATION_SERVICE_EXCHANGE, true, false);
    }

    @Bean
    public Binding communicationServiceAsyncBinding() {
        return BindingBuilder
                .bind(communicationServiceAsyncQueue())
                .to(communicationServiceAsyncExchange())
                .with(AppConstants.COMMUNICATION_SERVICE_ROUTING_KEY)
                .noargs();
    }


    // --- Payment Service RPC Queue, Exchange, and Routing ---

    @Bean
    public Queue paymentServiceRpcQueue() {
        return new Queue(AppConstants.PAYMENT_SERVICE_RPC_QUEUE, true);
    }

    @Bean
    public Exchange paymentServiceRpcExchange() {
        return new DirectExchange(AppConstants.PAYMENT_SERVICE_RPC_EXCHANGE, true, false);
    }

    @Bean
    public Binding paymentServiceRpcBinding() {
        return BindingBuilder
                .bind(eventManagementServiceRpcQueue())
                .to(eventManagementServiceRpcExchange())
                .with(AppConstants.PAYMENT_SERVICE_RPC_ROUTING_KEY)
                .noargs();
    }


    // Configure the RPC Listener Container for the RPC queues
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setConcurrentConsumers(3);
        return factory;
    }
}