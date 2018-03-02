package me.philcali.service.reflection.verb;

import me.philcali.service.annotations.GET;
import me.philcali.service.binding.ResourceMethod;
import me.philcali.service.binding.ResourceMethod.Builder;

public class GetResourceMethodLocator implements IHttpVerbTranslation<GET> {

    @Override
    public Builder translate(final GET annotation) {
        return ResourceMethod.builder()
                .withMethod("GET")
                .withPatternPath(annotation.value());
    }
}
