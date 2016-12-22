# le-crap

A patching program for the Len'en Project currently designed to make the translation files easier to read.

## Usage

NOTE: When running the program, it is recommended to do so through the console, using "java -jar le-crap.jar". This isn't necessary, but if you don't do this you won't get any messages.

The first time the program is run, it will generate a new configuration file called "config.edn". If you wish to change the paths the program reads and writes to, do so here. The "data-path" is where le-crap looks for translation files (the files the game uses). The "script-path" is where it places the script files that are generated. Edit these two as necessary before running the program again.

When the program is run a second time, it looks through the data files, and generates some script files from those. It performs this process anytime script files are missing, replacing any existing scripts, so PLEASE BE SURE TO BACK UP THE SCRIPT DIRECTORY.

Finally, if the program is run while all of the script files are present, it will move the script contents to the game files.

## Downloads

In the "releases" tab on the Github page.

## Changelog

Version 0.3.0 - Completely repurposed the program. Wiki stuff has been removed, and currently it simply makes the translation files easier to read.

Version 0.2.0 - Added support for Fumikado's Scenario. Added better system for when the application can't connect to the wiki. Added support for asynchronous wiki lookups, should hopefully speed up the download speed. Added support for the text encoding used in the latest alpha of BPoHC (0.02b), UPDATE YOUR GAME.

Version 0.1.0 - Initial release, supports Yabusame's scenario in BPoHC.

## License

Copyright © 2015 Ryan Haney (Yoshiquest)

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
