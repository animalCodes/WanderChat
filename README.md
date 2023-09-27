# WanderChat

A simple chat enhancement plugin for the WanderMC minecraft server.

## Features

### Text formatting

Markdown style formatting - surrounding text with single '*'s will cause the text to be made italic, doing the same with two will make it bold.

Links will automatically (if the regex holds up) be detected and made clickable.

### Tagging

If a message contains the username of an online player prefixed with an '@' (example: `@notch`) that player will be "tagged".

Tagging involves playing a sound and sending the receiver an actionbar message notifying them of who did the tagging.

### Ignoring

A pretty standard system for blocking messages from particular players.

Ignoring is done with `/ignore {username}` (ignored player must be online), unignoring is done with `/unignore {username}` and a list of currently ignored players can be viewed by running `/ignore` without any arguments.

You cannot be tagged by a player you are ignoring.

### Tips

Every *x* minutes (configurable, default 15) a random message from the `tips` list in `config.yml` will be broadcast to all online players.

### Announce after set period

Ability to "announce" (broadcast with uniform style) a message to all online players after a given duration has passed, command: `/announce in {duration} {message}`.

Duration format is `*n*h*n*m*n*s` where
- Each *n* is any whole number,
- `h`, `m` and `s` are case-insensitive,
- `h` stands for hours, `m` stands for minutes and `s` stands for seconds,
- each number-unit pair is optional, but at least one must be specified.
(An explanation will be printed if `/announce in` is run on its own)

Scheduled announcements can be cancelled with `/announce cancel`

## Building

As this plugin is made specifically for the WanderMC server, pre-built downloads aren't available.

That said, feel free to clone and download the plugin for your own use, it uses Maven for packaging.

=====

WanderChat is licensed under the GNU General Public License V3.
