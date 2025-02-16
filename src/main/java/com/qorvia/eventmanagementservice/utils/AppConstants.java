package com.qorvia.eventmanagementservice.utils;

public class AppConstants {

    // Specific Async Queue Names for each service
    public static final String ACCOUNT_SERVICE_ASYNC_QUEUE = "account-service-async-queue";
    public static final String NOTIFICATION_SERVICE_ASYNC_QUEUE = "notification-service-async-queue";
    public static final String BLOG_SERVICE_ASYNC_QUEUE = "blog-service-async-queue";
    public static final String COMMUNICATION_SERVICE_ASYNC_QUEUE = "communication-service-async-queue";
    public static final String EVENT_MANAGEMENT_SERVICE_ASYNC_QUEUE = "event-management-service-async-queue";
    public static final String PAYMENT_SERVICE_ASYNC_QUEUE = "payment-service-async-queue";

    // Specific RPC Queue Names for each service
    public static final String ACCOUNT_SERVICE_RPC_QUEUE = "account-service-rpc-queue";
    public static final String NOTIFICATION_SERVICE_RPC_QUEUE = "notification-service-rpc-queue";
    public static final String BLOG_SERVICE_RPC_QUEUE = "blog-service-rpc-queue";
    public static final String COMMUNICATION_SERVICE_RPC_QUEUE = "communication-service-rpc-queue";
    public static final String EVENT_MANAGEMENT_SERVICE_RPC_QUEUE = "event-management-service-rpc-queue";
    public static final String PAYMENT_SERVICE_RPC_QUEUE = "payment-service-rpc-queue";

    // Exchange Names
    public static final String ACCOUNT_SERVICE_EXCHANGE = "account-service-exchange";
    public static final String NOTIFICATION_SERVICE_EXCHANGE = "notification-service-exchange";
    public static final String BLOG_SERVICE_EXCHANGE = "blog-service-exchange";
    public static final String COMMUNICATION_SERVICE_EXCHANGE = "communication-service-exchange";
    public static final String EVENT_MANAGEMENT_SERVICE_EXCHANGE = "event-management-service-exchange";
    public static final String PAYMENT_SERVICE_EXCHANGE = "payment-service-exchange";

    // RPC Exchange Names
    public static final String ACCOUNT_SERVICE_RPC_EXCHANGE = "account-service-rpc-exchange";
    public static final String NOTIFICATION_SERVICE_RPC_EXCHANGE = "notification-service-rpc-exchange";
    public static final String BLOG_SERVICE_RPC_EXCHANGE = "blog-service-rpc-exchange";
    public static final String COMMUNICATION_SERVICE_RPC_EXCHANGE = "communication-service-rpc-exchange";
    public static final String EVENT_MANAGEMENT_SERVICE_RPC_EXCHANGE = "event-management-service-rpc-exchange";
    public static final String PAYMENT_SERVICE_RPC_EXCHANGE = "payment-service-rpc-exchange";

    // Routing Keys
    public static final String ACCOUNT_SERVICE_ROUTING_KEY = "account-service.routing";
    public static final String NOTIFICATION_SERVICE_ROUTING_KEY = "notification-service.routing";
    public static final String BLOG_SERVICE_ROUTING_KEY = "blog-service.routing";
    public static final String COMMUNICATION_SERVICE_ROUTING_KEY = "communication-service.routing";
    public static final String EVENT_MANAGEMENT_SERVICE_ROUTING_KEY = "event-management-service.routing";
    public static final String PAYMENT_SERVICE_ROUTING_KEY = "payment-service.routing";

    // RPC Routing Keys
    public static final String ACCOUNT_SERVICE_RPC_ROUTING_KEY = "account-service.rpc.routing";
    public static final String NOTIFICATION_SERVICE_RPC_ROUTING_KEY = "notification-service.rpc.routing";
    public static final String BLOG_SERVICE_RPC_ROUTING_KEY = "blog-service.rpc.routing";
    public static final String COMMUNICATION_SERVICE_RPC_ROUTING_KEY = "communication-service.rpc.routing";
    public static final String EVENT_MANAGEMENT_SERVICE_RPC_ROUTING_KEY = "event-management-service.rpc.routing";
    public static final String PAYMENT_SERVICE_RPC_ROUTING_KEY = "payment-service.rpc.routing";

