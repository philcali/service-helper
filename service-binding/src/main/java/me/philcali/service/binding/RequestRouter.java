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
        private List<ResourceMethod> resources = new ArrayList<>();
        private Supplier<IResponse> notFound;

        public RequestRouter build() {
            return new RequestRouter(this);
        }

        public Builder withNotFound(final Supplier<IResponse> notFound) {
            this.notFound = notFound;
            return this;
        }

        public Builder withResources(final List<ResourceMethod> resources) {
            this.resources = resources;
            return this;
        }

        public Builder withResources(ResourceMethod... resources) {
            Arrays.stream(resources).forEach(this.resources::add);
            return this;
        }
    }

    private final List<ResourceMethod> resources;
    private final Supplier<IResponse> notFound;

    private RequestRouter(final Builder builder) {
        this.resources = builder.resources;
        this.notFound = Optional.ofNullable(builder.notFound).orElseGet(() -> Response::notFound);
    }

    @Override
    public IResponse apply(final IRequest input) {
        try {
            return resources.stream()
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
