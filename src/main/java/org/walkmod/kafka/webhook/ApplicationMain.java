package org.walkmod.kafka.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static spark.Spark.*;

public class ApplicationMain {

    private static final Logger log = LoggerFactory.getLogger(ApplicationMain.class);

    private static final String WEBHOOK = "webhook";

    public static void main(String[] args) throws Exception {

        WebhooksNotifier messageProducer = new WebhooksNotifier();
        ObjectMapper mapper = new ObjectMapper();
        WebhooksManager manager = new WebhooksManager();

        port(8081);

        //test URL
        get("/", (request, response) -> "It works!");

        post("/message/*", (request, response) -> {
            log.info("New message");
            String wildcardValue =  request.splat()[0].trim();
            messageProducer.publish(wildcardValue, request.body());
            return request.body();
        });

        post("/" + WEBHOOK, (request, response) -> {
            log.info("New webhook");
            Webhook webhook = mapper.readValue(request.body(), Webhook.class);
            try {
                manager.create(webhook);
            } catch (InvalidWebhookException e) {
                response.status(HttpStatus.BAD_REQUEST_400);
                return "";
            }
            return request.body();
        });

        delete("/" + WEBHOOK, (request, response) -> {
            log.info("Delete webhook");
            try {
                manager.delete(mapper.readValue(request.body(), Webhook.class));
            } catch (InvalidWebhookException e) {
                response.status(HttpStatus.BAD_REQUEST_400);
                return "";
            }
            return request.body();
        });

        get("/" + WEBHOOK, (request, response) -> {
            return mapper.writeValueAsString(manager.get());
        });

        //TESTING: a self webhook that receives events
        post("/test/" + WEBHOOK,  (request, response) -> {
            log.info("Yeah! a webhook notification");
            log.info(request.body());
            return "";
        });
        log.info("Ready to accept connections! at http://localhost:8081");

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                log.info("Shutdown of Kafka consumers...");
                manager.shutdown();
                log.info("Shutdown SUCCESSFUL");
            }
        });
    }
}
