Quake Injector
###

Quake Injector is a tool to *download, install and play* Quake (some call it "Quake 1") singleplayer maps from Spirit's "Quaddicted archive":http://www.quaddicted.com - basically the complete history of all quake singleplayer maps ever made. Ever been annoyed by an excruciating installation process? Could not figure out how to install a certain map? Well, this tool makes it a piece of cake. Simply pick a map and click Install.
 
"[* http://www.quaddicted.com/wp-content/uploads/quakeinjector_20090528_n.png *]":http://www.quaddicted.com/wp-content/uploads/quakeinjector_20090528.png

Features
***
- Simply pick a map, click install and play
- "All Quake singleplayer maps ever made":http://www.quaddicted.com/spmaps.html  (900+) in the database
- Filter, sort, browse to find your favourite map
- Automatically installs dependencies (e.g. Quoth)
- Cross-Platform
- "Free, Open Source Software":http://github.com/hrehfeld/QuakeInjector/, released under the GPL.
- Scan your Quake directory for already installed maps
 

Installation
***

Quake Injector requires "Java 6":http://www.java.com/en/download/ and a Quake installation ("Linux howto":http://tldp.org/HOWTO/Quake-HOWTO-2.html, "modern Fitzquake engine":http://www.celephais.net/fitzquake/).

1) Make sure you have Java 6 installed,
2) Download Quake Injector Alpha 1
3) Extract the .zip file,
4) Double click `quakeinjector.jar`, or simply start it with `java -jar quakeinjector.jar` from the extraction directory. On windows, you can also use the supplied `quakeinjector.bat`.

  Do **not** launch it directly from your browser, your settings would be eaten by a "friendly horde of Shoggies":http://www.macguff.fr/goomi/unspeakable/vault299.html, and the settings are vital to the program.

*Keep in mind this software is still in heavy development. The alpha release suggests it not being feature complete, and possibly buggy.* See the included licence (`COPYING`) for more details on warranties, etc. (there are none).

Known Problems
***

1) On **Linux/MacOS** and other case-sensitive operating systems you won’t have much joy as there is no engine that can ignore the case of filenames *yet*. There will be one and it will be a good one, but for now you're pretty much on your own, as **a lot of maps were packed with mixed case filenames** by the original authors.

2) Also be aware that most engines cannot run some of the latest and greatest maps, because those **maps break the original quake engine limits**. "Fitzquake":http://www.celephais.net/fitzquake/ is highly recommended.

Bug Reports, Help with Development, etc.
***

If anything that feels weird occurs to you, or you find a definite bug, please "report it as an issue":http://github.com/hrehfeld/QuakeInjector/issues or mail Spirit (spixrixt@quaxddixcted.com, remove all xs from that address) or Hauke (hreaaaahfeaaaald@uni-aaakobaalenz.de, remove all as from that address). Likewise, give us a shout if you want to help with development or package management (all the packages need testing).

Manual
***
This will be updated soon with more.

Filtering, Sorting and the Package Table
===

Right above the table of available packages you see a filter field. Here you can specify a filter for the listing. Let's say you want to see only maps from 2001? Enter "2001" (without the quotes). Or for all maps by Vondur, enter "Vondur". You can also enter multiple words, they will be connect by a logical AND. So "czg 2000" would show you all maps czg made in 2000. To reset the filter simply delete all text from it.

At the top of the package list there are the table headers (duh!). Click on them to sort the table.

This works well in combination with a filter. For example all maps from 2008 sorted by rating.

The rightmost column marks the installation status, if there is a check then the package should be installed and ready to play.


Credits
***

Spirit:
    - idea
    - database maintenance
    - hosting
    - feedback & testing

Hauke Rehfeld (megaman in the quake world):
    - programming
