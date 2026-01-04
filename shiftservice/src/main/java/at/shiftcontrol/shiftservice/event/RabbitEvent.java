package at.shiftcontrol.shiftservice.event;

public record RabbitEvent(
    Object payload,      // generated AsyncAPI model (any event)
    String routingKey    // e.g. "assignment.switch.completed"
) {}
