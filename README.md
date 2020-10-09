## DiscordSupportBot v1.2 - Updated to latest JDA version
### Changes:
- Updated to JDA 4.2.0
- Made server setup config stuff hardcoded because it broke and IDK how to fix and for a bot only on one server, IDC about that too much. Running "..." will just use the hardcoded defaults in listeners/DiscordSetupListener.java line 45+
- Hackily (is that a word?) add near 2k limit message handling (either cutting it short by 100-250 characters or send the 2k message by itself + another description)
- Updated a couple of dependencies
- Probably made the code less efficient. Sorry, I am not a real dev, just a person who knows *some* basic java and tried to patch the program.
### Build Instructions
1. Install java 8
2. Set Java Home to java 8 if `java -version` isn't already 8
3. `mvn clean install`
### Other info
- No guarentee on anything working. Again, I am not a bot dev, I am just trying to keep my personal bot running since I haven't found any other good replacements.
- You can join the official support bot discord at https://discord.gg/DDamWUd (not managed by me)
- Run the bot `java -jar JARFILE.jar BOTTOKEN`. If you do not want to compile, github actions *should* be set up to automatically build the repo.
- There is no public version of this bot since a) I'm poor, b) the config system is broken and is hardcoded, and c) the bot tends to sometimes break and I'm not restarting the bot every 20 seconds to fix it. A publically hosted version of the upstream repo can be found using https://discord.com/oauth2/authorize?client_id=324388172312346624&scope=bot&permissions=8 (though the bot may stop working at any time as it isn't updated) and the incomplete version 3 of this bot by scarsz can be found at https://github.com/DiscordSupportBot/DiscordSupportBot.v3.
