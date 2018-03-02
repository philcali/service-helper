package me.philcali.service.reflection.verb;

import me.philcali.service.annotations.PUT;
import me.philcali.service.binding.ResourceMethod;
import me.philcali.service.binding.ResourceMethod.Builder;

public class PutResourceMethodLocator implements IHttpVerbTranslation<PUT> {

    @Override
    public Builder translate(final PUT annotation) {
        return ResourceMethod.builder()
                .withMethod("PUT")
                .withPatternPath(annotation.value());
    }

}
