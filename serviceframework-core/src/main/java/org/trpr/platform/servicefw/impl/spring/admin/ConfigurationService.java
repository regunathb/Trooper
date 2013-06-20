package org.trpr.platform.servicefw.impl.spring.admin;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.trpr.platform.servicefw.spi.ServiceKey;

import java.io.IOException;

/**
 * <code>ConfigurationService</code> provides methods for accessing and modifying Configurations for Services
 *
 * @author devashish.shankar
 * @version 1.0, 12th June, 2013
 */
public interface ConfigurationService {

    /**
     * Gets the resource file for a Service Key
     * @param serviceKey The key of the Service for which Configuration has to be retrieved
     * @return Resource (file), null if not found
     */
    public Resource getConfig(ServiceKey serviceKey);

    /**
     * Modify the Configuration for a service
     * @param serviceKey The key of the Service for which Configuration has to be modified
     * @param modifiedConfigFile The byte array representation of the Configuration file
     */
    public void modifyConfig(ServiceKey serviceKey, ByteArrayResource modifiedConfigFile) throws IOException;

    /**
     * Add a service, along with its configFile.
     * @param serviceKey The key of the Service
     * @param configFile  The configFile containing the bean definition of the Service
     */
    public void addService(ServiceKey serviceKey, Resource configFile);

    /**
     * Reloads the configuration file containing the service
     * @param serviceKey the Service Key
     */
    void reloadConfig(ServiceKey serviceKey);
}
