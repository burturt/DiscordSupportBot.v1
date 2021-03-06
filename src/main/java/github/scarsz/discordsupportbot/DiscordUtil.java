package github.scarsz.discordsupportbot;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.lang.StringUtils;

public class DiscordUtil {

    public static Object pullEvent(Class<? extends Event> eventClass) {
        final Object[] pulledEvent = {null};

        ListenerAdapter adapter = new ListenerAdapter() {
            public void onGenericEvent(Event event) {
                if (event.getClass().getSimpleName().equals(eventClass.getSimpleName())) {
                    pulledEvent[0] = event;
                }
            }
        };
        DiscordSupportBot.get().getJda().addEventListener(adapter);

        while (pulledEvent[0] == null) try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        DiscordSupportBot.get().getJda().removeEventListener(adapter);
        return pulledEvent[0];
    }

    public static GuildMessageReceivedEvent pullGuildMessageReceivedEvent(User user) {
        while (1 < 2) {
            GuildMessageReceivedEvent pulled = (GuildMessageReceivedEvent) pullEvent(GuildMessageReceivedEvent.class);
            if (pulled.getMember() == null || pulled.getMember().getUser().isBot()) continue;
            if (pulled.getMember().getUser().equals(user)) {
                return pulled;
            }
        }
    }

    public static GuildMessageReactionAddEvent pullGuildMessageReactionAddEvent(User user) {
        while (1 < 2) {
            GuildMessageReactionAddEvent pulled = (GuildMessageReactionAddEvent) pullEvent(GuildMessageReactionAddEvent.class);
            if (pulled.getMember() == null || pulled.getMember().getUser().isBot()) continue;
            if (pulled.getMember().getUser().equals(user)) {
                return pulled;
            }
        }
    }
    public static GuildMessageReactionAddEvent pullGuildMessageReactionAddEvent(Message message, User user) {
        message.addReaction("✅").queue();
        while (1 < 2) {
            GuildMessageReactionAddEvent pulled = (GuildMessageReactionAddEvent) pullEvent(GuildMessageReactionAddEvent.class);
            if (pulled.getMember() == null || pulled.getMember().getUser().isBot()) continue;
            if (pulled.getMember().getUser().equals(user) && pulled.getMessageId().equals(message.getId())) {
                return pulled;
            }
        }
    }

    public static boolean pullYesOrNo(Message messageToListenTo, User author) {
        messageToListenTo.addReaction("✅").queue();
        messageToListenTo.addReaction("❌").queue();

        while (1 < 2) {
            GuildMessageReactionAddEvent pulled = DiscordUtil.pullGuildMessageReactionAddEvent(author);
            if (!pulled.getMessageId().equals(messageToListenTo.getId())) continue;
            String emoji = pulled.getReactionEmote().getName();
            if (!emoji.equals("✅") && !emoji.equals("❌")) continue;
            return emoji.equals("✅");
        }
    }

    public static int pullInteger(TextChannel channel, User user) {
        while (1 < 2) {
            GuildMessageReceivedEvent pulled = DiscordUtil.pullGuildMessageReceivedEvent(user);
            if (StringUtils.isNumeric(pulled.getMessage().getContentRaw())) {
                return Integer.parseInt(pulled.getMessage().getContentRaw());
            } else {
                channel.sendMessage("Your message must be an integer.").queue();
            }
        }
    }

    public static Role getRoleByNameFromGuild(Guild guild, String roleName) {
        for (Role role : guild.getRoles()) {
            if (role.getName().equalsIgnoreCase(roleName)) {
                return role;
            }
        }
        return null;
    }

}
