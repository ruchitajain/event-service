package interview.dto;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class EventRequest {
    private OffsetDateTime eventTimestamp;
    private String body;
}
