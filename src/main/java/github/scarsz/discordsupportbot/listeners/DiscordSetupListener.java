package github.scarsz.discordsupportbot.listeners;

import github.scarsz.discordsupportbot.DiscordSupportBot;
import github.scarsz.discordsupportbot.DiscordUtil;
import github.scarsz.discordsupportbot.GuildInfo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DiscordSetupListener extends ListenerAdapter {

    private List<String> guildsBeingSetup = new ArrayList<>();

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        GuildInfo guildInfo = DiscordSupportBot.getGuildInfo(event.getGuild());
        if (guildInfo == null || !guildInfo.isSetUp()) {
            Thread setupThread = new Thread(() -> handleSetup(event));
            setupThread.setName("Setup thread - " + event.getGuild().getId() + " by " + event.getAuthor().getId());
            setupThread.start();
        }
    }

    public void handleSetup(GuildMessageReceivedEvent event) {
        if (guildsBeingSetup.contains(event.getGuild().getId())) return;
        if (event.getMember() == null || event.getMember().getUser().isBot() || (!event.getMember().isOwner() && !event.getMember().hasPermission(Permission.ADMINISTRATOR))) return;

        GuildInfo guildInfo = DiscordSupportBot.getGuildInfo(event.getGuild());
        DiscordSupportBot.get().getRegisteredGuilds().remove(guildInfo);

        if ((guildInfo != null && guildInfo.isSetUp()) || !event.getMessage().getContentRaw().startsWith("...")) return;

        System.out.println("Setting up " + event.getGuild());
        guildsBeingSetup.add(event.getGuild().getId());

        boolean pmTranscriptsOnClose = true;
        boolean authorCanCloseTicket = false;
        String defaultReactionEmoji = "🔒";
        int secondsUntilTicketCloses = 15;
        int hoursUntilChannelTimeout = 72;
        int maxOpenTickets = 0;
        String[] rolesAllowedToCloseTickets = new String[0];
        while (rolesAllowedToCloseTickets.length == 0) {
            rolesAllowedToCloseTickets = Arrays.stream("Admin".split(","))
                    .map(s -> DiscordUtil.getRoleByNameFromGuild(event.getGuild(), s))
                    .filter(Objects::nonNull)
                    .map(ISnowflake::getId)
                    .toArray(String[]::new);
        }

        guildInfo = new GuildInfo(pmTranscriptsOnClose, event.getChannel().getId(), defaultReactionEmoji, authorCanCloseTicket, secondsUntilTicketCloses, rolesAllowedToCloseTickets, hoursUntilChannelTimeout, maxOpenTickets, event.getGuild().getId());
        DiscordSupportBot.get().getRegisteredGuilds().add(guildInfo);

        event.getChannel().deleteMessages(event.getChannel().getHistory().retrievePast(100).complete()).complete();
        event.getChannel().sendMessage("This channel is now set up to accept support ticket requests by people sending messages here. Take the time to send the channel's first message instructing users on how to request support (hint: saying messages here). To re-setup your support system, recreate this channel and type `...`. Note that support channels do not get made for you as you're the guild owner so you can send messages here freely.").queue(message -> message.delete().queueAfter(1, TimeUnit.MINUTES));

        guildsBeingSetup.remove(event.getGuild().getId());
    }

}
