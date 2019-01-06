# XPages Runtime

This project is an experiment in loading up the XPages runtime from Domino 10.0.1 outside of Domino - specifically, in Open Liberty, but there's nothing in here tied to that container.

Since XPages does not inherently require OSGi, this project doesn't bother initializing an OSGi runtime (though some Eclipse dependencies are brought along for the ride).

It has the ability to run XPage classes inside the project (such as the example `xsp.LibertyTest`) using the direct servlet runtime, as well as loading XPages applications from inside an NSF in the active Notes/Domino environment's data directory (see below). In the latter case, the XPages applications still see themselves as running "in Domino" with Servlet 2.4, due to the way the inner runtime works.

## Building

To build this project, you must have a Mavenized version of the XPages runtime from Domino 10.0.1, as created by using the "XSP Artifacts" preferences pane in [Darwino Studio](https://www.darwino.com).

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