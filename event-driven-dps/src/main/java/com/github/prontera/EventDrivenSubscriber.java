package com.github.prontera;

import com.github.prontera.domain.EventSubscriber;
import com.github.prontera.domain.type.EventStatus;
import com.github.prontera.persistence.EventSubscriberMapper;
import com.github.prontera.util.HibernateValidators;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;

/**
 * rabbitmq消息消费者
 * @author Zhao Junjian
 */
public class EventDrivenSubscriber {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventDrivenSubscriber.class);

    @Autowired
    private EventSubscriberMapper subscriberMapper;
    @Autowired
    private EventHandler handler;

    @PostConstruct
    public void afterProperties() {
        Preconditions.checkState(handler != null, "root EventHandler MUST not be null");
    }

    @Transactional(rollbackFor = Exception.class)
    public int persistAndHandleMessage(String businessType, String payload, String guid) {
        Preconditions.checkNotNull(businessType);
        Preconditions.checkNotNull(payload);
        Preconditions.checkNotNull(guid);
        /**这里感觉可以优化，因为当前这样并不能保证消费消息后，insert日志表会成功，
         * 这里应该进行手动的ack确认，先插入日志表，如果成功再ack*/
        final EventSubscriber subscriber = new EventSubscriber();
        subscriber.setBusinessType(businessType);
        subscriber.setPayload(payload);
        subscriber.setGuid(guid);
        subscriber.setLockVersion(0);
        subscriber.setEventStatus(EventStatus.NEW);
        HibernateValidators.throwsIfInvalid(subscriber);
        int influence = 0;
        try {
            /**这里通过消息的guid数据库唯一来实现去重*/
            influence = subscriberMapper.insertSelective(subscriber);
        } catch (DuplicateKeyException e) {
            LOGGER.info("duplicate key in processing message '{}'", guid);
        }
        // 非重复消息则执行实际的业务
        if (influence > 0) {
            handler.handle(subscriber);
        }
        return influence;
    }

}
