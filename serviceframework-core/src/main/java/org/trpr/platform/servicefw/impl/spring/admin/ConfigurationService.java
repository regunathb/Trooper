package org.trpr.platform.servicefw.impl.spring.admin;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.trpr.platform.servicefw.spi.ServiceKey;

/**
 * Created with IntelliJ IDEA.
 * User: root
 * Date: 11/6/13
 * Time: 1:06 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ConfigurationService {

    public Resource getConfig(ServiceKey serviceKey);

    public void modifyConfig(ServiceKey handlerName, ByteArrayResource modifiedHandlerConfigFile);

    public void addService(ServiceKey serviceKey, Resource configFile);
}
