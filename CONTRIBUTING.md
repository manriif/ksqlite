# Contributing

Bug reports and pull requests are welcome. For anything beyond a small fix, opening an issue
first to discuss the approach saves everyone's time.

There's no formal style guide yet, match the surrounding code and the patterns already used in
whichever module you're touching.

Pull requests that do not include a verifiable problem statement or human-validated changes may be 
closed without review.

## Building

### Komple

[Komple](https://github.com/manriif/komple) compiles SQLite for this project, and this project
is in fact where Komple came from. Understanding Komple helps make sense of the build scripts
here.

Besides `make` on Unix, the strict minimum Komple itself needs and likely already on your
machine, no toolchain needs installing by hand. Komple handles the rest.

### IDE

This project can't be opened in IntelliJ IDEA (as of writing), its Android Gradle Plugin version is 
too recent for IJ's bundled support. Android Studio is required instead. AS also supports C and C++,
which matters here given how much of this project touches native code. I haven't tried other IDEs.

I personally run the latest Android Studio nightly build.

### Hardware

Ksqlite development started on an Intel Mac and moved to Apple Silicon since late March. Gradle
sync should succeed on either. I don't own a Windows or Linux machine, so tasks aren't set up to
skip themselves when the host can't run them, meaning I can't guarantee a working Gradle sync on
those OSes.

On macOS, cross-compiling to every target should work:

- The Android NDK compiles Android Native targets
- Xcode compiles Apple targets, provided it's up to date with every needed target SDK installed
- Zig cc compiles Windows and Linux targets, both JVM and native

> [!NOTE]
> Apple targets can't be compiled on a machine that isn't licensed for Xcode.

Komple installs every required tool locally. Between that and the number of supported Kotlin
targets, expect to need at least **12 GB** of disk space when cross-compiling from macOS, an SSD
and good bandwidth recommended. Tools are downloaded and installed once, and survive Gradle
build cache invalidation.

## Testing

There is nothing special to note here. The tests should be run like standard Kotlin tests.