package me.philcali.service.reflection.verb;

import me.philcali.service.annotations.POST;
import me.philcali.service.binding.ResourceMethod;
import me.philcali.service.binding.ResourceMethod.Builder;

public class PostResourceMethodLocator implements IHttpVerbTranslation<POST> {

    @Override
    public Builder translate(final POST annotation) {
        return ResourceMethod.builder()
                .withMethod("POST")
                .withPatternPath(annotation.value());
    }

}
