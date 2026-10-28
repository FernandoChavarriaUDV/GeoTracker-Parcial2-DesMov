package com.cargaglobal.mobilebackend.infrastructure.aws;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

class LambdaLivenessProviderTest {
    private LambdaLivenessProvider provider(boolean enabled, String legacy, String create, String result) {
        return new LambdaLivenessProvider(enabled, create, result, "compare-arn", "us-east-1", new ObjectMapper());
    }
    @Test void splitRequiresBothFunctions() {
        assertTrue(provider(true, "", "create-arn", "result-arn").configured());
        assertFalse(provider(true, "", "create-arn", "").configured());
        assertFalse(provider(true, "legacy", "", "result-arn").configured());
    }
    @Test void disabledAndEmptyNeverContactAws() {
        assertFalse(provider(false, "", "create-arn", "result-arn").configured());
        assertFalse(provider(true, "", "", "").configured());
        assertThrows(RuntimeException.class, () -> provider(false, "", "", "").create("request"));
    }
    @Test void requiresComparisonFunction() {
        assertFalse(new LambdaLivenessProvider(true,"create","result","","us-east-1",new ObjectMapper()).configured());
    }
}
