#!/usr/bin/env bash
# tag::adocSnippet[]
mkdir -p deployment/src/main/java
mkdir -p deployment/src/main/resources
mkdir -p runtime/src/main/java
mkdir -p runtime/src/main/resources

echo "<project xmlns='http://maven.apache.org/POM/4.0.0' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'
         xsi:schemaLocation='http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd'>
    <modelVersion>4.0.0</modelVersion>

    <groupId>io.quarkus.workshop.super-heroes</groupId>
    <artifactId>extension-banner-parent</artifactId>
    <version>1.0</version>
    <packaging>pom</packaging>
    <name>Quarkus Workshop :: Extensions :: Banner Extension</name>

    <modules>
        <module>runtime</module>
        <module>deployment</module>
    </modules>

     <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>io.quarkus</groupId>
                <artifactId>quarkus-bom</artifactId>
                <version>\${quarkus.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <properties>
        <quarkus.version>1.7.0.Final</quarkus.version>
        <surefire-plugin.version>2.22.0</surefire-plugin.version>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
        <maven.build.timestamp.format>yyyy-MM-dd</maven.build.timestamp.format>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
</project>
" > pom.xml

echo "<project xmlns='http://maven.apache.org/POM/4.0.0' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'
         xsi:schemaLocation='http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd'>
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>io.quarkus.workshop.super-heroes</groupId>
        <artifactId>extension-banner-parent</artifactId>
        <version>1.0</version>
    </parent>

    <artifactId>extension-banner-deployment</artifactId>
    <name>Quarkus Workshop :: Extensions :: Banner Extension :: Deployment</name>

    <dependencies>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-core-deployment</artifactId>
            <version>\${quarkus.version}</version>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-arc-deployment</artifactId>
            <version>\${quarkus.version}</version>
        </dependency>
        <dependency>
            <groupId>io.quarkus.workshop.super-heroes</groupId>
            <artifactId>extension-banner</artifactId>
            <version>\${project.version}</version>
        </dependency>

        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-junit5-internal</artifactId>
            <version>\${quarkus.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
                <configuration>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>io.quarkus</groupId>
                            <artifactId>quarkus-extension-processor</artifactId>
                            <version>\${quarkus.version}</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
            <plugin>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.0.0-M4</version>
                <configuration>
                    <systemProperties>
                        <java.util.logging.manager>org.jboss.logmanager.LogManager</java.util.logging.manager>
                    </systemProperties>
                </configuration>
            </plugin>
        </plugins>
    </build>

</project>" > deployment/pom.xml

echo "<project xmlns='http://maven.apache.org/POM/4.0.0' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'
         xsi:schemaLocation='http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd'>
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>io.quarkus.workshop.super-heroes</groupId>
        <artifactId>extension-banner-parent</artifactId>
        <version>1.0</version>
    </parent>

    <artifactId>extension-banner</artifactId>
    <name>Quarkus Workshop :: Extensions :: Banner Extension :: Runtime</name>

    <dependencies>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-core</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-arc</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>io.quarkus</groupId>
                <artifactId>quarkus-bootstrap-maven-plugin</artifactId>
                <version>\${quarkus.version}</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>extension-descriptor</goal>
                        </goals>
                        <phase>compile</phase>
                        <configuration>
                            <deployment>\${project.groupId}:\${project.artifactId}-deployment:\${project.version}</deployment>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
            <plugin>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
                <configuration>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>io.quarkus</groupId>
                            <artifactId>quarkus-extension-processor</artifactId>
                            <version>\${quarkus.version}</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>" > runtime/pom.xml
# end::adocSnippet[]
