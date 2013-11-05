package org.trpr.platform.servicefw.impl.spring.admin;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.trpr.platform.core.PlatformException;
import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;
import org.trpr.platform.servicefw.impl.spring.SpringServicesContainer;
import org.trpr.platform.servicefw.spi.ServiceKey;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *   <code>ConfigurationServiceImpl</code> is the default implementation of @link{ConfigurationService}.
 *   It stores and retrieves the configurations from file system.
 *
 *   @author devashish.shankar
 *   @version 1.0, 12th June, 2013
 */
public class ConfigurationServiceImpl implements ConfigurationService {

    /** Logger instance for this class*/
    private static final Logger LOGGER = LogFactory.getLogger(ConfigurationServiceImpl.class);

    /** The previous service file (save file) */
    public static final String PREV_CONFIG_FILE = "/spring-services-prev.xml";

    /** The map holding the mapping of a config file URI to the list of Services */
    private Map<URI,List<ServiceKey>> configFileToServices = new HashMap<URI, List<ServiceKey>>();

    /** The ComponentContainer for reloading */
    private SpringServicesContainer springServicesContainer;

    /**
     * Interface method implementation.
     * @param serviceKey The key of the Service for which Configuration has to be retrieved
     * @return
     */
    public Resource getConfig(ServiceKey serviceKey) {
        for(URI key : configFileToServices.keySet()) {
            if(this.configFileToServices.get(key).contains(serviceKey)) {
                return new FileSystemResource(key.getPath());
            }
        }
        return null;
    }

