package net.swofty.service.friend.endpoints;

import net.swofty.commons.impl.ServiceProxyRequest;
import net.swofty.commons.protocol.objects.friend.NotifyFriendPresenceProtocolObject;
import net.swofty.service.friend.FriendCache;
import net.swofty.service.generic.redis.ServiceEndpoint;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FriendPresenceNotificationEndpoint implements ServiceEndpoint<
        NotifyFriendPresenceProtocolObject.NotifyFriendPresenceMessage,
        NotifyFriendPresenceProtocolObject.NotifyFriendPresenceResponse> {

    private static final Map<UUID, State> lastNotifiedState = new ConcurrentHashMap<>();
    private static final long DEBOUNCE_MS = 5000;

    @Override
    public NotifyFriendPresenceProtocolObject associatedProtocolObject() {
        return new NotifyFriendPresenceProtocolObject();
    }

    @Override
    public NotifyFriendPresenceProtocolObject.NotifyFriendPresenceResponse onMessage(
            ServiceProxyRequest message,
            NotifyFriendPresenceProtocolObject.NotifyFriendPresenceMessage messageObject) {

        // Merge presence but keep server info if missing
        net.swofty.service.friend.PresenceStorage.upsertPreservingServer(
                new net.swofty.commons.presence.PresenceInfo(
                        messageObject.player(),
                        messageObject.online(),
                        null,
                        null,
                        System.currentTimeMillis()
                )
        );

        long now = System.currentTimeMillis();
        State prev = lastNotifiedState.get(messageObject.player());
        boolean stateChanged = prev == null || prev.online != messageObject.online();
        boolean allowNotify = stateChanged || (prev != null && now - prev.lastNotifiedMs > DEBOUNCE_MS);

        if (allowNotify) {
            if (messageObject.online()) {
                FriendCache.handlePlayerJoin(messageObject.player(), messageObject.playerName());
            } else {
                FriendCache.handlePlayerLeave(messageObject.player(), messageObject.playerName());
            }
            lastNotifiedState.put(messageObject.player(), new State(messageObject.online(), now));
        }

        return new NotifyFriendPresenceProtocolObject.NotifyFriendPresenceResponse(true);
    }

    private record State(boolean online, long lastNotifiedMs) {}
}

