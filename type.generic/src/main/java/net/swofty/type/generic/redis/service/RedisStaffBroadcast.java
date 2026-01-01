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
            StaffBroadcastProtocolObject.StaffBroadcastMessage msg =
                    proto.getSerializer().deserialize(message.getString("payload"));

            String formatted = "§b[STAFF] §f" + msg.senderName() + " §7(" + msg.server() + ")§f: " + msg.message();

            for (HypixelPlayer player : HypixelGenericLoader.getLoadedPlayers()) {
                if (!player.getRank().isStaff()) continue;
                if (!ChatCommand.isStaffViewEnabled(player.getUuid())) continue;
                player.sendMessage(formatted);
            }

            return new JSONObject().put("success", true);
        } catch (Exception e) {
            return new JSONObject().put("success", false).put("error", e.getMessage());
        }
    }
}

