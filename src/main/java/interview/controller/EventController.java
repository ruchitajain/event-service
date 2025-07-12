package interview.controller;

import interview.service.BatchingService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.micrometer.core.annotation.Timed;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/ingest")
public class EventController {
    @Value("#{'${event.valid-customer-tiers}'.split(',')}")
    private List<String> validCustomerTiersList;

    private Set<String> validCustomerTiers;

    @Autowired
    private BatchingService batchingService;

    @Autowired
    private MeterRegistry meterRegistry;

    private Counter dropRequests;

    @PostConstruct
    public void init() {
        validCustomerTiers = new HashSet<>(validCustomerTiersList);
        dropRequests = meterRegistry.counter("custom.requests.drop");
    }

    @GetMapping("/hello")
    String getHello(){
        return "hello";
    }

    @Timed(value = "ingest.request.time", percentiles = {0.95, 0.99}, histogram = true)
    @PostMapping
    public ResponseEntity<?> ingestEvent(@RequestBody String eventPayload, HttpServletRequest request) {

        String customerTier = request.getHeader("X-Customer-Tier");
        if (customerTier == null || !validCustomerTiers.contains(customerTier)) {
            dropRequests.increment();
            return new ResponseEntity<>("Invalid or missing X-Customer-Tier header", HttpStatus.BAD_REQUEST);
        }

        batchingService.addEvent(eventPayload);
        return new ResponseEntity<>("Event received successfully", HttpStatus.OK);
    }

}
