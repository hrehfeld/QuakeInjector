# Quake Injector
Quake Injector is a tool to *download, install and play* Quake (some call it "Quake 1") singleplayer maps from Spirit's "Quaddicted archive":https://www.quaddicted.com - basically the complete history of all quake singleplayer maps ever made. Ever been annoyed by an excruciating installation process? Could not figure out how to install a certain map? Well, this tool makes it a piece of cake. Simply pick a map and click Install.

![Screenshot of the Quake Injector](https://www.quaddicted.com/_media/quakeinjector_20091117.png "Screenshot of the Quake Injector")

## Features
- Simply pick a map, click install and play
- [All Quake singleplayer maps ever made](https://www.quaddicted.com/reviews/)
- Filter, sort, browse to find your favourite map
- Automatically installs dependencies (e.g. Quoth)
- Cross-Platform
- [Free, Open Source Software](https://github.com/hrehfeld/QuakeInjector/), released under the GPL.
- Scan your Quake directory for already installed maps
 

## Installation
Quake Injector requires Quake and Java.

1. Make sure you have the [latest Java version](https://adoptopenjdk.net/) installed
1. Download the latest [Quake Injector release](https://github.com/hrehfeld/QuakeInjector/releases)
1. Extract the .zip file
1. Double click `quakeinjector.jar`, or simply start it with `java -jar quakeinjector.jar` from the extraction directory. On Windows, you can also use the supplied `quakeinjector.bat`.

Do **not** launch it directly from your browser, your settings would be eaten by a [friendly horde of Shoggies](http://www.macguff.fr/goomi/unspeakable/vault299.html), and the settings are vital to the program.

*Keep in mind this software is still in heavy development. The alpha tag hints at it not being feature complete, and possibly buggy.* See the included licence (`COPYING`) for more details on warranties, etc. (there are none).

## Basic Manual
### Filtering, Sorting and the Package Table
Right above the table of available packages you see a filter field. Here you can specify a filter for the listing. Let's say you want to see only maps from 2001? Enter "`2001`" without the quotes. Or for all maps by Vondur, enter "`Vondur`". You can also enter multiple words, they will be connect by a logical AND. So "`czg 2000`" would show you all maps czg made in 2000. To reset the filter simply delete all text from it.

At the top of the package list there are the table headers (duh!). Click on them to sort the table.

This works well in combination with a filter. For example you could look at all maps from 2008 sorted by rating.

## Known Problems
1. On **Linux/MacOS** and other case-sensitive operating systems you won’t have much joy as there is no engine that can ignore the case of filenames *yet*. There will be one and it will be a good one, but for now you're pretty much on your own, as **a lot of maps were packed with mixed case filenames** by the original authors.

1. Also be aware that most engines cannot run some of the latest and greatest maps, because those **maps break the original quake engine limits**. A [modern Quake engine](http://neogeographica.com/site/pages/guides/engines.html#04) is recommended.

## Development
### Bugs and feedback
If anything that feels weird occurs to you, or you find a definite bug, please [report it as an issue](https://github.com/hrehfeld/QuakeInjector/issues). Likewise, give us a shout if you want to help with development. Pull requests are welcome!

### Running from source
To run the application for development, run the following command in the root directory.

On Windows:
```
gradlew.bat run
```
On Unix:
```
./gradlew run
``` 

### Building
To build a jar, grab the source and run the following command in the root directory. The jar will be in the `build/libs` directory.

On Windows:
```
gradlew.bat assemble
```
On Unix:
```bash
./gradlew assemble
```

### Building a Windows EXE
To build an EXE for Windows, grab the source and run the following command in the root directory. It will create a zip in the `build/distributions` directory.

On Windows:
```
gradlew.bat winDist
```
On Unix:
```bash
./gradlew winDist
```

## Credits
- Hauke 'megaman' Rehfeld (initial programming)
- Spirit (initial concept)
- [These lovely people](https://github.com/hrehfeld/QuakeInjector/graphs/contributors) (updates, fixes, features, maintenance) ❤️
