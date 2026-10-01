package com.bookworm.notification.exception;

/** Thrown when the underlying mail transport rejects/fails a send - maps to HTTP 502. */
public class NotificationDeliveryException extends RuntimeException {
    public NotificationDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
