package com.cyril.event.handle;

import com.cyril.event.Event;

public interface RetryHandler {
    /**
     * 是否要跳过本次运行
     * @param event
     * @return
     */
    boolean shouldJumpCurrRun(Event event) ;

    /**
     * 是否要退出
     * @param event
     * @return
     */
    boolean shouldExit(Event event);
}
