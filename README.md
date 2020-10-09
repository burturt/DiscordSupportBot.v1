### DiscordSupportBot v1 Updated
Changes:
- Updated to JDA 4.2.0
- Made server setup config stuff hardcoded because it broke and IDK how to fix and for a bot only on one server, IDC about that too much. Running "..." will just use the hardcoded defaults in listeners/DiscordSetupListener.java line 45+
- Hackily (is that a word?) add near 2k limit message handling (either cutting it short by 100-250 characters or send the 2k message by itself + another description)
- Updated a couple of dependencies
- Probably made the code less efficient. Sorry, I am not a real dev, just a person who knows *some* basic java and tried to patch the program.
### Build Instructions
1. Install java 8
2. Set Java Home to java 8 if `java -version` isn't already 8
3. `mvn clean install`
