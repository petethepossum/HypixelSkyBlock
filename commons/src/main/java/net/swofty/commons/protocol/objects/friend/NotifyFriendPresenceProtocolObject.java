package net.swofty.commons.protocol.objects.friend;

import net.swofty.commons.protocol.ProtocolObject;
import net.swofty.commons.protocol.Serializer;
import org.json.JSONObject;

import java.util.UUID;

public class NotifyFriendPresenceProtocolObject extends ProtocolObject<
        NotifyFriendPresenceProtocolObject.NotifyFriendPresenceMessage,
        NotifyFriendPresenceProtocolObject.NotifyFriendPresenceResponse> {

    @Override
    public Serializer<NotifyFriendPresenceMessage> getSerializer() {
        return new Serializer<>() {
            @Override
            public String serialize(NotifyFriendPresenceMessage value) {
                JSONObject json = new JSONObject();
                json.put("player", value.player().toString());
                json.put("playerName", value.playerName());
                json.put("online", value.online());
                return json.toString();
            }

            @Override
            public NotifyFriendPresenceMessage deserialize(String json) {
                JSONObject obj = new JSONObject(json);
                return new NotifyFriendPresenceMessage(
                        UUID.fromString(obj.getString("player")),
                        obj.getString("playerName"),
                        obj.getBoolean("online")
                );
            }

            @Override
            public NotifyFriendPresenceMessage clone(NotifyFriendPresenceMessage value) {
                return new NotifyFriendPresenceMessage(value.player(), value.playerName(), value.online());
            }
        };
    }

    @Override
    public Serializer<NotifyFriendPresenceResponse> getReturnSerializer() {
        return new Serializer<>() {
            @Override
            public String serialize(NotifyFriendPresenceResponse value) {
                return value.success() ? "true" : "false";
            }

            @Override
            public NotifyFriendPresenceResponse deserialize(String json) {
                return new NotifyFriendPresenceResponse(Boolean.parseBoolean(json));
            }

            @Override
            public NotifyFriendPresenceResponse clone(NotifyFriendPresenceResponse value) {
                return new NotifyFriendPresenceResponse(value.success());
            }
        };
    }

    public record NotifyFriendPresenceMessage(UUID player, String playerName, boolean online) {}

    public record NotifyFriendPresenceResponse(boolean success) {}
}

