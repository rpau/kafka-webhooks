package org.walkmod.kafka.webhook;

import com.squareup.okhttp.*;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.stream.Collectors;

public class WebhookConsumer implements Runnable, Closeable {

    private final KafkaConsumer<String, String> consumer;
    private static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    private final OkHttpClient client = new OkHttpClient();
    private final Webhook webhook;
    private static final Logger log = LoggerFactory.getLogger(WebhookConsumer.class);
    private static final String KAFKA_PROPERTIES = "connect-standalone.properties";

    public WebhookConsumer(Webhook webhook) {
        Properties properties = loadKafkaProperties();
        properties.put("group.id", webhook.getGroup());
        this.webhook = webhook;
        consumer = new KafkaConsumer<>(properties);
        log.info("Subscription of {} in group {} to {}", webhook.getUrl(), webhook.getGroup(),
                webhook.getTopic());
        consumer.subscribe(Arrays.asList(webhook.getTopic()));
    }

    public Webhook getWebhook() {
        return webhook;
    }

    private Properties loadKafkaProperties() {
        Properties properties = new Properties();
        try {
            properties.load(new FileReader(KAFKA_PROPERTIES));
        } catch (Exception e) {
            log.error("Error reading Kafka consumer properties", e);
            throw new RuntimeException("Error reading kafka properties: " + KAFKA_PROPERTIES, e);
        }
        return properties;
    }

    @Override
    public void run() {
        try {
            log.info("Ready to consume messages");
            while (webhook.isWorking()) {
                ConsumerRecords<String, String> records = consumer.poll(webhook.getTimeout());

                for (ConsumerRecord<String, String> record : records) {
                    try {
                        log.info("Sending record {} to {}", record.value(), webhook.getUrl());
                        RequestBody body = RequestBody.create(JSON, record.value());
                        Request request = new Request.Builder()
                                .url(webhook.getUrl())
                                .post(body)
                                .build();
                        Response response = client.newCall(request).execute();
                        if (!response.isSuccessful()) {
                            log.info(webhook.getUrl() + " with " + record.value() + "returned " + response.code());
                            webhook.isWorking(false);
                            log.info(webhook.getUrl() + " [DISABLED]");
                        }
                    } catch (IOException e) {
                      log.error("Error performing a request to " + webhook.getUrl(), e);
                    }
                    log.info("Polling next records");
                }
            }
        }  catch (WakeupException e) {
            // ignore for shutdown
        } finally {
            consumer.close();
        }
    }

    @Override
    public void close() {
        consumer.wakeup();
    }
}