    // Method to get exchange for a given service
    public static String getExchange(String serviceName) {
        switch (serviceName) {
            case "account-service":
                return ACCOUNT_SERVICE_EXCHANGE;
            case "notification-service":
                return NOTIFICATION_SERVICE_EXCHANGE;
            case "blog-service":
                return BLOG_SERVICE_EXCHANGE;
            case "communication-service":
                return COMMUNICATION_SERVICE_EXCHANGE;
            case "event-management-service":
                return EVENT_MANAGEMENT_SERVICE_EXCHANGE;
            case "payment-service":
                return PAYMENT_SERVICE_EXCHANGE;
            case "account-service-rpc":
                return ACCOUNT_SERVICE_RPC_EXCHANGE;
            case "notification-service-rpc":
                return NOTIFICATION_SERVICE_RPC_EXCHANGE;
            case "blog-service-rpc":
                return BLOG_SERVICE_RPC_EXCHANGE;
            case "communication-service-rpc":
                return COMMUNICATION_SERVICE_RPC_EXCHANGE;
            case "event-management-service-rpc":
                return EVENT_MANAGEMENT_SERVICE_RPC_EXCHANGE;
            case "payment-service-rpc":
                return PAYMENT_SERVICE_RPC_EXCHANGE;
            default:
                return "Unknown service";
        }
    }

    // Method to get queue for a given service type (async or rpc)
    public static String getQueue(String serviceName, String serviceType) {
        if ("rpc".equalsIgnoreCase(serviceType)) {
            switch (serviceName) {
                case "account-service":
                    return ACCOUNT_SERVICE_RPC_QUEUE;
                case "notification-service":
                    return NOTIFICATION_SERVICE_RPC_QUEUE;
                case "blog-service":
                    return BLOG_SERVICE_RPC_QUEUE;
                case "communication-service":
                    return COMMUNICATION_SERVICE_RPC_QUEUE;
                case "event-management-service":
                    return EVENT_MANAGEMENT_SERVICE_RPC_QUEUE;
                case "payment-service":
                    return PAYMENT_SERVICE_RPC_QUEUE;
                default:
                    return "Unknown service";
            }
        } else {
            switch (serviceName) {
                case "account-service":
                    return ACCOUNT_SERVICE_ASYNC_QUEUE;
                case "notification-service":
                    return NOTIFICATION_SERVICE_ASYNC_QUEUE;
                case "blog-service":
                    return BLOG_SERVICE_ASYNC_QUEUE;
                case "communication-service":
                    return COMMUNICATION_SERVICE_ASYNC_QUEUE;
                case "event-management-service":
                    return EVENT_MANAGEMENT_SERVICE_ASYNC_QUEUE;
                case "payment-service":
                    return PAYMENT_SERVICE_ASYNC_QUEUE;
                default:
                    return "Unknown service";
            }
        }
    }

    // Method to get routing key for a given service type (async or rpc)
    public static String getRoutingKey(String serviceName, String serviceType) {
        if ("rpc".equalsIgnoreCase(serviceType)) {
            switch (serviceName) {
                case "account-service":
                    return ACCOUNT_SERVICE_RPC_ROUTING_KEY;
                case "notification-service":
                    return NOTIFICATION_SERVICE_RPC_ROUTING_KEY;
                case "blog-service":
                    return BLOG_SERVICE_RPC_ROUTING_KEY;
                case "communication-service":
                    return COMMUNICATION_SERVICE_RPC_ROUTING_KEY;
                case "event-management-service":
                    return EVENT_MANAGEMENT_SERVICE_RPC_ROUTING_KEY;
                case "payment-service":
                    return PAYMENT_SERVICE_RPC_ROUTING_KEY;
                default:
                    return "Unknown service";
            }
        } else {
            switch (serviceName) {
                case "account-service":
                    return ACCOUNT_SERVICE_ROUTING_KEY;
                case "notification-service":
                    return NOTIFICATION_SERVICE_ROUTING_KEY;
                case "blog-service":
                    return BLOG_SERVICE_ROUTING_KEY;
                case "communication-service":
                    return COMMUNICATION_SERVICE_ROUTING_KEY;
                case "event-management-service":
                    return EVENT_MANAGEMENT_SERVICE_ROUTING_KEY;
                case "payment-service":
                    return PAYMENT_SERVICE_ROUTING_KEY;
                default:
                    return "Unknown service";
            }
        }
    }
}
