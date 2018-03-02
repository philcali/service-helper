package me.philcali.service.reflection.verb;

import me.philcali.service.annotations.DELETE;
import me.philcali.service.binding.ResourceMethod;
import me.philcali.service.binding.ResourceMethod.Builder;

public class DeleteResourceMethodLocator implements IHttpVerbTranslation<DELETE> {

    @Override
    public Builder translate(final DELETE annotation) {
        return ResourceMethod.builder()
                .withMethod("DELETE")
                .withPatternPath(annotation.value());
    }

}
