package com.cyril.event.annoations;

import com.alibaba.fastjson.JSONObject;

public interface BroadcastConsumerInterface {
    void consume(JSONObject body);
}
