# XPages Runtime

This project is an experiment in loading up the XPages runtime from Domino 10.0.1 outside of Domino - specifically, in Open Liberty, but there's nothing in here tied to that container.

Since XPages does not inherently require OSGi, this project doesn't bother initializing an OSGi runtime (though some Eclipse dependencies are brought along for the ride).

It has the ability to run XPage classes inside the project (such as the example `xsp.LibertyTest`) using the direct servlet runtime, as well as loading XPages applications from inside an NSF in the active Notes/Domino environment's data directory (see below). In the latter case, the XPages applications still see themselves as running "in Domino" with Servlet 2.4, due to the way the inner runtime works.

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

## License

The code in the project is licensed under the Apache License 2.0. The dependencies in the binary distribution are licensed under IBM's license to which you must have agreed when building this application.
