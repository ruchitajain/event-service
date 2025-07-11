package interview;

import com.fasterxml.jackson.databind.ObjectMapper;
import interview.dto.EventRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Slf4j
@Service
public class BatchingService {
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    //    @Value("${event.batch.max-size-bytes:5242880}") // 5MB
    private int maxBatchSizeBytes=5242880;

//    @Value("${event.batch.flush-interval-millis:5000}") // 5 seconds
    private long flushIntervalMillis=5000;
//    @Value("${event.s3.bucket}")
    private String s3Bucket="ruchita-events";

//    @Value("${event.s3.region:us-east-1}")
    private String awsRegion="eu-north-1";
    private final List<EventRequest> eventBuffer = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ByteArrayOutputStream bufferSizeTracker = new ByteArrayOutputStream();
    private S3Client s3Client;

    @PostConstruct
    public void init() {
        s3Client = S3Client.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
        scheduler.scheduleAtFixedRate(this::flush, flushIntervalMillis, flushIntervalMillis, TimeUnit.MILLISECONDS);
    }

    public synchronized void addEvent(EventRequest event) {
        try {
            byte[] eventBytes = objectMapper.writeValueAsBytes(event);
            if (bufferSizeTracker.size() + eventBytes.length >= maxBatchSizeBytes) {
                flush();
            }
            bufferSizeTracker.write(eventBytes);
            eventBuffer.add(event);
        } catch (Exception e) {
            log.error("Error buffering event", e);
        }
    }

    public synchronized void flush() {
        if (eventBuffer.isEmpty()) return;

        try {
            // Serialize and write to destination (e.g., S3, disk)
            List<EventRequest> toFlush = List.copyOf(eventBuffer);
            String jsonBatch = objectMapper.writeValueAsString(toFlush);
            byte[] contentBytes = jsonBatch.getBytes(StandardCharsets.UTF_8);

            String objectKey = generateS3Key();

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(s3Bucket)
                    .key(objectKey)
                    .contentType("application/json")
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(contentBytes));
            log.info("Flushing batch of {} events (size={} bytes)", eventBuffer.size(), bufferSizeTracker.size());
        } catch (Exception e) {
            log.error("Failed to flush batch", e);
        } finally {
            eventBuffer.clear();
            bufferSizeTracker.reset();

            bufferSizeTracker.reset();
        }
    }
    private String generateS3Key() {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy/MM/dd/HH/mm-ss-SSS"));
        return "events/" + timestamp + ".json";
    }
}
