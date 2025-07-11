package interview.controller;

import interview.BatchingService;
import interview.dto.EventRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/ingest")
public class EventController {

    @GetMapping("/hello")
    String getHello(){
        return "hello";
    }

    private Set<String> validCustomerTiers;

    @Autowired
    private BatchingService batchingService;

    @PostConstruct
    public void init() {
        validCustomerTiers = Set.of("gold", "silver", "platinum");
//        validCustomerTiers = new HashSet<>(validCustomerTiersList);
    }

    @PostMapping
    public ResponseEntity<?> ingestEvent(
            @RequestHeader(value = "X-Customer-Tier", required = false) String customerTier,
            @RequestBody EventRequest eventRequest) {

        if (customerTier == null || !validCustomerTiers.contains(customerTier.toLowerCase())) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Invalid or missing X-Customer-Tier header");
        }

        batchingService.addEvent(eventRequest);

        return ResponseEntity.ok().body("{\"status\":\"success\"}");
    }

}
