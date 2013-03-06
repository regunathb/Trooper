## 1.2.0 (March 6, 2013)
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
