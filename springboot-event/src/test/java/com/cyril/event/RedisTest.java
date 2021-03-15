package com.cyril.event;

import com.cyril.event.channel.RedisEventChannel;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
@Slf4j
public class RedisTest {

    @Autowired
    private RedisEventChannel redisEventChannel;

    @Test
    public void testSend() throws InterruptedException {
        redisEventChannel.publish("10010", "10010decanshu");
        Thread.sleep(10 * 60 * 1000);
    }

}
