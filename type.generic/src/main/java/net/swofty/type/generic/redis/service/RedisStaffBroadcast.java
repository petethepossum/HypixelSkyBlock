package net.swofty.type.generic.redis.service;

import net.swofty.commons.protocol.objects.staff.StaffBroadcastProtocolObject;
import net.swofty.commons.proxy.FromProxyChannels;
import net.swofty.proxyapi.redis.ProxyToClient;
import net.swofty.type.generic.HypixelGenericLoader;
import net.swofty.type.generic.command.commands.ChatCommand;
import net.swofty.type.generic.user.HypixelPlayer;
import org.json.JSONObject;

public class RedisStaffBroadcast implements ProxyToClient {
    @Override
    public FromProxyChannels getChannel() {
        return FromProxyChannels.STAFF_BROADCAST;
    }

    @Override
    public JSONObject onMessage(JSONObject message) {
        try {
            StaffBroadcastProtocolObject proto = new StaffBroadcastProtocolObject();
            String payload = message.has("payload")
                    ? message.getString("payload")
                    : proto.getSerializer().serialize(
                            new StaffBroadcastProtocolObject.StaffBroadcastMessage(
                                    java.util.UUID.fromString(message.getString("sender")),
                                    message.optString("senderName", "Staff"),
                                    message.getString("message"),
                                    message.optString("server", "unknown")
                            ));
            StaffBroadcastProtocolObject.StaffBroadcastMessage msg =
                    proto.getSerializer().deserialize(payload);

            String resolvedName;
            try {
                resolvedName = net.swofty.type.generic.user.HypixelPlayer.getDisplayName(msg.sender());
            } catch (Exception e) {
                resolvedName = msg.senderName(); // fallback to provided name
            }

            String serverLabel = msg.server() == null ? "" : msg.server();
            boolean showServer = !serverLabel.isBlank() && !"proxy".equalsIgnoreCase(serverLabel);
            String formatted = "§b[STAFF] §f" + resolvedName
                    + (showServer ? " §7(" + serverLabel + ")" : "")
                    + "§f: " + msg.message();

            for (HypixelPlayer player : HypixelGenericLoader.getLoadedPlayers()) {
                if (!player.getRank().isStaff()) continue;
                if (!ChatCommand.isStaffViewEnabled(player.getUuid())) continue;
                player.sendMessage(formatted);
            }


            return new JSONObject()
                    .put("success", true)
                    .put("sender", msg.sender().toString())
                    .put("senderName", msg.senderName())
                    .put("message", msg.message())
                    .put("server", msg.server());
        } catch (Exception e) {
            return new JSONObject().put("success", false).put("error", e.getMessage());
        }
    }
}

