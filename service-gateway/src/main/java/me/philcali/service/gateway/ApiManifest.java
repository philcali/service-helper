package me.philcali.service.gateway;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.apigateway.AmazonApiGatewayClientBuilder;
import com.amazonaws.services.apigateway.model.AuthorizerType;
import com.amazonaws.services.apigateway.model.ContentHandlingStrategy;
import com.amazonaws.services.apigateway.model.CreateAuthorizerRequest;
import com.amazonaws.services.apigateway.model.CreateResourceRequest;
import com.amazonaws.services.apigateway.model.CreateRestApiRequest;
import com.amazonaws.services.apigateway.model.DeleteMethodRequest;
import com.amazonaws.services.apigateway.model.GetAuthorizersRequest;
import com.amazonaws.services.apigateway.model.GetResourceRequest;
import com.amazonaws.services.apigateway.model.GetResourcesRequest;
import com.amazonaws.services.apigateway.model.GetResourcesResult;
import com.amazonaws.services.apigateway.model.GetRestApisRequest;
import com.amazonaws.services.apigateway.model.IntegrationType;
import com.amazonaws.services.apigateway.model.Method;
import com.amazonaws.services.apigateway.model.PutIntegrationRequest;
import com.amazonaws.services.apigateway.model.PutIntegrationResponseRequest;
import com.amazonaws.services.apigateway.model.PutMethodRequest;
import com.amazonaws.services.apigateway.model.PutMethodResponseRequest;
import com.amazonaws.services.apigateway.model.RestApi;

import me.philcali.service.annotations.request.Authorizer;
import me.philcali.service.binding.ResourceMethod;
import me.philcali.service.gateway.lambda.IFunctionPool;
import me.philcali.service.gateway.lambda.ServerlessFunction;
import me.philcali.service.gateway.lambda.ServerlessFunctionPool;
import me.philcali.service.gateway.resource.IResourceLoader;
import me.philcali.service.gateway.resource.ResourceMethodLoader;
import me.philcali.service.reflection.ReflectiveOperation;

public class ApiManifest {
    private static final String DEFAULT_REGION = "us-east-1";
    private static final String API_GATEWAY_ARN = "arn:aws:execute-api:%s:%s:%s/*/*/*";
    private static final String API_GATEWAY_ARN_URI = "arn:aws:apigateway:%s:lambda:path/2015-03-31/functions/%s/invocations";
    private static final int MAX_LIMIT = 500;

    public static final class Builder {
        private IResourceLoader loader;
        private IFunctionPool functionPool;
        private AppContext context;
        private AmazonApiGateway gateway;

        public Builder withContext(final AppContext context) {
            this.context = context;
            return this;
        }

        public Builder withResourceMethodLoader(final IResourceLoader loader) {
            this.loader = loader;
            return this;
        }

        public Builder withFunctionPool(final IFunctionPool functionPool) {
            this.functionPool = functionPool;
            return this;
        }

        private void createApiGatewayClient() {
            if (Objects.isNull(gateway)) {
                gateway = Optional.ofNullable(context.getRegionName())
                        .map(r -> AmazonApiGatewayClientBuilder.standard().withRegion(r).build())
                        .orElseGet(AmazonApiGatewayClientBuilder::defaultClient);
            }
        }

        private void createResourceLoader() {
            if (Objects.isNull(loader)) {
                loader = new ResourceMethodLoader(context.getJarFile());
            }
        }

        private void createFunctionPool() {
            if (Objects.isNull(functionPool)) {
                functionPool = ServerlessFunctionPool.builder()
                        .withContext(context)
                        .build();
            }
        }

