package com.karanteam.doorkeeper.config;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class MessageProducerConfig {

    private final MessagingConfig messagingConfig;

    public MessageProducerConfig(MessagingConfig messagingConfig) {
        this.messagingConfig = messagingConfig;
    }

    @Bean
    public Producer<Long, String> messageProducer() {
        Properties props = new Properties();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, messagingConfig.getServerLocation());
        props.put(ProducerConfig.CLIENT_ID_CONFIG, messagingConfig.getClientId());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        return new KafkaProducer<>(props);
    }
}
