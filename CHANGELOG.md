## 2.0.3 (Aug 02, 2022)
  - Fix for deprecated method in runtime-core

## 2.0.2 (May 04, 2017)
  - Using File.separator in JettyWebAppContextFactory for being OS agnostic
  
## 2.0.1 (Oct 14, 2016)
  - Removed bottleneck in PlatformEventMultiCaster
  
## 2.0.0 (May 20, 2016)
  - Removed logback as packaged logging implementation. Uses only the slf4j logging facade. Users of Trooper, including Trooper profiles, now provide their own logging binding
  - JDK version is 1.8
  - Changes to all profile dependencies due to above logging change

## 1.3.4 (Feb 26, 2016)
  - Support for multiple component containers

## 1.3.3 (Sep 01, 2015)
  - Upgrade for Spring 4.2.0.RELEASE version
  - Upgrade to other libraries to be API compatible with Spring upgrade
  - Code fixes post upgrade

## 1.3.3-SNAPSHOT (Jun 01, 2015)
  - Added support to change admin port for batch console. Helps to run multiple Trooper batch instances on same machine
  - Corrected JSON formatting in services console

## 1.3.2 (May 20, 2015)
  - Upgrading snapshot release to full release
  
## 1.3.2-SNAPSHOT (Dec 09, 2014)
  - Added support for chaining batch jobs
  - Job executions are sorted by recency of execution 
  - Enhancement to use pre-created queues in RabbitMQ Integration and Mule transport modules
  - New HTablePool implementation that validates connections periodically
  - Fix to Trooper batch for pagination (issue #34) and alphabetical sorting (issue #35)
  - Fix to Trooper batch admin page performance issue (issue #36)
  - Fix in FileLocator to use absolute path of files if specified
  - Fix in JobInfo to make use of date formatter thread safe
  - Fix in Bootstrap to handle failed init of component container

## 1.3.1 (Jan 5, 2014)
  - Bug fix for invoking Metrics Timer in service profile

## 1.3.0 (Nov 14, 2013)
  - Upgrade of all modules to use Spring 3.2.5.RELEASE
  - Enhancements/fixes to modules to make them compatible with Spring 3.2.5.RELEASE version

## 1.2.9 (Nov 5, 2013)
  - Bug fix for preventing concurrent job execution in batch profile
  - Bug fix in Rabbit consumer for queue with zero messages
  - Bug fix to prevent duplicate loading of services in service profile

## 1.2.6 (July 23, 2013)
  - Modified POM files to make build versions explicit
  - Bug fixes. Notably race condition in batch JMX administrator

## 1.2.5 (July 08, 2013)
  - Added ability to re-init Batch jobs and Services. Available using REST endpoints
  - Added support to renew TGT tickets for Kerberos secured CDH4 client access libraries i.e. dataaccess-hbase
  - Added support to connect to live RabbitMQ nodes in a cluster, node affinity
  - Bug fixes. Notably Jetty console display settings for batch-core

## 1.2.4 (June 19, 2013)
  - Forced System.exit() in bootstrap destroy
  - Warm up HTable instances during startup for faster read/write to HBase 
  - Bugfixing in dataacess-hbase
  - Ignore errors in case properties file is not found (and apply defaults)

## 1.2.3 (June 3, 2013)
- **New features**
  - Support for Kerberos authentication in CDH4
  - Bumped up hibarnate version to 3.2.3-ga
  - Added requestRate as a metric in Service Framework
  - Added support for loading configurations from Trooper config locations
  - Bugfixing for Batch HA mode, Zookeeper path is created if it doesn't exist
  - Minor bugfixing in ServiceFramework, BootstrapLauncher
  - Configuration console for Service Framework core 
  - Moved to Jackson for JSON marshalling/unmarshlling 

## 1.2.2 (March 14, 2013)
- **New features**
  - CodaHale metrics integration for better Metrics gathering. New metrics include: P50,P75,P99,P99.9 Response Times, 1-min, 5-min, 15-min rate, etc.
  - Added Notification support for metrics. Users can now define custom rules about the metrics and get notifications when any of the rule matches
  - Ability to turn off sync in HA mode
  - Minor bugfixing: Next fire time not updated in Batch profile [FIXED]

- **Docs changes:**  
  - https://github.com/regunathb/Trooper/wiki/Writing-Services-in-Trooper [Info on how to use the Metrics Notifications]
  - https://github.com/regunathb/Trooper/wiki/Trooper-Services-Web-Console [Added images to show the new console]

## 1.2.1 (March 6, 2013)
- **New features**
  - Server mode for serviceframework, that can be used to view metrics and test the services
  - Custom property configurer, that can be used to configure bean properties
  - Optimization of Trooper admin (batch server mode), making it much faster when number of executions are high
  - Added ability to limit the number of jobInstances for which data has to be stored in memory (by Trooper Admin)
  - Minor bugfixings
  - Added ability to ignore errors, if job sync doesn't happen due to permissions error (HA mode)

## 1.1.0 (February 12, 2013)
- **New features in Batch profile:**
  - Cron expression and next fire time visible on jobs page
  - Dynamic deployment of jobs. (Jobs can be deployed on the fly)
  - Added job configuration UI for modification of existing jobs and deployment of new jobs
  - Support for uploading dependency JARs with job configuration
  - Added API for pushing and pulling jobs from one Job Host to the other.
  - Clustered mode (High Availability wrapper) now supports automatic synchronization of job Hosts.
  - In Clustered (HA) mode, job page shows all the deployed job hosts, and their consoles are accessible 

- **Docs changes:**  
  - https://github.com/regunathb/Trooper/wiki/Writing-Batch-jobs-in-Trooper [Info on how to use the Job configuration console]
  - https://github.com/regunathb/Trooper/wiki/Trooper-Batch-Web-Console [Changed images to show the new console]
  - https://github.com/regunathb/Trooper/wiki/Useful-Batch-Libraries [Information on synchronization]
