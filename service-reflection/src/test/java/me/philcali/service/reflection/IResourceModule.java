package me.philcali.service.reflection;

import me.philcali.service.annotations.Resource;

public interface IResourceModule {
    @Resource
    BookResource books();
}
