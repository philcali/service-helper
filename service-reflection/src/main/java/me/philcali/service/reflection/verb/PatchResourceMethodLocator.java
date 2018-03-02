package me.philcali.service.reflection.verb;

import me.philcali.service.annotations.PATCH;
import me.philcali.service.binding.ResourceMethod;
import me.philcali.service.binding.ResourceMethod.Builder;

public class PatchResourceMethodLocator implements IHttpVerbTranslation<PATCH> {

    @Override
    public Builder translate(final PATCH annotation) {
        return ResourceMethod.builder()
                .withMethod("PATCH")
                .withPatternPath(annotation.value());
    }

}
