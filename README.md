# Trooper

Trooper is a Java module like framework that provides various runtime profiles for building applications. Batch, Service and Orchestration runtime profiles are currently supported.
It is an umbrella project for a number of things:

* Build Service Oriented applications that can be distributed and scaled.
* Create a Java module-like system to build runtime profiles that applications can choose from : Basic, Service, Orchestration, Batch etc.
* Implement a number of patterns suited for scalability and deployment on commodity hardware. E.g. sharding, statelessness, data locality, fail-fast, checkpointing and recovery.
* Sub-projects that may be used totally independent of Trooper. E.g. the "mule-transport-rabbitmq" is a Maven project providing a RabbitMQ transport for Mule.
* Provide data models suited for service interactions, event driven design and metrics collection

## Releases

| Release | Date | Description |
|:------------|:----------------|:------------|
| Version 2.0.3    | Aug 2022       |     Fix for deprecated method in runtime-core
| Version 2.0.2    | May 2017       |     Bug fix for Jetty loading in non-Unix OS
| Version 2.0.1    | Oct 2016       |     Removed bottleneck in PlatformEventMultiCaster
| Version 2.0.0    | May 2016       |     Jdk 1.8, Removed logback as default. Uses only slf4j
| Version 1.3.4    | Feb 2016       |     Support for multiple component containers

## Changelog

Changelog can be viewed in CHANGELOG.md file (https://github.com/regunathb/Trooper/blob/master/CHANGELOG.md)

## Documentation and Examples

The Trooper "examples" project group demonstrates usage of various application profiles supported by Trooper.
Documentation is continuously being added to the Wiki page of Trooper (https://github.com/regunathb/Trooper/wiki)

## Getting help

For discussion, help regarding usage, or receiving important announcements, subscribe to Trooper mailing list: http://groups.google.com/group/trooper-users

## License

Trooper is licensed under : The Apache Software License, Version 2.0. Here is a copy of the license (http://www.apache.org/licenses/LICENSE-2.0.txt)

## Project lead

* Regunath B ([@regunathb](http://twitter.com/RegunathB))

## Core contributors

* Shashikant Soni ([@shashiks](https://github.com/shashiks))
* Srikanth PS ([@srikanthps](http://twitter.com/srikanthps))
* Devashish Shankar ([@devashishshankar](https://github.com/devashishshankar))
* Jagadeesh Huliyar ([@jagadeesh-huliyar](https://github.com/jagadeesh-huliyar))

## Trooper users

( _Write to us if you are a Trooper user and would like to be mentioned here_ )

* [Flipkart](http://www.flipkart.com) - large eCommerce portal in India
  * Catalog update propagation to website uses Trooper Batch and Orchestration profiles
  * Notification platform uses Trooper Batch and Orchestration profiles
  * Review Summarization processing pipeline is built using [Sift](https://github.com/regunathb/Sift) and Trooper Batch
  * [Phantom](https://github.com/Flipkart/phantom) (Service Proxy) uses Trooper Basic profile and HBase persistence libraries. Mobile API platform is built on Service Proxy.

*  R&D department of a big company providing national electricity in France
  * Uses Trooper Batch profile in an application that helps researchers to do theirs statistics studies. It is used to import new 
data daily from many sources.  
