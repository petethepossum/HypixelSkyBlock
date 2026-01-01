package net.swofty.service.friend.endpoints;

import net.swofty.commons.impl.ServiceProxyRequest;
import net.swofty.commons.protocol.objects.friend.NotifyFriendPresenceProtocolObject;
import net.swofty.service.friend.FriendCache;
import net.swofty.service.generic.redis.ServiceEndpoint;

public class FriendPresenceNotificationEndpoint implements ServiceEndpoint<
        NotifyFriendPresenceProtocolObject.NotifyFriendPresenceMessage,
        NotifyFriendPresenceProtocolObject.NotifyFriendPresenceResponse> {

    @Override
    public NotifyFriendPresenceProtocolObject associatedProtocolObject() {
        return new NotifyFriendPresenceProtocolObject();
    }

    @Override
    public NotifyFriendPresenceProtocolObject.NotifyFriendPresenceResponse onMessage(
            ServiceProxyRequest message,
            NotifyFriendPresenceProtocolObject.NotifyFriendPresenceMessage messageObject) {

        if (messageObject.online()) {
            FriendCache.handlePlayerJoin(messageObject.player(), messageObject.playerName());
        } else {
            FriendCache.handlePlayerLeave(messageObject.player(), messageObject.playerName());
        }
        return new NotifyFriendPresenceProtocolObject.NotifyFriendPresenceResponse(true);
    }
}

