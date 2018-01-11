# kafka-encryption lib

 [![Build Status](https://travis-ci.org/idealo/lib-kafka-encryption.svg?branch=master)](https://travis-ci.org/idealo/lib-kafka-encryption)
 [![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.idealo.kafka/kafka-encryption/badge.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22de.idealo.kafka%22) 

## description
This is a lightweight library for symmetric encryption when using kafka. It is implemented by replacing the default value (de)serializer.

Inspired by https://blog.codecentric.de/en/2016/10/transparent-end-end-security-apache-kafka-part-1/

## usage

```xml
 <dependency>
    <groupId>de.idealo.kafka</groupId>
    <artifactId>spring-boot-starter-kafka-encryption</artifactId>
    <version>LATEST</version>
</dependency>
```

application.yml
```yaml
spring:
  kafka-encryption:
#     enabled: false (default:true)
     topic-passwords:
        test-topic: password
```

output when missed config
``` output

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v1.5.9.RELEASE)

FATAL: KAFKA-ENCRYPTION no password for encryption configured
```


# development

## build + tests
```
mvn clean verify
```

## structure

### internal
- crypto-spring (symmetrics cryptography based on spring-security-core)
- kafka-dependency-config (pinned version dependencies of kafka)

### lib
- kafka-encryption (plain lib, value encrypt serializer + value decrypt deserializer)
- spring-boot-starter-kafka-encryption (lib + spring-boot autoconfiguration)

### demonstration
- spring-boot-demo-app (spring-boot demo app)
