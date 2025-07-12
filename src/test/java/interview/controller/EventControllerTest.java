package interview.controller;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventController.class)
public class EventControllerTest {
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private BatchingService batchingService;
//
//    @MockBean
//    private MeterRegistry meterRegistry;
//
//    private ObjectMapper objectMapper = new ObjectMapper();
//    private EventRequest sampleEvent;
//
//    @BeforeEach
//    void setup() {
//        sampleEvent = new EventRequest();
//        sampleEvent.setEventTimestamp(OffsetDateTime.now());
//        sampleEvent.setBody("testing");
//    }
//
//    @Test
//    void testIngestEvent_ShouldRecordTimedMetric() throws Exception {
//        // Perform request
//        mockMvc.perform(post("/ingest")
//                        .header("X-Customer-Tier", "gold")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(sampleEvent)))
//                .andExpect(status().isOk());
//
//        // Verify metric was recorded
//        Timer timer = meterRegistry.find("ingest.request.time").timer();
//        assertThat(timer).isNotNull();
//        assertThat(timer.count()).isGreaterThanOrEqualTo(1);
//    }
}
