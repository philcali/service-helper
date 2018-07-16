package me.philcali.service.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import me.philcali.service.binding.IOperation;
import me.philcali.service.binding.request.IRequest;
import me.philcali.service.binding.response.IResponse;
import me.philcali.service.binding.response.Response;
import me.philcali.service.reflection.function.ITranslation;
import me.philcali.service.reflection.response.IResponseTranslation;

public class ReflectiveOperation implements IOperation<IRequest, IResponse> {
    private final Object object;
    private final Method method;
    private final ITranslation<Parameter, IRequest> parameterTranslation;
    private final IResponseTranslation responseTranslation;

    public ReflectiveOperation(
            final Object object,
            final Method method,
            final ITranslation<Parameter, IRequest> parameterTranslation,
            final IResponseTranslation responseTranslation) {
        this.object = object;
        this.method = method;
        this.parameterTranslation = parameterTranslation;
        this.responseTranslation = responseTranslation;
    }

    public Method getMethod() {
        return method;
    }

    @Override
    public IResponse apply(final IRequest input) {
        try {
            final Parameter[] parameters = method.getParameters();
            final Object[] inputs = new Object[parameters.length];
            for (int index = 0; index < parameters.length; index++) {
                inputs[index] = parameterTranslation.apply(parameters[index], input);
            }
            final Object result = method.invoke(object, inputs);
            return responseTranslation.translate(result, method);
        } catch (InvocationTargetException ie) {
            throw (RuntimeException) ie.getCause();
        } catch (Exception e) {
            return Response.internalError(e);
        }
    }
}
