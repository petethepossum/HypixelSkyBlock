package net.swofty.type.skyblockgeneric.event.actions.player;

import net.minestom.server.event.player.PlayerChatEvent;
import net.swofty.commons.ServerType;
import net.swofty.commons.StringUtility;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.data.datapoints.DatapointChatType;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEvent;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.party.PartyManager;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.proxyapi.redis.ServerOutboundMessage;
import net.swofty.commons.protocol.objects.staff.StaffBroadcastProtocolObject;

import java.util.List;

public class ActionPlayerChat implements HypixelEventClass {

    @HypixelEvent(node = EventNodes.PLAYER, requireDataLoaded = false)
    public void run(PlayerChatEvent event) {
        final SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        event.setCancelled(true);

        SkyBlockDataHandler dataHandler = player.getSkyblockDataHandler();
        if (dataHandler == null) return;

        String message = event.getRawMessage();
        Rank rank = player.getRank();

        // Sanitize message to remove any special Unicode characters
        if (!rank.isStaff())
            message = message.replaceAll("[^\\x00-\\x7F]", "");

        String finalMessage = message;

        DatapointChatType.Chats chatType = player.getChatType().currentChatType;
        if (chatType == DatapointChatType.Chats.PARTY) {
            if (!PartyManager.isInParty(player)) {
                player.sendMessage("§cYou are not in a party and were moved to the ALL channel.");
                player.getChatType().switchTo(DatapointChatType.Chats.ALL);
                return;
            }

            PartyManager.sendChat(player, message);
            return;
        }

        if (chatType == DatapointChatType.Chats.STAFF) {
            if (!rank.isStaff()) {
                player.sendMessage("§cYou are not staff and were moved to the ALL channel.");
                player.getChatType().switchTo(DatapointChatType.Chats.ALL);
                return;
            }
            String serverName = HypixelConst.getShortenedServerName() != null
                    ? HypixelConst.getShortenedServerName()
                    : (HypixelConst.getServerName() != null ? HypixelConst.getServerName() : "unknown");

            StaffBroadcastProtocolObject proto = new StaffBroadcastProtocolObject();
            String payload = proto.getSerializer().serialize(
                    new StaffBroadcastProtocolObject.StaffBroadcastMessage(
                            player.getUuid(),
                            player.getFullDisplayName(),
                            finalMessage,
                            serverName
                    )
            );
            ServerOutboundMessage.sendMessageToProxy(
                    net.swofty.commons.proxy.ToProxyChannels.PLAYER_HANDLER,
                    new org.json.JSONObject()
                            .put("uuid", player.getUuid().toString())
                            .put("action", "STAFF_BROADCAST")
                            .put("payload", payload),
                    json -> {}
            );

            // Immediate local echo to staff on this server who have staffview on
            SkyBlockGenericLoader.getLoadedPlayers().stream()
                    .filter(SkyBlockPlayer::getRankIsStaff)
                    .filter(staff -> net.swofty.type.generic.command.commands.ChatCommand.isStaffViewEnabled(staff.getUuid()))
                    .forEach(staff -> staff.sendMessage("§b[STAFF] " + player.getFullDisplayName() + " §7(" + serverName + ")§f: " + finalMessage));
            return;
        }

        List<SkyBlockPlayer> receivers = SkyBlockGenericLoader.getLoadedPlayers();

        receivers.removeIf(receiver -> {
            return HypixelConst.getTypeLoader().getType() == ServerType.SKYBLOCK_ISLAND &&
                    !receiver.getInstance().equals(player.getInstance());
        });

        receivers.forEach(onlinePlayer -> {
            boolean showLevel = onlinePlayer.getToggles().get(DatapointToggles.Toggles.ToggleType.SKYBLOCK_LEVELS_IN_CHAT);

            if (showLevel)
                if (rank.equals(Rank.DEFAULT))
                    onlinePlayer.sendMessage(player.getFullDisplayName() + "§7: " + finalMessage);
                else
                    onlinePlayer.sendMessage(player.getFullDisplayName() + "§f: " + finalMessage);
            else
                if (rank.equals(Rank.DEFAULT))
                    onlinePlayer.sendMessage(rank.getPrefix() + StringUtility.getTextFromComponent(player.getName()) + "§7: " + finalMessage);
                else
                    onlinePlayer.sendMessage(rank.getPrefix() + StringUtility.getTextFromComponent(player.getName()) + "§f: " + finalMessage);
        });
    }
}

