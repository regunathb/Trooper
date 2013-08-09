/*
 * Copyright 2012-2015, the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.trpr.platform.runtime.impl.bootstrap.spring;


import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.Calendar;

import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;
import org.trpr.platform.core.spi.management.jmx.AppInstanceAwareMBean;
import org.trpr.platform.model.event.PlatformEvent;
import org.trpr.platform.runtime.common.RuntimeConstants;
import org.trpr.platform.runtime.common.RuntimeVariables;
import org.trpr.platform.runtime.impl.bootstrap.management.jmx.BootstrapModelMBeanExporter;
import org.trpr.platform.runtime.impl.config.FileLocator;
import org.trpr.platform.runtime.spi.bootstrap.BootstrapInfo;
import org.trpr.platform.runtime.spi.bootstrap.management.jmx.BootstrapManagedBean;
import org.trpr.platform.runtime.spi.container.Container;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * The <code>Bootstrap</code> class starts up the Trooper runtime as per the configured nature. This class is initialized using 
 * the absolute path to the bootstrap config file.
 * The runtime variables are loaded from this file and set into the {@link RuntimeVariables} instance. It also initializes the logging framework and
 * manages the configured {@link Container} via life cycle call back methods that may be invoked via management interfaces such as JMX MBeans.
 * 
 * @author Regunath B
 * @version 1.0, 06/06/2012
 */
public class Bootstrap extends AppInstanceAwareMBean implements BootstrapManagedBean, Runnable {

	/** States for the background thread */
	private static final int EXIT = 0;
	private static final int WAIT = 1;
	
	/** The Trooper startup display contents*/
	private static final MessageFormat STARTUP_DISPLAY = new MessageFormat( 
	    "\n*************************************************************************\n" +
        " Trooper __\n" +
        "      __/  \\" +  "         Runtime Nature : {0}" + "\n" +
        "   __/  \\__/" +  "         Component Container : {1}" + "\n" +
        "  /  \\__/  \\" + "         Startup Time : {2}" + " ms\n" +
        "  \\__/  \\__/" + "         Host Name: {3}" + "\n" +
        "     \\__/" + "\n" +
	    "*************************************************************************"
		);
	
	/** The prefix to be added to file absolute paths when loading Spring XMLs using the FileSystemXmlApplicationContext*/
	private static final String FILE_PREFIX = "file:";	
	
	/** The Logger instance for this class */
	private static final Logger LOGGER = LogFactory.getLogger(Bootstrap.class);
	
	/** The sleep duration before signalling a JVM kill via System.exit()*/
	private static final long SLEEP_BEFORE_EXIT = 2000;
	
	/** The background thread state */
	private int backgroundThreadState = WAIT;

	/** Path to bootstrap.config file */
	private String bootstrapConfigFile;

	/** The RuntimeVariable instance	 */
	private static RuntimeVariables runtimeVariables;
	
	/** The ApplicationContext that loaded the Container */
	private AbstractApplicationContext containerContext;

	/** The Container instance initialized by this Bootstrap */
	private Container container;
	
	/** The Thread's context class loader that is used in lifecycle methods of this Bootstrap */
	private ClassLoader tccl;
	
	/** The machine name where this Bootstrap is running */
	private String hostName;
	
	/**
	 * No args constructor
	 */
	public Bootstrap() {
		try {
		this.hostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			//ignore the exception, not critical information
		}
	}
	
	/**
	 * Initializes this Bootstrap
	 */
	public void init() {
		String configPath = System.getProperty(RuntimeConstants.CONFIG_FILE_VAR);
		if (configPath == null) {
			// logging has not been configured, so use System.out.println();
			System.out.println("Bootstrap config file not found. Trooper runtime exit.");
			return;
		}
		init(configPath);
	}

	/**
	 * Initializes this Bootstrap with the specified config file path
	 * 
	 * @param bootstrapConfigFile the runtime bootstrap config file
	 */
	public void init(String bootstrapConfigFile) {
		
		this.bootstrapConfigFile = bootstrapConfigFile;

		// store the thread's context class loader for later use by life cycle methods
		this.tccl = Thread.currentThread().getContextClassLoader();
		
		try {
			// start the runtime
			this.start();
			// Register this Bootstrap with the platform's MBean server after start() has been called and any app specific JMX name suffix has been loaded
			new BootstrapModelMBeanExporter().exportBootstrapMBean(this);
		} catch (Exception e) {
			// catch all block to consume and minimally log bootstrap errors
			// Log to both logger and System.out.println();
			LOGGER.error("Fatal error in bootstrap sequence. Cannot continue!",	e);
			System.out.println("Fatal error in bootstrap sequence. Cannot continue!");
			e.printStackTrace(System.out);
			return;
		}
		
		// Start up the background thread which will keep the JVM alive when #stop() is called on this Bootstrap via a management console/interface
		Thread backgroundThread = new Thread(this);
		backgroundThread.setName(RuntimeConstants.BOOTSTRAP_BACKGROUND_THREADNAME);
		backgroundThread.start();
	}

