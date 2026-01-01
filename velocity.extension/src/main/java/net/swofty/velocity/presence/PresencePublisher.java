package net.swofty.velocity.presence;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.swofty.commons.ServiceType;
import net.swofty.commons.presence.PresenceInfo;
import net.swofty.commons.protocol.objects.presence.UpdatePresenceProtocolObject;
import net.swofty.proxyapi.redis.ServerOutboundMessage;

import java.util.UUID;

public final class PresencePublisher {

    private PresencePublisher() {}

    public static void publish(Player player, boolean online, String serverType, UUID serverId) {
        PresenceInfo info = new PresenceInfo(
                player.getUniqueId(),
                online,
                serverType,
                serverId != null ? serverId.toString() : null,
                System.currentTimeMillis()
        );

        ServerOutboundMessage.sendMessageToAllServicesFireAndForget(
                new UpdatePresenceProtocolObject(),
                new UpdatePresenceProtocolObject.UpdatePresenceMessage(info)
        );
    }

    public static void publish(Player player, boolean online, RegisteredServer server, String serverType) {
        String serverIdStr = null;
        if (server != null) {
            var gameServer = net.swofty.velocity.gamemanager.GameManager.getFromRegisteredServer(server);
            if (gameServer != null) {
                serverIdStr = gameServer.shortDisplayName();
            } else {
                serverIdStr = server.getServerInfo().getName();
            }
        }

        PresenceInfo info = new PresenceInfo(
                player.getUniqueId(),
                online,
                serverType,
                serverIdStr,
                System.currentTimeMillis()
        );

        ServerOutboundMessage.sendMessageToAllServicesFireAndForget(
                new UpdatePresenceProtocolObject(),
                new UpdatePresenceProtocolObject.UpdatePresenceMessage(info)
        );
    }
}

