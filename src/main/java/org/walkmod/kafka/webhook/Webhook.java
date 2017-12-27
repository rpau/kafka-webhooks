package org.walkmod.kafka.webhook;


import spark.utils.StringUtils;


import java.util.stream.Stream;

public class Webhook {

    private String url;
    private String topic;
    private String group;
    private boolean isWorking = true;
    private long timeout = 100;

    public Webhook() {}

    public Webhook(String url, long timeout, String group, String topic) {
        this.url = url;
        this.group = group;
        this.topic = topic;
        this.timeout = timeout;
    }

    public boolean isValid() {
        return  hasUrl()
                && StringUtils.isNotBlank(getGroup())
                && StringUtils.isNotBlank(getTopic());
    }

    public String getUrl() {
        return url;
    }

    public String getTopic() {
        return topic;
    }

    public String getGroup() {
        return group;
    }

    public boolean hasUrl() {
        return StringUtils.isNotBlank(getUrl());
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setTopic(String topic) {
        this.topic = topic;
        if (group == null && StringUtils.isNotBlank(topic)) {
            group = topic;
        }
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public boolean isWorking() {
        return isWorking;
    }

    public void isWorking(boolean isWorking) {
        this.isWorking = isWorking;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}
