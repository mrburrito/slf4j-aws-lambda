# slf4j-aws-lambda

This project is no longer necessary and will not be maintained. The code is available in the `legacy` branch, but Amazon released a library, `aws-lambda-java-log4j`, supporting log4j in Lambda functions. This library, in combination with the `slf4j-log4j12` adapter serves the original purpose of this project.

## Gradle

Add the following dependencies to your `build.gradle` for your Lambda functions. (Versions as of Oct 25, 2016)

```
    runtime 'com.amazonaws:aws-lambda-java-log4j:1.0.0'
    runtime 'org.slf4j:slf4j-log4j12:1.7.21'
```

## Maven

Add the following dependencies to your `pom.xml` for your Lambda functions. (Versions as of Oct 25, 2016)

```
    <dependency>
        <groupId>com.amazonaws</groupId>
        <artifactId>aws-lambda-java-log4j</artifactId>
        <version>1.0.0</version>
    </dependency>
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-log4j12</artifactId>
        <version>1.7.21</version>
    </dependency>
```