    /**
     * Reloads the configuration file containing the service
     * @param serviceKey the Service Key
     */
    @Override
    public void reloadConfig(ServiceKey serviceKey) {
         Resource config = this.getConfig(serviceKey);
         if(config!=null) {
            this.springServicesContainer.loadComponent(config);
         }
    }
    /**
     *Interface method implementation.
     * @param serviceName
     * @param modifiedServiceConfigFile
     */
    public void modifyConfig(ServiceKey serviceName, ByteArrayResource modifiedServiceConfigFile) throws IOException {
        //Check if Service file can be read
        File oldServiceFile = null;
        try {
            oldServiceFile = this.getConfig(serviceName).getFile();
        } catch (IOException e1) {
            LOGGER.error("Service Config File for service: "+serviceName+" not found. Returning");
            throw new PlatformException("File not found for service: "+serviceName,e1);
        }
        if(!oldServiceFile.exists()) {
            LOGGER.error("Service Config File: "+oldServiceFile.getAbsolutePath()+" doesn't exist. Returning");
            throw new PlatformException("File not found: "+oldServiceFile.getAbsolutePath());
        }
        if(!oldServiceFile.canRead()) {
            LOGGER.error("No read permission for: "+oldServiceFile.getAbsolutePath()+". Returning");
            throw new PlatformException("Read permissions not found for file: "+oldServiceFile.getAbsolutePath());
        }
        //Check if write permission is there
        if(!oldServiceFile.canWrite()) {
            LOGGER.error("No write permission for: "+oldServiceFile.getAbsolutePath()+". Write permissions are required to modify service confib");
            throw new PlatformException("Required permissions not found for modifying file: "+oldServiceFile.getAbsolutePath());
        }
        LOGGER.debug("Reloading configuration for service: "+serviceName);
        this.createPrevConfigFile(serviceName);
        LOGGER.debug("Created previous configuration file");
        try {
            this.upload(modifiedServiceConfigFile.getByteArray(), oldServiceFile.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.error("IOException while uploading file to path: "+oldServiceFile.getAbsolutePath());
            this.restorePrevConfigFile(serviceName);
            throw new PlatformException(e);
        }

        Resource configFile =  this.getConfig(serviceName);
        try {
            this.springServicesContainer.loadComponent(configFile);
        } catch(Exception e) {
            this.restorePrevConfigFile(serviceName);
            this.springServicesContainer.loadComponent(this.getConfig(serviceName));
            throw new PlatformException(e);
        }
        this.removePrevConfigFile(serviceName);
    }

    /**
     * Interface method implementation.
     * @param serviceKey The key of the Service
     * @param configFile  The configFile containing the bean definition of the Service
     */
    public void addService(ServiceKey serviceKey, Resource configFile) {
    	FileSystemResource fileResource = (FileSystemResource)configFile;
		// check if the bean definition XML really exists or is just a canonical location and therefore may be ignored in mapping services to XML location
		if (!fileResource.exists()) {
			LOGGER.debug("Not registering service bean configuration from : " + fileResource + " as file does not exist. Is this a canonical file?");
			return;
		} 
        try {
            if(this.configFileToServices.get(configFile.getURI())==null) {
                this.configFileToServices.put(configFile.getURI(), new LinkedList<ServiceKey>());
            }
            this.configFileToServices.get(configFile.getURI()).add(serviceKey);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Creates a temporary file, which is a duplicate of the current config file,
     * with the name {@link ConfigurationServiceImpl#PREV_CONFIG_FILE}
     * @param serviceName Name of the Service
     */
    private void createPrevConfigFile(ServiceKey serviceName) {
        File configFile = null;
        try {
            configFile = this.getConfig(serviceName).getFile();
        } catch (IOException e1) {
            LOGGER.error("Exception while getting serviceConfigFile",e1);
            throw new PlatformException("Exception while getting serviceConfigFile",e1);
        }
        File prevFile = new File(configFile.getParent()+"/"+PREV_CONFIG_FILE);
        try {
            prevFile.createNewFile();
        } catch (IOException e1) {
            LOGGER.error("Unable to create file: "+prevFile.getAbsolutePath()+". Please check permissions",e1);
            throw new PlatformException("Unable to create file: "+prevFile.getAbsolutePath()+". Please check permissions",e1);
        }
        if(configFile.exists()) {
            if(prevFile.exists()) {
                prevFile.delete();
            }
            configFile.renameTo(prevFile);
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                LOGGER.error("IOException while clearing config File",e);
                throw new PlatformException("IOException while clearing config File",e);
            }
            prevFile.deleteOnExit();
        }
    }


    /**
     * This method removes the temporary previous config File
     * @param serviceName Name of the job
     */
    private void removePrevConfigFile(ServiceKey serviceName) {
        File configFile = null;
        try {
            configFile = this.getConfig(serviceName).getFile();
        } catch (IOException e) {
            LOGGER.error("IOException while getting ServiceConfigFile",e);
        }
        String prevFilePath = configFile.getParent()+PREV_CONFIG_FILE;
        File prevFile = new File(prevFilePath);
        if(prevFile.exists()){
            prevFile.delete();  // DELETE previous XML File
        }
    }

    /**
     * Restores the previous config file, if found
     * @param serviceName Name of the job
     */
    private void restorePrevConfigFile(ServiceKey serviceName) {
        File configFile = null;
        try {
            configFile = this.getConfig(serviceName).getFile();
        } catch (IOException e) {
            LOGGER.error("IOException while getting ServiceConfigFile",e);
        }
        if(configFile.exists()) {
            configFile.delete();
        }
        File prevFile = new File(configFile.getParent()+"/"+PREV_CONFIG_FILE);
        if(prevFile.exists()) {
            prevFile.renameTo(configFile);
        }
        //TODO: In case of new service, add: else { this.jobXMLFile.remove(serviceName); }
    }

    /**
     * Uploads the file to the given path. Creates the file and directory structure, if the file
     * or parent directory doesn't exist
     */
    private void upload(byte[] fileContents, String destPath) throws IOException {
        File destFile = new File(destPath);
        LOGGER.debug("Uploading fileContents to path: "+destPath);
        //If exists, overwrite
        if(destFile.exists()) {
            destFile.delete();
            destFile.createNewFile();
        }
        //Creating directory structure
        try {
            destFile.getParentFile().mkdirs();
        } catch(Exception e) {
            LOGGER.error("Unable to create directory structure for uploading file");
            throw new PlatformException("Unable to create directory structure for uploading file");
        }
        FileOutputStream fos = new FileOutputStream(destFile);
        fos.write(fileContents);
    }


    /** Getter/Setter methods */
    public SpringServicesContainer getSpringServicesContainer() {
        return springServicesContainer;
    }

    public void setSpringServicesContainer(SpringServicesContainer springServicesContainer) {
        this.springServicesContainer = springServicesContainer;
    }
}
