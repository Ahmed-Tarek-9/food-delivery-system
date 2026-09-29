package com.masrdelivery.exception;

import com.masrdelivery.domain.order.OrderStatus;

public class IllegalOrderTransitionException extends PlatformException {
    public IllegalOrderTransitionException(OrderStatus from, OrderStatus to) {
        super("Cannot transition order status from " + from + " to " + to);
    }
}
