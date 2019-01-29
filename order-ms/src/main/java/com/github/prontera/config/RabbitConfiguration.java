package com.github.prontera.config;

import com.github.prontera.EventDrivenPublisher;
import com.google.common.collect.ImmutableMap;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Zhao Junjian
 */
@Configuration
public class RabbitConfiguration {
    /**默认的交换器名称*/
    public static final String DEFAULT_DIRECT_EXCHANGE = "prontera.direct";
    /**队列名称*/
    public static final String POINT_QUEUE = "point";
    /**死信队列名称*/
    public static final String DEAD_POINT_QUEUE = "d.point";
    /**路由键*/
    public static final String POINT_KEY = "0666fb88-4cc2-11e7-9226-0242ac130004";
    /**死信队列的路由键*/
    public static final String DEAD_POINT_KEY = "a0b1d08b-4ccd-11e7-9226-0242ac130004";

    static {
        EventDrivenPublisher.registerType(EventBusinessType.ADD_PTS.name(), DEFAULT_DIRECT_EXCHANGE, POINT_KEY);
    }

    @Bean
    public EventDrivenPublisher eventDrivenPublisher() {
        return new EventDrivenPublisher();
    }

    /**
     * 声明一个direct类型的交换器
     * @return
     */
    @Bean
    public DirectExchange defaultExchange() {
        return new DirectExchange(DEFAULT_DIRECT_EXCHANGE, true, false);
    }

    /**
     * 声明一个队列，绑定队列上的死信交换器为源交换器，并设置死信的路由键
     * @return
     */
    @Bean
    public Queue pointQueue() {
        /**将当前交换器作为死信交换器，修改*/
        final ImmutableMap<String, Object> args =
                ImmutableMap.of("x-dead-letter-exchange", DEFAULT_DIRECT_EXCHANGE,
                        "x-dead-letter-routing-key", DEAD_POINT_KEY);
        return new Queue(POINT_QUEUE, true, false, false, args);
    }

    /**
     * 绑定原队列
     * @return
     */
    @Bean
    public Binding pointBinding() {
        return BindingBuilder.bind(pointQueue()).to(defaultExchange()).with(POINT_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 声明一个死信队列
     * @return
     */
    @Bean
    public Queue deafPointQueue() {
        return new Queue(DEAD_POINT_QUEUE, true, false, false);
    }

    /**
     * 绑定死信交换器和死信队列
     * @return
     */
    @Bean
    public Binding deadPointBinding() {
        return BindingBuilder.bind(deafPointQueue()).to(defaultExchange()).with(DEAD_POINT_KEY);
    }

}
