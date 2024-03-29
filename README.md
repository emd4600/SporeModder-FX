Check the website: https://emd4600.github.io/SporeModder-FX/

SporeModder FX is a tool for designing modifications for the videogame Spore. So far it's the most advanced tool, including features such as:
 - An advanced text editor, with syntax highlighting, error diagnose, autocomplete,...
 - A modern text format to visualize most of the files used by Spore.
 - A "Spore User Interface" editor, that allows you to create and edit the Spore UI.
 - An image and texture viewer that can visualize common images, but also textures used by Spore.
 - A model viewer that can visualize most of the models that are in the Spore format.
 
The tool is designed to have a simple and user-friendly user interface, so making mods for Spore is easier than ever!

If you have any doubt, want to report a bug or you just want to suggest how to improve the program, you can:
 - Create an issue at https://github.com/emd4600/SporeModder-FX/issues
 - Join the Spore Modding discord server, and ask for help or contact me there.

## Development

SporeModder FX uses Maven as its build system. To build it, you can use the following command:
```
mvn -Drevision="2.2.3" clean package
```
The output file will be generated in `shade/sporemodderfx.jar`. Change the revision to the appropriate version number. The version number is read by the program and used to check for updates.

For generating an update, check https://github.com/emd4600/SporeModder-FX-Updater

## Credits
SporeModder FX was programmed in Java 1.8, using the Eclipse tool. Additionally, the following libraries were used:
 - [RichTextFX](https://github.com/FXMisc/RichTextFX) and all its dependencies.
 - [JSPF](https://github.com/gearlles/jspf) (Java Simple Plugin Framework)
 - [JSON-java](https://github.com/stleary/JSON-java)
 - [Java DDS ImageIO Plugin](https://github.com/GoldenGnu/java-dds)
 - [WinMerge](http://winmerge.org/) (not a Java library, but it's included in the public release to compare files)
 - [UnicodeBOMInputStream](https://github.com/gpakosz/UnicodeBOMInputStream)
 - Even though it was not directly used, the custom ribbon developed in this program was heavily based in [FXRibbon](https://github.com/dukke/FXRibbon)
 
Also wanted to give thanks to:
 - [rob55rod/Splitwirez](
https://github.com/Splitwirez), for all his advice that ensured the program didn't end up being a total unusable mess.
 - [@0KepOnline](https://github.com/0KepOnline/), [@A-xesey](https://github.com/A-xesey), [@Rosalie231](https://github.com/Rosalie241) for their contributions, both in code and in discovered internal names of the game.
 - BentLent, Snek, Psi and Darhagonable, for testing the program.
