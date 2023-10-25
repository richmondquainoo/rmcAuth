package com.example.authvento.rabbitmq;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@RequiredArgsConstructor
public class RabbitMQConfig {

    private final ConnectionFactory connectionFactory;

    private static final String topicExchange = "test-exchange";
    private static final String authEmailQueue = "test-queue";

    private static final String authEmailRoutingKey="test-route";

    //template to send messages
    @Bean
    public AmqpTemplate amqpTemplate(){

        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jacksonConverter());
        return rabbitTemplate;
    }

    //Allows us to receive messages - ie message listener
    @Bean
    public SimpleRabbitListenerContainerFactory simpleRabbitListenerContainerFactory(){
        SimpleRabbitListenerContainerFactory factory =
                new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jacksonConverter());
        return factory;
    }

    //message converter
    @Bean
    public MessageConverter jacksonConverter(){
        MessageConverter jackson2JsonMessageConverter =
                new Jackson2JsonMessageConverter();
        return jackson2JsonMessageConverter;
    }

    @Bean
    public TopicExchange topicExchange(){
        return new TopicExchange(topicExchange);
    }

    @Bean
    public Queue authEmailQueue(){
        return new Queue(this.authEmailQueue);
    }

       @Bean
    public Binding emailBinding(){
        return BindingBuilder
                .bind(authEmailQueue())
                .to(topicExchange())
                .with(this.authEmailRoutingKey);
    }



   }
