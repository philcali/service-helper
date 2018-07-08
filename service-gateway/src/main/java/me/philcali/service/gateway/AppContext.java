package me.philcali.service.gateway;

import com.beust.jcommander.Parameter;

public class AppContext {

    @Parameter(names = { "--jar", "-j" }, required = true)
    private String jarFile;

    @Parameter(names = { "--iam-role", "-i" }, required = true)
    private String iamRole;

    @Parameter(names = { "--service", "-s" }, required = true)
    private String serviceName;

    @Parameter(names = { "--region", "-r" }, required = false)
    private String regionName;

    @Parameter(names = { "--name", "-n" }, required = false)
    private String functionName = "ServiceFunction";

    @Parameter(names = { "--authorizer", "-a" }, required = false)
    private String authorizerName = "CustomAuthorizer";

    @Parameter(names = { "--timeout", "-t" }, required = false)
    private int timeout = 10;

    @Parameter(names = { "--memory", "-m" }, required = false)
    private int memorySize = 512;

    public String getJarFile() {
        return jarFile;
    }

    public String getIamRole() {
        return iamRole;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getRegionName() {
        return regionName;
    }

    public String getFunctionName() {
        return functionName;
    }

    public String getAuthorizerName() {
        return authorizerName;
    }

    public int getTimeout() {
        return timeout;
    }

    public int getMemorySize() {
        return memorySize;
    }
}
