package com.karanteam.doorkeeper.service;

import com.karanteam.doorkeeper.config.MessagingConfig;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = MessagingService.class)
public class MessagingServiceTest {

    private static final String MESSAGE = "MESSAGE";
    private static final String TOPIC = "TOPIC";

    @Autowired
    private MessagingService messagingService;

    @MockBean
    private Producer<Long, String> producer;

    @MockBean
    private MessagingConfig messagingConfig;

    @Test
    public void testSendMessage() {
        when(messagingConfig.getTopic()).thenReturn(TOPIC);
        when(producer.send(any(), any())).thenReturn(
            CompletableFuture.completedFuture(
                new RecordMetadata(new TopicPartition(TOPIC, 0), 0, 0, 0, 0L, 0, 0)
            )
        );
        messagingService.sendMessage(MESSAGE);

        verify(producer, times(1)).send(eq(new ProducerRecord<>(TOPIC, MESSAGE)), any());
    }
}
