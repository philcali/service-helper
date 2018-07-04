package me.philcali.service.reflection;

import java.util.List;

/**
 * This is a tagged interface to allow SPI injection of reflective modules
 *
 * @author philcali
 */
public interface IModule {
    List<Object> getComponents();
}
