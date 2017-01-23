# dce-plugin-sdk-java

DCE plugin SDK for Java.


## Requirements

- Java 1.8

## Installation

### Install from Maven:

Put this in your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>dce-plugin-sdk</id>
        <url>https://raw.github.com/DaoCloud/dce-plugin-sdk-java/mvn-repo/</url>
        <snapshots>
            <enabled>true</enabled>
            <updatePolicy>always</updatePolicy>
        </snapshots>
    </repository>
</repositories>

<dependency>
  <groupId>io.daocloud.dce</groupId>
  <artifactId>dce-plugin-sdk</artifactId>
  <version>0.1</version>
</dependency>
```

## Example/Usage

### Set/Get Plugin Config

You can use this SDK to save your plugin's config.
**config should not bigger than 1MB.**

```java
#TBD
```

## License
dce-plugin-sdk-java is licensed under the MIT License - see the
[LICENSE](https://github.com/DaoCloud/dce-plugin-sdk-java/blob/master/LICENSE) file for details
