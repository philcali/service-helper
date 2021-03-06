package me.philcali.service.binding;

import java.util.function.Predicate;

import me.philcali.service.binding.request.IRequest;
import me.philcali.service.binding.response.IResponse;

public class ResourceMethod implements Predicate<IRequest>, IOperation<IRequest, IResponse> {
    public static class Builder {
        private String method;
        private String patternPath;
        private IOperation<IRequest, IResponse> operation;

        public ResourceMethod build() {
            return new ResourceMethod(this);
        }

        public Builder withMethod(final String method) {
            this.method = method;
            return this;
        }

        public Builder withOperation(final IOperation<IRequest, IResponse> operation) {
            this.operation = operation;
            return this;
        }

        public Builder withPatternPath(final String patternPath) {
            this.patternPath = patternPath;
            return this;
        }
    }

    public static Builder builder() {
        return new Builder();
    }


    private final String method;
    private final String patternPath;
    private final IOperation<IRequest, IResponse> operation;

    private ResourceMethod(final Builder build) {
        this.method = build.method;
        this.patternPath = build.patternPath;
        this.operation = build.operation;
    }
    
    public String getMethod() {
        return method;
    }
    
    public String getPatternPath() {
        return patternPath;
    }
    
    public IOperation<IRequest, IResponse> getOperation() {
        return operation;
    }

    @Override
    public IResponse apply(final IRequest input) {
        return operation.apply(input);
    }

    @Override
    public boolean test(final IRequest request) {
        return request.getHttpMethod().equalsIgnoreCase(method)
                && request.getResource().equalsIgnoreCase(patternPath);
    }
}
