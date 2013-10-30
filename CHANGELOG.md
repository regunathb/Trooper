## 1.2.8 (Oct 27, 2013)
  - Bug fix for preventing concurrent job execution in batch profile
  - Bug fix in Rabbit consumer for queue with zero messages
<br />  

## 1.2.6 (July 23, 2013)
  - Modified POM files to make build versions explicit
  - Bug fixes. Notably race condition in batch JMX administrator
<br />  

## 1.2.5 (July 08, 2013)
  - Added ability to re-init Batch jobs and Services. Available using REST endpoints
  - Added support to renew TGT tickets for Kerberos secured CDH4 client access libraries i.e. dataaccess-hbase
  - Added support to connect to live RabbitMQ nodes in a cluster, node affinity
  - Bug fixes. Notably Jetty console display settings for batch-core
<br />  

## 1.2.4 (June 19, 2013)
  - Forced System.exit() in bootstrap destroy
  - Warm up HTable instances during startup for faster read/write to HBase 
  - Bugfixing in dataacess-hbase
  - Ignore errors in case properties file is not found (and apply defaults)
<br />

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
<br />

## 1.2.2 (March 14, 2013)
- **New features**
  - CodaHale metrics integration for better Metrics gathering. New metrics include: P50,P75,P99,P99.9 Response Times, 1-min, 5-min, 15-min rate, etc.
  - Added Notification support for metrics. Users can now define custom rules about the metrics and get notifications when any of the rule matches
  - Ability to turn off sync in HA mode
  - Minor bugfixing: Next fire time not updated in Batch profile [FIXED]
<br />
- **Docs changes:**  
  - https://github.com/regunathb/Trooper/wiki/Writing-Services-in-Trooper [Info on how to use the Metrics Notifications]
  - https://github.com/regunathb/Trooper/wiki/Trooper-Services-Web-Console [Added images to show the new console]
<br />

## 1.2.1 (March 6, 2013)
- **New features**
  - Server mode for serviceframework, that can be used to view metrics and test the services
  - Custom property configurer, that can be used to configure bean properties
  - Optimization of Trooper admin (batch server mode), making it much faster when number of executions are high
  - Added ability to limit the number of jobInstances for which data has to be stored in memory (by Trooper Admin)
  - Minor bugfixings
  - Added ability to ignore errors, if job sync doesn't happen due to permissions error (HA mode)
<br />

## 1.1.0 (February 12, 2013)
- **New features in Batch profile:**
  - Cron expression and next fire time visible on jobs page
  - Dynamic deployment of jobs. (Jobs can be deployed on the fly)
  - Added job configuration UI for modification of existing jobs and deployment of new jobs
  - Support for uploading dependency JARs with job configuration
  - Added API for pushing and pulling jobs from one Job Host to the other.
  - Clustered mode (High Availability wrapper) now supports automatic synchronization of job Hosts.
  - In Clustered (HA) mode, job page shows all the deployed job hosts, and their consoles are accessible 
<br />
- **Docs changes:**  
  - https://github.com/regunathb/Trooper/wiki/Writing-Batch-jobs-in-Trooper [Info on how to use the Job configuration console]
  - https://github.com/regunathb/Trooper/wiki/Trooper-Batch-Web-Console [Changed images to show the new console]
  - https://github.com/regunathb/Trooper/wiki/Useful-Batch-Libraries [Information on synchronization]
