package com.cyril.event.channel;

import com.cyril.event.handle.RetryStrategy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventOtherParam {

    private RetryStrategy retryStrategy;
}
