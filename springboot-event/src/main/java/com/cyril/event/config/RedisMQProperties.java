package com.cyril.event.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mq.redis")
@Data
public class RedisMQProperties {

    private String topic;

    private String processing;

}
