# XPages Runtime

This project allows for running XPages applications outside of Domino. It has been specifically used in Open Liberty, but there's nothing in here tied to that container.

Since XPages does not inherently require OSGi, this project doesn't bother initializing an OSGi runtime (though some Eclipse dependencies are brought along for the ride).

## Building and Using

To build this project or use it as a dependency, you must have a property in your Maven ~/.m2/settings.xml named `notes-platform` and containing a URL to a Domino Update Site for Domino 10+. For example:

```xml
<profiles>
    <profile>
        <id>notes</id>
        <properties>
            <notes-platform>file:///Users/jesse/Java/Domino10.0.1</notes-platform>
        </properties>
    </profile>
</profile>
<activeProfiles>
    <activeProfile>notes</activeProfile>
</activeProfiles>
```

Such an update site can be built from a Notes or Domino installation using the [`generate-domino-update-site` Maven plugin](https://github.com/OpenNTF/generate-domino-update-site).

When using this runtime in your downstream project, that project should also include a repository reference for the Update Site accessed via the [`p2-layout-resolver` Maven plugin](https://github.com/OpenNTF/p2-layout-provider), configured with the ID `com.hcl.xsp.repo`.

Additionally, this project expects to find the `xsp.http.bootstrap.jar` file from a Domino or Designer installation installed to your Maven repo with the group ID `com.hcl.xsp`, artifact ID `xsp.http.bootstrap`, and a version of at least 10.0.1. This can be installed from the command line via something like:

```
mvn install:install-file -Dfile=/Volumes/Windows-Host/Notes/jvm/lib/ext/xsp.http.bootstrap.jar -DgroupId=com.hcl.xsp -DartifactId=xsp.http.bootstrap -Dversion=11.0.0 -Dpackaging=jar
```

## Running

The servlet container running the app must have access to a local Notes or Domino environment. On Windows, this is taken care of by the registry when installing the apps. On Linux or macOS, this can be accomplished by setting appropriate environment variables, such as by adding this to a `server.env` file in Open Liberty:

```
Notes_ExecDirectory=/Applications/IBM Notes.app/Contents/MacOS
LD_LIBRARY_PATH=/Applications/IBM Notes.app/Contents/MacOS
DYLD_LIBRARY_PATH=/Applications/IBM Notes.app/Contents/MacOS
```

Notes or Domino do not need to be running - indeed, it's probably best if they're not.

## Web App Layout

Apps built using this runtime should be structured generally like a [normal web app](https://www.mkyong.com/maven/how-to-create-a-web-application-project-with-maven/), with some important notes about the translation from an NSF-based XPages application to a web app:

* Themes should go in `WEB-INF/themes`
* XPages should go in `WEB-INF/xpages`
* Custom controls should go in `WEB-INF/controls`
* The faces-config file should be `WEB-INF/faces-config.xml`
* The XSP properties file should be `WEB-INF/xsp.properties`
* Unlike in an NSF, images, JavaScript, CSS, and other resources should all go in the content root (`src/main/webapp` in Maven layout) and have no inherent folder structure

## Limitations and Expectations

* There is no context database by default. If you wish to have a default `database` and `session*` variables, they'll have to be created and managed using a variable resolver or other mechanism.
* Similarly, it's best to avoid Domino-specific components in general, such as `xp:dominoDocument` and `xp:dominoView`. Though they can be made to work, it's asking for trouble.
* All extensions (such as `XspLibrary` implementations) must be declared using the `META-INF/services` method and not `plugin.xml`. An extension of type `com.ibm.commons.Extension` can be directly translated by taking the `type` and making a file of that name in the services directory, containing a list of newline-separated class names.
* Similarly, this runtime will not recognize servlets declared via the [Equinox servlet extension point](https://www.eclipse.org/equinox/server/http_in_equinox.php) and must be registered either using the `@WebServlet` annotation or in the app's web.xml.
* Any OSGi Activator classes will not be activated by default, and must instead be explicitly registered. This can be done by registering a service class to `org.openntf.xpages.runtime.osgi.ActivatorNameProvider` implementing the interface of the same name and returning a list of Activator classes to instantiate. These classes are expected to have a public static property named `instance` that will be set to the created instance.
* Unlike an NSF-hosted XPages application, these apps are not running inside a `com.ibm.designer.runtime.domino.adapter.ComponentModule` and so not all normal capabilities are available.
* The XPages servlet is mapped to "*" to allow for URLs like `/foo.xsp/bar/baz`.

## License

The code in the project is licensed under the Apache License 2.0. The dependencies in the binary distribution are licensed under IBM or HCL's Domino license to which you must have agreed when building or using this application.
