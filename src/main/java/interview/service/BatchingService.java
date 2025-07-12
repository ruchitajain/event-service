package interview.service;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Service
public class BatchingService {
    private final S3Client s3Client;
    private final MeterRegistry meterRegistry;

    @Value("${event.s3.bucket}")
    private String bucketName;

    private final ConcurrentLinkedQueue<String> eventQueue = new ConcurrentLinkedQueue<>();
    private final AtomicLong currentBatchSize = new AtomicLong(0);
    private static final long MAX_BATCH_SIZE = 5 * 1024 * 1024; // 5MB

    public BatchingService(S3Client s3Client, MeterRegistry meterRegistry) {
        this.s3Client = s3Client;
        this.meterRegistry = meterRegistry;
    }

    @Async
    public void addEvent(String eventPayload) {
        eventQueue.add(eventPayload);
        currentBatchSize.addAndGet(eventPayload.getBytes().length);

        if (currentBatchSize.get() >= MAX_BATCH_SIZE) {
            flushEvents();
        }
    }

    @Scheduled(fixedRate = 5000)
    public void flushOnSchedule() {
        if (!eventQueue.isEmpty()) {
            flushEvents();
        }
    }

    private synchronized void flushEvents() {
        if (eventQueue.isEmpty()) {
            return;
        }

        try {
            List<String> batch = new ArrayList<>();
            long batchSize = 0;

            while (!eventQueue.isEmpty() && batchSize < MAX_BATCH_SIZE) {
                String event = eventQueue.poll();
                if (event != null) {
                    batch.add(event);
                    batchSize += event.getBytes().length;
                }
            }

            String fileName = "batch-" + System.currentTimeMillis() + ".json";
            String batchContent = String.join("\n", batch);
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromString(batchContent));
            log.info("Batch stored in S3: " + fileName);

            // Increment success counter
            meterRegistry.counter("batches.processed.success").increment();
        } catch (Exception e) {
            log.error("Error storing batch in S3: " + e.getMessage());

            // Increment error counter
            meterRegistry.counter("batches.processed.error").increment();
        } finally {
            currentBatchSize.set(0);
        }
    }
}
