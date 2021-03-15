package com.cyril.event.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(OnsBroadcastMqProperties.class)
@ConfigurationProperties(prefix = "mq.ons")
@Data
@Getter
@Setter
public class OnsMQProperties {

    private String onsAddr;

    private String topic;

    private String accessKey;

    private String secretKey;

    private String producer;

    private String consumer;

}
