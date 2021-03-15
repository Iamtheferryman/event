package com.cyril.event.processor.function;

import com.cyril.event.processor.FunctionMap;
import com.cyril.event.processor.FunctionNameMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * function processor
 */
@Slf4j
public abstract class FunctionBroadcastProcessor extends FunctionProcessor {

    @Autowired
    @Qualifier(value = "broadcastFunctionMap")
    private FunctionMap functionMap;

    @Autowired
    @Qualifier(value = "broadcastFunctionNameMap")
    private FunctionNameMap functionNameMap;

}
