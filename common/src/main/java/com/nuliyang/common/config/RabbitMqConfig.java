package com.nuliyang.common.config;


import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMqConfig {



    // 定义一个交换机（DirectExchange）
    @Bean
    public DirectExchange myExchange() {
        return ExchangeBuilder.directExchange("weiYangAiEx").durable(true).build();
    }

    // 定义一个队列
    @Bean
    public Queue myQueue() {
        return QueueBuilder.nonDurable("weiYangAiQueue").build();
    }

    // 绑定队列到交换机，指定 routing key
    @Bean
    public Binding binding() {
        return BindingBuilder.bind(myQueue())
                .to(myExchange())
                .with("weiYangAiRoutingKey");
    }


    @Bean
    public Jackson2JsonMessageConverter  jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter jackson2JsonMessageConverter){
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter);
        return rabbitTemplate;
    }



}
