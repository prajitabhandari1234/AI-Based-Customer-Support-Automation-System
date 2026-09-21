package com.cqu.coit13230.AIBasedCustomerSupport.testing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.cqu.coit13230.AIBasedCustomerSupport.model.TicketCategory;
import com.cqu.coit13230.AIBasedCustomerSupport.service.AiService;

/**
 * Verifies that an unavailable OpenAI endpoint falls back to the local AI
 * rules so a support request can still be analysed without failing.
 */
@SpringBootTest(properties = {
        "app.ai.provider=openai",
        "app.ai.openai.api-key=dummy-test-key",
        "app.ai.openai.endpoint=http://127.0.0.1:9/v1/responses",
        "app.ai.openai.connect-timeout-seconds=1",
        "app.ai.openai.request-timeout-seconds=1",
        "app.seed.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:fallbacktest;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
})
@ActiveProfiles("test")
class OpenAiFallbackResilienceTest {

    @Autowired
    private AiService aiService;

    @Test
    void unreachableOpenAiEndpointFallsBackToLocalRulesInsteadOfFailingRequest() {
        var result = aiService.analyse("My app is not working and shows an error");
        assertTrue(result.inScope());
        assertEquals(TicketCategory.TECHNICAL, result.category());
        assertTrue(result.reply().contains("technical problem") || result.reply().contains("escalated"));
    }
}
