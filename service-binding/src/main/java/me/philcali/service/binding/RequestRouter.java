package me.philcali.service.binding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import me.philcali.service.binding.request.IRequest;
import me.philcali.service.binding.response.HttpException;
import me.philcali.service.binding.response.IResponse;
import me.philcali.service.binding.response.Response;

public class RequestRouter implements IOperation<IRequest, IResponse> {
    public static class Builder {
        private List<ResourceMethod> resourceMethods = new ArrayList<>();
        private Supplier<IResponse> notFound;

        public RequestRouter build() {
            return new RequestRouter(this);
        }

        public Builder withNotFound(final Supplier<IResponse> notFound) {
            this.notFound = notFound;
            return this;
        }

        public Builder withResources(final List<ResourceMethod> resourceMethods) {
            this.resourceMethods.addAll(resourceMethods);
            return this;
        }

        public Builder withResources(ResourceMethod... resourceMethods) {
            return withResources(Arrays.asList(resourceMethods));
        }
    }

    public static RequestRouter.Builder builder() {
        return new Builder();
    }

    private final List<ResourceMethod> resourcesMethods;

    private final Supplier<IResponse> notFound;

    private RequestRouter(final Builder builder) {
        this.resourcesMethods = builder.resourceMethods;
        this.notFound = Optional.ofNullable(builder.notFound).orElseGet(() -> Response::notFound);
    }

    public List<ResourceMethod> getResourceMethods() {
        return resourcesMethods;
    }

    @Override
    public IResponse apply(final IRequest input) {
        try {
            return resourcesMethods.stream()
                    .filter(resource -> resource.test(input))
                    .findFirst()
                    .map(resource -> resource.apply(input))
                    .orElseGet(notFound);
        } catch (HttpException e) {
            return Response.builder()
                    .withStatusCode(e.getStatusCode())
                    .withException(e)
                    .build();
        } catch (Exception e) {
            return Response.internalError(e);
        }
    }
}
