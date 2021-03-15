package com.cyril.event;

import com.cyril.event.channel.OnsEventChannel;
import com.cyril.event.channel.RedisEventChannel;
import com.cyril.event.exception.PublishEventException;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
@Slf4j
public class OnsTest {

    @Autowired
    private OnsEventChannel onsEventChannel;

    @Autowired
    private RedisEventChannel redisEventChannel;

    @Test
    public void testSend() throws InterruptedException, PublishEventException {
        int i = 0;
        while (true) {
            String id = "" + (i++);
//            redisEventChannel.publish("10010", "BBBB");
            onsEventChannel.publish("10011", "AAAA" + id);
            System.out.println("发送id====" + id);
            Thread.sleep(200);
        }

//        redisEventChannel.publish("10010", "BBBB");
//        onsEventChannel.publish("10011", "CCCC");
//        redisEventChannel.publish("10011", "DDDD");
//        Thread.sleep(10 * 60 * 1000);
    }

}
