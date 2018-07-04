package me.philcali.service.function.auth;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import me.philcali.service.function.auth.IPolicy;
import me.philcali.service.function.auth.Policy;

public class PolicyTest {

    @Test(expected = NullPointerException.class)
    public void testRequiredPrincipalId() {
        Policy.builder().build();
    }

    @Test(expected = IllegalStateException.class)
    public void testRequiredMethods() {
        Policy.builder().withPrincipalId("abc-123").build();
    }

    @Test
    public void testRequiredMethodParams() {
        String methodArn = "arn:aws:api-gateway:us-east-1:abc123:example/prod/GET/cats";
        IPolicy policy = Policy.builder().withPrincipalId("abc-123").withMethodArn(methodArn).allowAllMethods()
                .withContext("name", "Philip Cali").build();
        final Map<String, Object> policyDocument = new HashMap<>();
        final List<Map<String, Object>> statements = new ArrayList<>();
        final Map<String, Object> allowStatement = new HashMap<>();
        allowStatement.put("Effect", "Allow");
        allowStatement.put("Action", "execute-api:Invoke");
        allowStatement.put("Resource", Arrays.asList("arn:aws:execute-api:us-east-1:abc123:example/prod/*/*"));
        statements.add(allowStatement);
        policyDocument.put("Version", "2012-10-17");
        policyDocument.put("Statement", statements);
        assertEquals("abc-123", policy.getPrincipalId());
        assertEquals(policyDocument, policy.getPolicyDocument());
    }

    @Test
    public void testEquals() {
        String methodArn = "arn:aws:api-gateway:us-east-1:abc123:example/prod/GET/cats";
        IPolicy policy = Policy.builder().withPrincipalId("abc-123").withMethodArn(methodArn).allowAllMethods().build();
        IPolicy other = Policy.builder().withPrincipalId("abc-123").withRegion("us-east-1").withAccountId("abc123")
                .withGatewayId("example").withStage("prod").allowAllMethods().build();
        assertEquals(policy, other);
    }
}