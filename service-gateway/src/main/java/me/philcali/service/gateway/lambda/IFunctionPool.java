package me.philcali.service.gateway.lambda;

public interface IFunctionPool {
    ServerlessFunction getFunction(String functionName);
}
