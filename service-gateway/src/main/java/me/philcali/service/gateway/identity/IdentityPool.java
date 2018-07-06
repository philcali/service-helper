package me.philcali.service.gateway.identity;

import java.util.Optional;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder;
import com.amazonaws.services.identitymanagement.model.CreateRoleRequest;
import com.amazonaws.services.identitymanagement.model.CreateRoleResult;
import com.amazonaws.services.identitymanagement.model.GetRoleRequest;
import com.amazonaws.services.identitymanagement.model.NoSuchEntityException;
import com.amazonaws.services.identitymanagement.model.Role;

public class IdentityPool {
    private static final String ASSUME_ROLE_DOCUMENT;
    private final AmazonIdentityManagement iam;
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
    }

    public IdentityPool(final String region) {
        this(Optional.ofNullable(region)
                .map(r -> AmazonIdentityManagementClientBuilder.standard().withRegion(r).build())
                .orElseGet(AmazonIdentityManagementClientBuilder::defaultClient));
    }

    public Role getRole(final String roleName) {
        try {
            return iam.getRole(new GetRoleRequest().withRoleName(roleName)).getRole();
        } catch (NoSuchEntityException e) {
            final CreateRoleResult result = iam.createRole(new CreateRoleRequest()
                    .withAssumeRolePolicyDocument(ASSUME_ROLE_DOCUMENT)
                    .withRoleName(roleName)
                    .withDescription("IAM role created to allow serverless interaction"));
            return result.getRole();
        }
    }
}
