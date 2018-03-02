package me.philcali.service.reflection.decoder;

import java.util.Map;

public interface IParamDecoder {
    Map<String, String> decode(String data);
}
