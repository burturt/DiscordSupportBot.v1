package github.scarsz.discordsupportbot.listeners;

import github.scarsz.discordsupportbot.DiscordSupportBot;
import github.scarsz.discordsupportbot.GuildInfo;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.RestAction;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class DiscordSupportTicketCloseListener extends ListenerAdapter {

    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
        Thread thread = new Thread(() -> handleTicketClose(event));
        thread.setName("Ticket closure thread - " + event.getChannel().getId() + " by " + event.getUser().getId());
        thread.setUncaughtExceptionHandler((t, e) -> {
            e.printStackTrace();
            t.interrupt();
        });
        thread.start();
    }

    private void handleTicketClose(GuildMessageReactionAddEvent event) {
        if (event.getUser().isBot()) return;
        GuildInfo guildInfo = DiscordSupportBot.getGuildInfo(event.getGuild());
        if (guildInfo == null || !guildInfo.isSetUp()) return;
        String possibleTicketAuthorId = event.getChannel().getName().replace(guildInfo.getFirstMessageChannel().getName() + "-", "");
        if (!StringUtils.isNumeric(possibleTicketAuthorId)) return;
        User ticketAuthor = event.getJDA().getUserById(possibleTicketAuthorId);

        if (event.getReactionEmote().getEmoji().equals("\u2705")) {
            Role role = event.getGuild().getRolesByName("Student", false).get(0);
            event.getGuild().addRoleToMember(possibleTicketAuthorId, role).queue();
            event.getChannel().sendMessage("Join request approved by " + event.getUser().getAsMention() + ". Student role given to " + ticketAuthor.getAsMention() + ".").queue();
        } else {
            event.getChannel().sendMessage("Join request closed without approval by " + event.getUser().getAsMention() + ".").queue();
        }

        boolean allowedToClose = (guildInfo.isAuthorCanCloseTicket() && event.getUser().equals(ticketAuthor)) ||
                event.getMember().getRoles().stream().map(ISnowflake::getId).anyMatch(s -> guildInfo.getRolesAllowedToCloseTickets().contains(s));
        if (!allowedToClose) return;

        // event.getMessageId()
        try {
            MessageHistory history = event.getChannel().getHistory();
            List<Message> retrievedMessages = null;
            while (retrievedMessages == null || retrievedMessages.size() > 0) retrievedMessages = history.retrievePast(100).complete();
            Message firstMessage = history.getRetrievedHistory().get(history.getRetrievedHistory().size() - 1);
            if (!event.getMessageId().equals(firstMessage.getId())) return;
        } catch (Exception e) {
            e.printStackTrace();
        }

        event.getChannel().sendMessage((guildInfo.isPmTranscriptsOnClose() ? "DMing the transcript to all participants " : "") + "in " + guildInfo.getSecondsUntilTicketCloses() + " seconds...").complete();

        try {
            Thread.sleep(guildInfo.getSecondsUntilTicketCloses() * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        MessageHistory history = event.getChannel().getHistory();
        while (history.retrievePast(100).complete().size() > 0);

        List<String> transcriptList = new LinkedList<>();
        for (Message message : history.getRetrievedHistory()) {
            String timeStamp = message.getTimeCreated().getMonth().name() + " " + message.getTimeCreated().getDayOfMonth() + ", " + message.getTimeCreated().getYear() + " " + message.getTimeCreated().getHour() + ":" + message.getTimeCreated().getMinute() + ":" + message.getTimeCreated().getSecond();


            String sendMessageString = "[" + timeStamp + "] " + message.getAuthor() + ": " + message.getContentRaw();
            if (sendMessageString.length() > 1900) {
                sendMessageString = "[" + timeStamp + "] " + message.getAuthor() + ": " + message.getContentRaw().substring(0, message.getContentRaw().length() - 100);
            }


            transcriptList.add(sendMessageString);
        }
        Collections.reverse(transcriptList);
        List<String> transcriptMessages = new LinkedList<>();
        List<String> builtMessageList = new LinkedList<>();
        for (String message : transcriptList) {
            if (builtMessageList.stream().mapToInt(String::length).sum() + builtMessageList.size() + message.length() + 1 > 1992) {
                transcriptMessages.add("```\n" + String.join("\n", builtMessageList) + "\n```");
                builtMessageList.clear();
            }
            builtMessageList.add(message.replace("```", "`"));
        }
        if (builtMessageList.size() > 0) transcriptMessages.add("```\n" + String.join("\n", builtMessageList) + "\n```");

        List<User> usersToMessageTranscriptTo = history.getRetrievedHistory().stream().map(Message::getAuthor).filter(user -> !user.isBot()).distinct().collect(Collectors.toList());
        usersToMessageTranscriptTo.stream().filter(user -> !user.isFake()).map(User::openPrivateChannel).map(RestAction::complete).forEach(privateChannel -> {
            privateChannel.sendMessage("Join request transcript regarding " + ticketAuthor + "'s ticket in " + event.getGuild()).queue();
            for (String transcriptMessage : transcriptMessages) {
                privateChannel.sendMessage(transcriptMessage).queue();
            }
        });

        event.getChannel().delete().queue();
    }

}
