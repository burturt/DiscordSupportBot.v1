### DiscordSupportBot v1 Updated
Changes:
- Updated to JDA 4.2.0
- Made server setup config stuff hardcoded. Running "..." will just use the hardcoded defaults in listeners/DiscordSetupListener.java line 45+
- Hackily (is that a word?) add near 2k limit message handling (either cutting it short by 100-250 characters or send the 2k message by itself + another description)
- Updated a couple of dependencies
