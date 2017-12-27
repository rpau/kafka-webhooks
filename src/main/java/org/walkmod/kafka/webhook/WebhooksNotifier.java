package org.walkmod.kafka.webhook;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.util.Properties;

public class WebhooksNotifier {

    private final KafkaProducer<String, String> producer;
    private static final Logger log = LoggerFactory.getLogger(WebhooksNotifier.class);

    public WebhooksNotifier() throws Exception {
        Properties properties = new Properties();
        properties.load(new FileReader("connect-standalone.properties"));
        producer = new KafkaProducer<String, String>(properties);
    }

    public void publish(String topic, String payload) {
       ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic, payload);
       producer.send(record);
       log.info("Topic: {} Payload: {} sent to Kafka", topic, payload);
    }
}
