package org.walkmod.kafka.webhook;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class WebhooksManager {

    private static final int THREAD_POOL_SIZE = 10;

    private final ExecutorService executorService;

    private final Map<String, WebhookConsumer> webhooks;

    public WebhooksManager() {
        executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        webhooks = new HashMap<>();
    }

    public void create(Webhook webhook) throws InvalidWebhookException {

        if (!webhook.isValid()) {
            throw new InvalidWebhookException();
        }

        WebhookConsumer consumer = newConsumer(webhook);
        executorService.execute(consumer);
        webhooks.put(webhook.getUrl(), consumer);
    }

    protected WebhookConsumer newConsumer(Webhook webhook) {
        return new WebhookConsumer(webhook);
    }

    public void delete(Webhook webhook) throws InvalidWebhookException {

        if (!webhook.hasUrl()) {
            throw new InvalidWebhookException();
        }

        WebhookConsumer consumer = webhooks.get(webhook.getUrl());

        if (consumer != null) {
            consumer.close();
        }
    }

    public List<Webhook> get() {
        return webhooks.values().stream().map(WebhookConsumer::getWebhook).collect(Collectors.toList());
    }

    public void shutdown() {

        webhooks.values().stream().forEach(WebhookConsumer::close);
        try {
            executorService.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executorService.shutdown();
    }
}
