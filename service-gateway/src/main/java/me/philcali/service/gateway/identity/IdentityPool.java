package me.philcali.service.gateway.identity;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder;
import com.amazonaws.services.identitymanagement.model.CreateRoleRequest;
import com.amazonaws.services.identitymanagement.model.CreateRoleResult;
import com.amazonaws.services.identitymanagement.model.GetRoleRequest;
import com.amazonaws.services.identitymanagement.model.NoSuchEntityException;
import com.amazonaws.services.identitymanagement.model.Role;

public class IdentityPool implements IUserPool {
    private static final String ASSUME_ROLE_DOCUMENT;
    private final AmazonIdentityManagement iam;
    private final Map<String, Role> roleCache;
    static {
        ASSUME_ROLE_DOCUMENT = "{\n" +
                "  \"Version\": \"2012-10-17\",\n" +
                "  \"Statement\": [\n" +
                "    {\n" +
                "      \"Effect\": \"Allow\",\n" +
                "      \"Principal\": {\n" +
                "        \"Service\": [\n" +
                "          \"lambda.amazonaws.com\",\n" +
                "          \"apigateway.amazonaws.com\"\n" +
                "        ]\n" +
                "      },\n" +
                "      \"Action\": \"sts:AssumeRole\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }

    public IdentityPool(final AmazonIdentityManagement iam) {
        this.iam = iam;
        this.roleCache = new ConcurrentHashMap<>();
    }

    public IdentityPool(final String region) {
        this(Optional.ofNullable(region)
                .map(r -> AmazonIdentityManagementClientBuilder.standard().withRegion(r).build())
                .orElseGet(AmazonIdentityManagementClientBuilder::defaultClient));
    }

    @Override
    public Role getRole(final String roleName) {
        return roleCache.computeIfAbsent(roleName, name -> {
            try {
                return iam.getRole(new GetRoleRequest().withRoleName(name)).getRole();
            } catch (NoSuchEntityException e) {
                final CreateRoleResult result = iam.createRole(new CreateRoleRequest()
                        .withAssumeRolePolicyDocument(ASSUME_ROLE_DOCUMENT)
                        .withRoleName(name)
                        .withDescription("IAM role created to allow serverless interaction"));
                return result.getRole();
            }
        });
    }
}
