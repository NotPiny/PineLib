# PineLib
PineLib is a modern minecraft plugin library designed with simplicity in mind while still offering powerful features.

# Features
## Menus
We have a simple menu system that allows you to create menus with ease. Whether it's a simple chest menu, renaming items with anvils*, displaying recipes with droppers/crafters or even a fully custom UI, PineLib gives you the tools to create it all.

*anvils require ProtocolLib installed in order to work.

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