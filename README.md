# ninstall

A toolkit to build installers, un-installers and updaters. Generate your native self extracting installers using Java or any other JVM based language.

## Status

 * Immature, but can build moderately complex installers, updaters and uninstallers.
 * Current focus is entirely on Linux, with Windows and Mac OS support coming soon.

## Getting Started

There aren't any docs to help you out just yet, but the general process is ...

 * Create a new Java project
 * Add `ninstall-lib` and probably (`ninstall-swt` and  `ninstall-linux`) to your project.
 * Create a new Java application like whats in [example1](example1/src/main/java/test1/Installer)
 * Run your Java installer builder application.
 * Look in `target`, there will be an executable installer.

## Installation

Available on Maven Central, so just add the following dependency to your project's `pom.xml`.

```xml
<dependency>
    <groupId>uk.co.bithatch</groupId>
    <artifactId>ninstall-lib</artifactId>
    <version>0.0.1</version>
</dependency>
```


### SNAPSHOT versions

or `SNAPSHOT` versions are available right now from the Maven Snapshots repository.


```xml
    <repository>
        <id>central-snapshots</id>
        <url>https://central.sonatype.com/repository/maven-snapshots</url>
        <snapshots/>
        <releases>
            <enabled>false</enabled>
        </releases>
    </repository>
```

and 

```xml
<dependency>
    <groupId>uk.co.bithatch</groupId>
    <artifactId>ninstall-lib</artifactId>
    <version>0.0.2-SNAPSHOT</version>
</dependency>
```