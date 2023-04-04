# CyberTPR
A simple plugin for Spigot Minecraft servers that allows players to change the name and lore of their items with commands.

## Features
- Use colour and formatting codes in names and lores
- Show the player a preview of their item before changing its name/lore
- Charge the player a number of XP levels to change their item's name/lore
- Customizable the max name and lore lengths
- Customizable messages

## Commands
### /crename [reload]
Displays information about the plugin, along with a link to this repository.

#### Permissions
- `cyberrename.info` - Allows access to this command. Enabled by default.
- `cyberrename.admin` - Allows the player to use the `reload` subcommand to reload the config.

### /setname [...name]
Aliases: `/rename`

Sets or resets the custom name of the item you're holding. Names can contain formatting codes. Run the command without any arguments to reset the item name.

#### Permissions
- `cyberrename.name` - Allows the player to rename items
- `cyberrename.name.bypassLengthLimit` - Allows the player to bypass the name length limit
- `cyberrename.name.bypassCost` - Allows the player to bypass the rename cost
- `cyberrename.name.color.*` - Allows all colour codes to be used in custom names
    - To allow only certain colours, use the children of `cyberrename.name.color`: `black`, `darkblue`, `darkgreen`, `cyan`, `darkred`, `purple`, `orange`, `gray`, `darkgray`, `blue`, `green`, `aqua`, `red`, `pink`, `yellow`, `white`, and `rgb`
- `cyberrename.name.format.*` - Allows all formatting codes to be used in custom names
    - To allow only certain colours, use the children of `cyberrename.name.format`: `bold`, `strikethrough`, `underline`, `italic`, `magic`, and `reset`

### /setlore [...lore]
Aliases: `/lore`, `/relore`

Sets or removes the lore of the item you're holding. Lores can contain formatting codes. Run the command without any arguments to remove the item's lore.

#### Permissions
- `cyberrename.lore` - Allows the player to change the lore of items
- `cyberrename.lore.bypassLengthLimit` - Allows the player to bypass the lore length limit
- `cyberrename.lore.bypassCost` - Allows the player to bypass the setlore cost
- `cyberrename.lore.color.*` - Allows all colour codes to be used in lores
    - To allow only certain colours, use the children of `cyberrename.name.color`: `black`, `darkblue`, `darkgreen`, `cyan`, `darkred`, `purple`, `orange`, `gray`, `darkgray`, `blue`, `green`, `aqua`, `red`, `pink`, `yellow`, `white`, and `rgb`
- `cyberrename.lore.format.*` - Allows all formatting codes to be used in lores
    - To allow only certain colours, use the children of `cyberrename.name.format`: `bold`, `strikethrough`, `underline`, `italic`, `magic`, and `reset`

## Configuration
See the comments in [config.yml](/src/main/resources/config.yml) to learn about config options.