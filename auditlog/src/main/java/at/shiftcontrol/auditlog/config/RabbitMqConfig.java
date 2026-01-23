package at.shiftcontrol.auditlog.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    @Value("${audit.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${audit.rabbitmq.queue}")
    private String queueName;

    @Value("${audit.rabbitmq.routingKey}")
    private String routingKey;

    @Bean
    TopicExchange shiftcontrolExchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    Queue auditlogQueue() {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    Binding trustBinding() {
        return BindingBuilder
            .bind(auditlogQueue())
            .to(shiftcontrolExchange())
            .with(routingKey);
    }
}
