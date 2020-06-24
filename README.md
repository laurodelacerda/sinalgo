Find more information about sinalgo at their [README](https://github.com/Sinalgo/sinalgo#readme)

If you need to export JAVA_HOME, do it beforehand as shown in example.

```
export JAVA_HOME=/your/path/to/java-jdk
```

Running and building Sinalgo is as simple as running:

```
./gradlew run 
```

If you need to pass command line arguments, you can use the following syntax:

```
./gradlew run -PappArgs="['arg1', 'arg2', 'arg3']"
```

And so on. As an example, the following command will run the sample2 project straight from the command line, 
skipping the project selector window.

```
./gradlew run -PappArgs="['-project', 'sample2']"
```

When using Windows, replace ```./gradlew``` with ```gradlew.bat```.

That will downloaded whichever Gradle version Sinalgo needs, build the application and run it. That includes 
all projects.

Gradle includes a ton of functionality. Some plugins being used here provide some nice resources not provided
by Gradle by default.

Right now, the following commands might be useful:

* ```./gradlew build``` will build a zip for distribution plus a jar with every dependency needed to run Sinalgo.

* ```./gradlew javadoc``` will generate a set of HTML documents containing documentation on Sinalgo's classes.
This also includes projects.