	/**
	 * Method to configure logging properties based on bootstrap location
	 */
	public void configureLogging() {
		File[] loggingConfigFiles = FileLocator.findFiles(RuntimeConstants.LOGGING_FILE, new File(this.bootstrapConfigFile).getParent());
		if (loggingConfigFiles.length == 0) {
			System.out.println("Logging configuration file : " + RuntimeConstants.LOGGING_FILE + " not found!. Unable to initialize logging framework");
			return;
		}
		if (loggingConfigFiles.length > 1) { // multiple logging configurations found. 
			// Prefer the logging config co-located with bootstrap config. Use first occurrence otherwise.
			for (File loggingFile : loggingConfigFiles) {
				if (loggingFile.getParent().equals(new File(getBootstrapConfigPath()).getParent())) {
					System.out.println("Multiple ( Total: "	+ loggingConfigFiles.length	+ " ) logging config files found. Using the occurence co-located with bootstrap config file.");
					configureLoggingFromFile(loggingFile);
					return;
				}
			}
		}
		System.out.println("Using logging config file : " + loggingConfigFiles[0].getAbsolutePath());
		configureLoggingFromFile(loggingConfigFiles[0]);
	}
	
	/**
	 * Interface method implementation
	 * @see BootstrapManagedBean#getBootstrapConfigPath()
	 */
	public String getBootstrapConfigPath() {
		return this.bootstrapConfigFile;
	}

	/**
	 * Interface method implementation
	 * @see BootstrapManagedBean#reloadLoggingConfigurations()
	 */
	public void reloadLoggingConfigurations(){
		this.configureLogging();
	}

	/**
	 * Interface method implementation
	 * @see BootstrapManagedBean#setBootstrapConfigPath(String)
	 */
	public void setBootstrapConfigPath(String bootstrapConfigPath){
		this.bootstrapConfigFile = bootstrapConfigPath;
	}

	/**
	 * Interface method implementation
	 * @see BootstrapManagedBean#start()
	 */
	public void start() throws Exception {
		// logging has not been configured, so use System.out.println();
		long start = System.currentTimeMillis();
		System.out.println("** Trooper runtime Bootstrap start **");
		
		// check if the tccl of the invoking thread is the same as the one that was used in #init(), else use the stored tccl
		if (Thread.currentThread().getContextClassLoader() != this.tccl) {
			Thread.currentThread().setContextClassLoader(tccl);
		}
		
		runtimeVariables = RuntimeVariables.getInstance();
		try {
			
			// configure the logging framework from a path relative to where the bootstrap config file is specified
			configureLogging();			
			
			// Load the bootstrap config file
			File bootstrapFile = new File(this.bootstrapConfigFile);
			// add the "file:" prefix to file names to get around strange behavior of FileSystemXmlApplicationContext that converts absolute path 
			// to relative path
			this.containerContext = new FileSystemXmlApplicationContext(FILE_PREFIX + bootstrapFile.getAbsolutePath());
			BootstrapInfo bootstrapInfo = (BootstrapInfo)this.containerContext.getBean(BootstrapInfo.class);
			
			// see if the path to projects root has been specified as a relative
			// path to the bootstrap.config file and
			// replace it with appropriate value to make it absolute			
			String path = bootstrapInfo.getProjectsRoot();
			if (path.startsWith(RuntimeConstants.CONFIG_FILE_NAME_TOKEN)) {
				path = path.replace(RuntimeConstants.CONFIG_FILE_NAME_TOKEN,
						new File(this.bootstrapConfigFile).getParent());
				bootstrapInfo.setProjectsRoot(new File(path).getCanonicalPath());
			}

			this.container = bootstrapInfo.getContainer();
			setPlatformVariablesFromConfig(bootstrapInfo);
			
			// export the RuntimeConstants.TRPR_APP_NAME property, if set, to System properties for use in JMX export
			try {
				System.setProperty(RuntimeConstants.TRPR_APP_NAME,RuntimeVariables.getVariable(RuntimeConstants.TRPR_APP_NAME));
			} catch (Exception e) {
				// Catch and consume this Exception. Only impact is on JMX binding name as exported by BootstrapModelMBeanExporter
			}

			// initialize the container
			this.container.init();

		} catch (Exception e) {
			// catch all block to consume and minimally log bootstrap errors
			// Log to both LOGGER and System.out.println();
			LOGGER.error("Fatal error in bootstrap sequence. Cannot continue!",	e);
			System.out.println("Fatal error in bootstrap sequence. Cannot continue!");
			e.printStackTrace(System.out);
			return;
		}

		// publish an event that this Bootstrap has started
		publishBootstrapEvent("** Trooper bootstrap complete **", RuntimeConstants.BOOTSTRAP_START_STATE);		
		
		// Log successful start up details
		String ccDisplay = RuntimeVariables.getContainerType() == null ? "None" :  RuntimeVariables.getContainerType();
		final Object[] displayArgs =         {
				RuntimeVariables.getRuntimeNature(),
				ccDisplay,
				(System.currentTimeMillis() - start),
				this.hostName,
        };
		LOGGER.info(STARTUP_DISPLAY.format(displayArgs));
		LOGGER.info("** Trooper Bootstrap complete **");
	}

