package me.philcali.service.reflection.verb;

import me.philcali.service.annotations.OPTIONS;
import me.philcali.service.binding.ResourceMethod;
import me.philcali.service.binding.ResourceMethod.Builder;

public class OptionsResourceMethodLocator implements IHttpVerbTranslation<OPTIONS> {

    @Override
    public Builder translate(final OPTIONS annotation) {
        return ResourceMethod.builder()
                .withMethod("OPTIONS")
                .withPatternPath(annotation.value());
    }

}
