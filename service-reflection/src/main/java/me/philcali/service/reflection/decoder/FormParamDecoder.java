package me.philcali.service.reflection.decoder;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class FormParamDecoder implements IParamDecoder {
    @Override
    public Map<String, String> decode(final String data) {
        return Arrays.stream(data.split("&"))
                .map(part -> part.split("="))
                .collect(Collectors.toMap(part -> part[0], part -> part[1]));
    }
}