	/**
	 * Interface method implementation
	 * @see BootstrapManagedBean#stop()
	 */
	public void stop() throws Exception {
		System.out.println("** Trooper runtime Stopping....**");
		if (this.container != null) {
			// publish an event that this Bootstrap is stopping
			publishBootstrapEvent("** Stopping Trooper runtime **", RuntimeConstants.BOOTSTRAP_STOP_STATE);		
			runtimeVariables.clear();
			this.container.destroy();
			this.container = null;
		}
		if (this.containerContext != null) {
			this.containerContext.destroy();
			this.containerContext = null;
		}
		System.out.println("** Trooper runtime stopped! **");
	}
	
	/**
	 * Interface method implementation
	 * @see BootstrapManagedBean#destroy()
	 */
	public void destroy() throws Exception {
		if (this.backgroundThreadState == EXIT) {
			// do nothing if already in EXIT state
			return;
		}
		System.out.println("** Trooper runtime shutdown initiated....**");
		this.stop();
		// notify the background thread to exit
		this.backgroundThreadState = EXIT;
		synchronized(this) {
			notifyAll();
		}	
		// ideally the above code should cause the JVM to exit if only daemon threads are running
		Thread.currentThread().sleep(SLEEP_BEFORE_EXIT);
		System.out.println("** Trooper runtime shutdown! **");
		// finally signal a JVM exit, if threads havent shutdown gracefully
		System.exit(EXIT);
	}
	
	/**
	 * The background Thread's run method. Simply waits until woken up and exits 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		while (true) {
			switch (backgroundThreadState) {
				case WAIT:
					synchronized(this) {
						try {
							wait(); // sleep until woken up
						}catch(InterruptedException ie) {
							// do nothing
						}
					}
					break;
				case EXIT:
					// simply return. The thread will die
					return;
			}
		}
	}
	
	/**
	 * Helper method to initialize the underlying logging system with the config file specified
	 */
	private void configureLoggingFromFile(File loggingConfigFile) {
		try {
			LogFactory.configureLogging(loggingConfigFile);
		} catch (Exception e) {
			System.err.println("Error initializing logging framework!. Error message : " + e.getMessage());
			e.printStackTrace(System.err);
			return;
		}
		// logging is ready now
		LOGGER.info("Logging framework is now initialized!");		
	}

	/**
	 * Populates all the bootstrap variables from the bootstrap configuration
	 */
	private void setPlatformVariablesFromConfig(BootstrapInfo bootstrapInfo) {
		runtimeVariables.setVariable(RuntimeConstants.TRPR_APP_NAME, bootstrapInfo.getApplicationName());
		runtimeVariables.setVariable(RuntimeConstants.PROJECTS_ROOT, bootstrapInfo.getProjectsRoot());
		runtimeVariables.setVariable(RuntimeConstants.NATURE, bootstrapInfo.getRuntimeNature());
		if (bootstrapInfo.getComponentContainerClassName() != null) {
			runtimeVariables.setVariable(RuntimeConstants.CONTAINER_TYPE, bootstrapInfo.getComponentContainerClassName());
		}
	}
	
	/** Helper method to publish {@link PlatformEvent} for bootstrap life-cycle */
	private void publishBootstrapEvent(String msg, String status){
		PlatformEvent bootstrapEvent=new PlatformEvent();
		bootstrapEvent.setEventMessage(msg);
		bootstrapEvent.setEventStatus(status);
		bootstrapEvent.setCreatedDate(Calendar.getInstance());
		bootstrapEvent.setEventSource(this.getClass().getName());
		bootstrapEvent.setEventType(RuntimeConstants.BOOTSTRAPMONITOREDEVENT);
		bootstrapEvent.setHostName(this.hostName);
		// pass it on to the container to publish it
		this.container.publishBootstrapEvent(bootstrapEvent);
	}
	
}