        public ApiManifest build() {
            Objects.requireNonNull(context, "App context must be present!");
            createApiGatewayClient();
            createResourceLoader();
            createFunctionPool();
            return new ApiManifest(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private final IResourceLoader loader;
    private final AmazonApiGateway gateway;
    private final IFunctionPool functionPool;
    private final AppContext context;

    public ApiManifest(final Builder builder) {
        this.loader = builder.loader;
        this.gateway = builder.gateway;
        this.context = builder.context;
        this.functionPool = builder.functionPool;
    }

    public void upsert() {
        final String restApiId = upsertRestApi();
        final Map<String, String> pathToResourceId = createResourceTable(restApiId);
        loader.getMethods().forEach((path, methods) -> {
            final String[] partParts = path.split("/");
            final StringJoiner fullPath = new StringJoiner("/");
            String parentId = null;
            for (final String part : partParts) {
                fullPath.add(part);
                if (part.isEmpty()) {
                    parentId = "/";
                    continue;
                }
                final String resourceId = pathToResourceId.get(parentId);
                parentId = fullPath.toString();
                pathToResourceId.computeIfAbsent(parentId, full -> createResource(
                        restApiId, resourceId, part));
            }
            final String paths = fullPath.toString();
            createMethods(restApiId, pathToResourceId.get(paths.isEmpty() ? "/" : paths), methods);
        });
        // TODO: destroy existing resources
    }

    private Map<String, String> createResourceTable(final String restApiId) {
        final Map<String, String> table = new LinkedHashMap<>();
        final GetResourcesResult result = gateway.getResources(new GetResourcesRequest()
                .withRestApiId(restApiId)
                .withLimit(MAX_LIMIT));
        result.getItems().forEach(resource -> {
            table.put(resource.getPath(), resource.getId());
        });
        return table;
    }

    private String upsertRestApi() {
        final List<RestApi> restApis = gateway.getRestApis(new GetRestApisRequest().withLimit(MAX_LIMIT)).getItems();
        for (final RestApi api : restApis) {
            if (api.getName().equalsIgnoreCase(context.getServiceName())) {
                return api.getId();
            }
        }
        final String restApiId = gateway.createRestApi(new CreateRestApiRequest()
                .withName(context.getServiceName()))
                .getId();
        authorizeFunctionsForApi(restApiId);
        return restApiId;
    }

    private void authorizeFunctionsForApi(final String restApiId) {
        Arrays.asList(context.getFunctionName(), context.getAuthorizerName()).forEach(functionName -> {
            final ServerlessFunction function = functionPool.getFunction(functionName);
            final String[] parts = function.getArn().split(":");
            final String gatewayArn = String.format(API_GATEWAY_ARN, parts[3], parts[4], restApiId);
            function.addPermission(gatewayArn);
        });
    }

    private String createResource(final String restApiId, final String parentId, final String pathPart) {
        System.out.println(String.format("Creating resource %s", pathPart));
        return gateway.createResource(new CreateResourceRequest()
                .withRestApiId(restApiId)
                .withPathPart(pathPart)
                .withParentId(parentId))
                .getId();
    }

    private void createMethods(final String restApiId, final String resourceId, final Map<String, ResourceMethod> methods) {
        final Map<String, Method> resourceMethods = Optional.ofNullable(gateway.getResource(new GetResourceRequest()
                .withEmbed("methods")
                .withResourceId(resourceId)
                .withRestApiId(restApiId))
                .getResourceMethods())
                .map(HashMap::new)
                .orElseGet(HashMap::new);
        for (Map.Entry<String, ResourceMethod> entry : methods.entrySet()) {
            final String lambdaArn = String.format(API_GATEWAY_ARN_URI,
                    Optional.ofNullable(context.getRegionName()).orElse(DEFAULT_REGION),
                    functionPool.getFunction(context.getFunctionName()).getArn());
            if (resourceMethods.containsKey(entry.getKey())) {
                resourceMethods.remove(entry.getKey());
                continue;
            }
            final Optional<String> authorizerId = extractAuthorizer(entry.getValue(), restApiId);
            // TODO: reflect parameter types and models ... saving that for another day
            gateway.putMethod(new PutMethodRequest()
                    .withAuthorizationType(authorizerId.map(a -> "CUSTOM").orElse("NONE"))
                    .withAuthorizerId(authorizerId.orElse(null))
                    .withRestApiId(restApiId)
                    .withResourceId(resourceId)
                    .withHttpMethod(entry.getKey()));
            gateway.putIntegration(new PutIntegrationRequest()
                    .withIntegrationHttpMethod("POST")
                    .withRestApiId(restApiId)
                    .withResourceId(resourceId)
                    .withHttpMethod(entry.getKey())
                    .withUri(lambdaArn)
                    .withType(IntegrationType.AWS_PROXY)
                    .withPassthroughBehavior("WHEN_NO_MATCH"));
            final Map<String, String> responseModels = new HashMap<>();
            responseModels.put("application/json", "Empty");
            gateway.putMethodResponse(new PutMethodResponseRequest()
                    .withHttpMethod(entry.getKey())
                    .withRestApiId(restApiId)
                    .withResourceId(resourceId)
                    .withStatusCode("200")
                    .withResponseModels(responseModels));
            final Map<String, String> responseTemplates = new HashMap<>();
            responseTemplates.put("application/json", null);
            gateway.putIntegrationResponse(new PutIntegrationResponseRequest()
                    .withRestApiId(restApiId)
                    .withResourceId(resourceId)
                    .withHttpMethod(entry.getKey())
                    .withContentHandling(ContentHandlingStrategy.CONVERT_TO_TEXT)
                    .withStatusCode("200")
                    .withResponseTemplates(responseTemplates));
            resourceMethods.remove(entry.getKey());
        }
        resourceMethods.keySet().forEach(method -> {
            gateway.deleteMethod(new DeleteMethodRequest()
                    .withHttpMethod(method)
                    .withRestApiId(restApiId)
                    .withResourceId(resourceId));
        });
    }

    private Optional<String> extractAuthorizer(final ResourceMethod method, final String restApiId) {
        String authorizerId = null;
        Authorizer authorizer = null;
        try {
            final ReflectiveOperation operation = (ReflectiveOperation) method.getOperation();
            authorizer = operation.getMethod().getAnnotation(Authorizer.class);
        } catch (ClassCastException cce) {
            authorizer = method.getOperation().getClass().getAnnotation(Authorizer.class);
        }
        if (Objects.nonNull(authorizer)) {
            final String authorizerName = authorizer.value().getSimpleName();
            authorizerId = createOrGetAuthorizer(authorizerName, restApiId);
        }
        return Optional.ofNullable(authorizerId);
    }

    private String createOrGetAuthorizer(final String authorizerName, final String restApiId) {
        return gateway.getAuthorizers(new GetAuthorizersRequest()
                .withLimit(MAX_LIMIT)
                .withRestApiId(restApiId))
                .getItems()
                .stream()
                .filter(auth -> auth.getName().equals(authorizerName))
                .findFirst()
                .map(auth -> auth.getId())
                .orElseGet(() -> gateway.createAuthorizer(new CreateAuthorizerRequest()
                        .withName(authorizerName)
                        .withRestApiId(restApiId)
                        .withType(AuthorizerType.TOKEN)
                        .withIdentitySource("method.request.header.Authorization")
                        .withAuthorizerUri(String.format(API_GATEWAY_ARN_URI,
                                Optional.ofNullable(context.getRegionName()).orElse(DEFAULT_REGION),
                                functionPool.getFunction(context.getAuthorizerName()).getArn())))
                        .getId());
    }
}
