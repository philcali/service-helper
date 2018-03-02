package me.philcali.service.reflection;

import java.lang.reflect.Method;

import me.philcali.service.binding.IOperation;
import me.philcali.service.binding.request.IRequest;
import me.philcali.service.binding.response.IResponse;

public interface IReflectiveOperationFactory {
    IOperation<IRequest, IResponse> create(Object object, Method method);
}
