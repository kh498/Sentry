# Sentry

This is a heavliy modified version of the original Sentry. All support for the external plugins have been removed and it supports only MC 1.8.x. However the code have been refractored so it is no long as trash as it once was, but it is still very much in active development. The version provided by maven might change even though the version number stay the same, and I apologize for this. But then again who would use this fork anyway? 



## Maven 

```
<repository>
    <id>Sentry</id>
    <url>https://raw.github.com/kh498/Sentry/mvn-repo/</url>
    <snapshots>
        <enabled>true</enabled>
        <updatePolicy>always</updatePolicy>
    </snapshots>
</repository>
```

```
<dependency>
    <groupId>net.aufdemrand</groupId>
    <artifactId>sentry</artifactId>
    <version>2.0.0</version>
</dependency>
```
