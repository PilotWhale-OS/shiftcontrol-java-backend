package at.shiftcontrol.trustservice.config;

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

    @Value("${trust.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${trust.rabbitmq.queue}")
    private String queueName;

    @Value("${trust.rabbitmq.routing-key}")
    private String routingKey;

    @Bean
    TopicExchange shiftcontrolExchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    Queue trustQueue() {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    Binding trustBinding() {
        return BindingBuilder
            .bind(trustQueue())
            .to(shiftcontrolExchange())
            .with(routingKey);
    }
}
