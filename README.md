# PineLib
PineLib is a modern minecraft plugin library designed with simplicity in mind while still offering powerful features.

# Features
## Menus
We have a simple menu system that allows you to create menus with ease. Whether it's a simple chest menu, renaming items with anvils*, displaying recipes with droppers/crafters or even a fully custom UI, PineLib gives you the tools to create it all.

*anvils require [ProtocolLib](https://github.com/dmulloy2/ProtocolLib) installed in order to work.

## Countdowns
We also have countdowns which both support Bukkit and Adventure API. These countdowns will automatically update themselves such as draining boss bars; they can also display information about themselves such as the time remaining using placeholders. (Not PlaceholderAPI)
### Placeholders
Placeholders are a simple template you can use to display information within the countdowns. All placeholders are formatted as `%%{placeholder}%%` and the following placeholders can be used:
- `seconds` - The number of seconds remaining.
- `minutes` - The number of minutes remaining.
- `hours` - The number of hours remaining.
- `percentage` - The percentage of the countdown remaining.
- `progress` - The amount of ticks remaining.
- `raw_progress` - `percentage` but represented as a number between 0 and 1.

# Installation
To utilize PineLib, you need to first download the jar file from the [releases tab](https://github.com/NotPiny/PineLib/releases) or from the [modrinth page](https://modrinth.com/plugin/pinelib/versions). Once you have the jar file, place it in your server's `plugins` folder. Now that we have the library loaded on the server lets include it in our plugin. To do this, you will need to put the jar file in your plugin's `libs` folder. If you don't have a `libs` folder, you can create one. Once you have the jar file in the `libs` folder, you will need to add the following to your `plugin.yml` file:

```yaml
name: PineLibUser
version: '1.0-SNAPSHOT'
main: com.example.pinelibuser.PineLibUser
api-version: '1.21'
prefix: PineLib
authors: [ Piny ]
description: Cool plugin using cool libraries
website: https://example.com
depend:
    - PineLib
```

now to actually use PineLib in your plugin, you will need to add it to your `build.gradle` file. If you don't have a `build.gradle` file, you can create one. Once you have the `build.gradle` file, you will need to add the following to it:

```groovy
dependencies {
    compileOnly files('libs/PineLib-1.1.0-BETA.jar')
}
```

# Usage
Using PineLib in your plugin is simple and designed to feel just as natural as using any code within your own plugin. You don't need to worry about getting the instance of PineLib when you use the features it provides. For information on how to use specific features, please refer to the documentation in the wiki.
