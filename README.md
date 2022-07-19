# Fokuso

A Fabric Minecraft (1.18) mod for client-side chat filtering using a filter list system to remove spam in chat which is sent on third party servers.
You can add filters with files containing regex patterns, or even implement a `ChatFilter` with your own code.

## Commands

### `/chatfilter list`

Lists all currently loaded filter lists and how many of their filters are enabled.

### `/chatfilter reload`

Loads filter lists from config folder

### `/chatfilter toggle <list> <enabled>`

Enable or disable a specific filter list.

## Regex filter list files

Filter lists can be added by adding files to the `fokuso` config folder with the `.filters` file extension.
Each line is a different regex pattern.
Empty lines and comments (lines starting with `#`) are ignored.
It's possible to use several shortcuts in the regex filters, e.g. `\u` for usernames.

| Shortcut character | Meaning                            |
|:------------------:|------------------------------------|
|         p          | Username prefix                    |
|         u          | Username                           |

```regexp
# Simple filter to remove all messages regarding achieved achievements.
\u has made the advancement \[.*?]
```

If you create any filter lists that might be useful to others, please share them, so that they can be added to this
page.

## Usage

This is intended as a simple tool to hide chat messages when it's not possible or desirable to alter the source.
It's primarily intended to be used to remove chat spam on third party servers but can also be used by modpack creators
to remove chat spam from other mods.