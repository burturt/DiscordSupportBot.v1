package github.scarsz.discordsupportbot.listeners;

import github.scarsz.discordsupportbot.DiscordSupportBot;
import github.scarsz.discordsupportbot.GuildInfo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;
import java.util.stream.Collectors;

public class DiscordSupportTicketCreationListener extends ListenerAdapter {

    private final String MESSAGE_TEMPLATE = "**__Author:__** {AUTHOR}\n" +
            "**__Message:__** {MESSAGE}\n" +
            "\n" +
            "Admin ping: <@&609582712898060299>\n*An admin will follow up with your request soon, and if none respond within 48 hours, feel free to ping the admin role.*\n*If you haven't already, please change your nickname to **something resembling your real name**. Thank you!*\n*To close this ticket, someone with the {CLOSERS} role needs to react to this message. Doing so will mark the request as solved.*";

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (event.getMember() == null) return;
        if (event.getMember().isOwner()) return;
        if (event.getAuthor().isBot()) return;
        if (!DiscordSupportBot.isSetUp(event.getGuild())) return;

        GuildInfo guildInfo = DiscordSupportBot.getGuildInfo(event.getGuild());

        TextChannel supportChannel = guildInfo.getFirstMessageChannel();

        if (!event.getChannel().equals(supportChannel)) return;

        if (guildInfo.getMaxOpenTickets() > 0 && guildInfo.getActiveTickets().size() >= guildInfo.getMaxOpenTickets()) return;

        List<TextChannel> possiblePreExistingTicketChannels = event.getGuild().getTextChannelsByName(event.getChannel().getName() + "-" + event.getAuthor().getId(), true);
        if (possiblePreExistingTicketChannels.size() > 0) {
            try {
            possiblePreExistingTicketChannels.get(0).sendMessage(event.getAuthor().getAsMention() + ", please send your messages to this channel. Use this channel to continue discussing your join request.\n```\n" + event.getMessage().getContentRaw() + "\n```").queue();
            } catch (Exception e) {
                possiblePreExistingTicketChannels.get(0).sendMessage(event.getAuthor().getAsMention() + ", please send your messages to this channel. Use this channel to continue discussing your join request.\n```\n" + event.getMessage().getContentRaw().substring(0, event.getMessage().getContentRaw().length() - 250) + "\n```").queue();
            }
            event.getMessage().delete().queue();
            return;
        }

        handleNewTicket(event);
    }

    private void handleNewTicket(GuildMessageReceivedEvent event) {
        GuildInfo guildInfo = DiscordSupportBot.getGuildInfo(event.getGuild());

        // create the new channel, inside of a category if possible
        String channelName = event.getChannel().getName() + "-" + event.getAuthor().getId();
        TextChannel newChannel;
        if (event.getChannel().getParent() == null) {
            newChannel = (TextChannel) event.getGuild().createTextChannel(channelName).complete();
        } else {
            newChannel = (TextChannel) event.getChannel().getParent().createTextChannel(channelName).complete();
        }
        event.getGuild().modifyTextChannelPositions().selectPosition(newChannel).moveTo(event.getChannel().getPosition() + 1).queue();

        // make the author forcefully have message read/write permission
        newChannel.createPermissionOverride(event.getMember()).setAllow(Permission.MESSAGE_READ, Permission.MESSAGE_WRITE).queue();

        try {
            newChannel.sendMessage(MESSAGE_TEMPLATE
                    .replace("{AUTHOR}", event.getAuthor().getAsMention())
                    .replace("{MESSAGE}", event.getMessage().getContentRaw())
                    .replace("{CLOSERS}", "`" + String.join(", ", guildInfo.getRolesAllowedToCloseTickets().stream().map(s -> event.getGuild().getRoleById(s).getName()).collect(Collectors.toList())) + "`")
            ).queue(message -> message.addReaction(guildInfo.getDefaultReactionEmoji()).queue());
        } catch (Exception e) {
            newChannel.sendMessage(event.getMessage().getContentRaw()).queue(message -> message.addReaction(guildInfo.getDefaultReactionEmoji()).queue());
            newChannel.sendMessage(MESSAGE_TEMPLATE
                    .replace("{AUTHOR}", event.getAuthor().getAsMention())
                    .replace("{MESSAGE}", "See above message")
                    .replace("{CLOSERS}", "`" + String.join(", ", guildInfo.getRolesAllowedToCloseTickets().stream().map(s -> event.getGuild().getRoleById(s).getName()).collect(Collectors.toList())) + "`")
            ).queue();
        }

        event.getMessage().delete().queue();
    }

}
