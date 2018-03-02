package me.philcali.service.reflection.verb;

import me.philcali.service.annotations.HEAD;
import me.philcali.service.binding.ResourceMethod;
import me.philcali.service.binding.ResourceMethod.Builder;

public class HeadResourceMethodLocator implements IHttpVerbTranslation<HEAD> {

    @Override
    public Builder translate(final HEAD annotation) {
        return ResourceMethod.builder()
                .withMethod("HEAD")
                .withPatternPath(annotation.value());
    }

}
