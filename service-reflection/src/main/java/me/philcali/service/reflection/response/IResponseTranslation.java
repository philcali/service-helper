package me.philcali.service.reflection.response;

import java.lang.reflect.Method;

import me.philcali.service.binding.response.IResponse;

public interface IResponseTranslation {
    IResponse translate(Object result, Method method);
}
