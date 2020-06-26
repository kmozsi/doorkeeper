package com.karanteam.doorkeeper.messaging;

import com.karanteam.doorkeeper.config.MessagingConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MessageProducer {

    private final Producer<Long, String> producer;
    private final MessagingConfig messagingConfig;

    public MessageProducer(Producer<Long, String> producer, MessagingConfig messagingConfig) {
        this.producer = producer;
        this.messagingConfig = messagingConfig;
    }

    public void sendMessage(final String message) {
        try {
            final ProducerRecord<Long, String> record = new ProducerRecord<>(messagingConfig.getTopic(), message);
            producer.send(record, (metadata, exception) -> {
                if (metadata != null) {
                    log.info("Notification sent (key={} value={}), meta(partition={}, offset={})",
                        record.key(), record.value(), metadata.partition(), metadata.offset());
                } else {
                    log.error("Notification could not be sent: " + exception.getMessage());
                }
            });
        } finally {
            producer.flush();
            producer.close();
        }
    }
}
