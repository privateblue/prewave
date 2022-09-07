### Building and running the app

The easiest way to build and run the application is to just use 
```
sbt run
``` 

Alternatively, a jar file can be assembled using `sbt assembly`, which then can be run the conventional way:
```
java -jar target/scala-2.13/prewave.jar
```

### How it works

On first run, the app retrieves a list of match terms from the API, and stores them in a local file named `terms`, to save API calls on every subsequent run. This optimization is possible because the output of the API is always the same. To refresh the list of terms, simply delete this file.

The app retrieves alerts from the API, and calculates a list of term IDs per alert, that match the alert. It prints the results to the console, and it also saves these results in a file named `matches`. A `matches` file might e.g. look like
```
i76u5zvferee,501 601
nuzbtju4654r,601
6hgzjkztt4rt4e,302
kw78k6jh45zr,701 801
i6kuttrg45z4,601 901
```
Its line format is a string Alert ID, followed by a comma, and then a list of term IDs separated by spaces.

At every run, this file is first read to make sure that we don't record matches again for an Alert ID that we have already registered here.

This way we get a growing list of historical matches. 

One easy way to test all this is to `tail -f matches` in one terminal, and run the app in another a couple of times.

### Notes

- A missing feature of this implementation is to lock the `matches` file for the duration of a run. This means that currently, running several instances of the app in parallel might result in duplicate Alert IDs in the file.
- Another possible improvement to the code could be representing match results by their own type, instead of just passing a `Map[String, List[Int]]` around.
- Furthermore, the API details should be read from a configuration file, instead of being hardcoded.
- Logging and more systematic error handling are areas that could also benefit from additional work.
- The functionality could be extended - quite easily actually - to poll the API in regular intervals, in a loop, until interrupted.
