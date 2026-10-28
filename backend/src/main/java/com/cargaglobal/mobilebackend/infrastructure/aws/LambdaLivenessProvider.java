package com.cargaglobal.mobilebackend.infrastructure.aws;

import com.cargaglobal.mobilebackend.application.port.LivenessProvider;
import com.cargaglobal.mobilebackend.application.exception.LivenessUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import java.time.Duration;
import java.util.Map;

@Component
public class LambdaLivenessProvider implements LivenessProvider {
    private final boolean enabled;
    private final String createFunction, resultFunction, compareFunction, region;
    private final ObjectMapper json;
    public LambdaLivenessProvider(@Value("${liveness.enabled:false}") boolean enabled,
            @Value("${liveness.create-function:}") String createFunction,
            @Value("${liveness.result-function:}") String resultFunction,
            @Value("${liveness.compare-function:}") String compareFunction,
            @Value("${liveness.region:us-east-1}") String region, ObjectMapper json) {
        this.enabled = enabled; this.createFunction = createFunction;
        this.resultFunction = resultFunction; this.compareFunction = compareFunction; this.region = region; this.json = json;
    }
    public boolean configured() {
        return enabled && !createFunction.isBlank() && !resultFunction.isBlank() && !compareFunction.isBlank();
    }
    public String region() { return region; }
    private tools.jackson.databind.JsonNode invoke(String target, Map<String,String> payload) {
        if (!configured()) throw new LivenessUnavailableException();
        try (var client = LambdaClient.builder().region(Region.of(region)).overrideConfiguration(
                c -> c.apiCallTimeout(Duration.ofSeconds(20)).apiCallAttemptTimeout(Duration.ofSeconds(10))).build()) {
            var response = client.invoke(InvokeRequest.builder().functionName(target)
                .payload(SdkBytes.fromUtf8String(json.writeValueAsString(payload))).build());
            if (response.functionError() != null) throw new LivenessUnavailableException();
            var body = json.readTree(response.payload().asUtf8String());
            if (body.has("error")) throw new LivenessUnavailableException();
            return body;
        } catch (Exception e) { throw new LivenessUnavailableException(); }
    }
    public String create(String requestId) {
        String id = invoke(createFunction,
            Map.of("action","create","requestId",requestId)).path("sessionId").asText();
        if (id.isBlank()) throw new LivenessUnavailableException();
        return id;
    }
    public Result result(String sessionId) {
        var result = invoke(resultFunction,
            Map.of("action","result","sessionId",sessionId));
        String reference = result.hasNonNull("referenceBase64") ? result.path("referenceBase64").asText() : null;
        if (reference != null && reference.length() > 2000000) throw new LivenessUnavailableException();
        return new Result(result.path("status").asText(), result.hasNonNull("confidence") ? result.path("confidence").asDouble() : null,reference);
    }
    public Double compare(String referenceBase64, String candidateBase64) {
        if (compareFunction.isBlank()) throw new LivenessUnavailableException();
        var response=invoke(compareFunction,Map.of("referenceBase64",referenceBase64,"candidateBase64",candidateBase64));
        return response.hasNonNull("similarity") ? response.path("similarity").asDouble() : null;
    }
}
