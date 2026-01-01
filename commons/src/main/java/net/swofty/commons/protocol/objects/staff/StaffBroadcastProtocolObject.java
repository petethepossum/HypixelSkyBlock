package net.swofty.commons.protocol.objects.staff;

import net.swofty.commons.protocol.ProtocolObject;
import net.swofty.commons.protocol.Serializer;
import org.json.JSONObject;

import java.util.UUID;

/**
 * Broadcast staff messages (including join/leave) to all game servers.
 */
public class StaffBroadcastProtocolObject extends ProtocolObject<
        StaffBroadcastProtocolObject.StaffBroadcastMessage,
        StaffBroadcastProtocolObject.StaffBroadcastResponse> {

    @Override
    public Serializer<StaffBroadcastMessage> getSerializer() {
        return new Serializer<>() {
            @Override
            public String serialize(StaffBroadcastMessage value) {
                JSONObject json = new JSONObject();
                json.put("sender", value.sender().toString());
                json.put("senderName", value.senderName());
                json.put("message", value.message());
                json.put("server", value.server());
                return json.toString();
            }

            @Override
            public StaffBroadcastMessage deserialize(String json) {
                JSONObject obj = new JSONObject(json);
                return new StaffBroadcastMessage(
                        UUID.fromString(obj.getString("sender")),
                        obj.getString("senderName"),
                        obj.getString("message"),
                        obj.optString("server", "")
                );
            }

            @Override
            public StaffBroadcastMessage clone(StaffBroadcastMessage value) {
                return new StaffBroadcastMessage(value.sender(), value.senderName(), value.message(), value.server());
            }
        };
    }

    @Override
    public Serializer<StaffBroadcastResponse> getReturnSerializer() {
        return new Serializer<>() {
            @Override
            public String serialize(StaffBroadcastResponse value) {
                return value.success() ? "true" : "false";
            }

            @Override
            public StaffBroadcastResponse deserialize(String json) {
                return new StaffBroadcastResponse(Boolean.parseBoolean(json));
            }

            @Override
            public StaffBroadcastResponse clone(StaffBroadcastResponse value) {
                return new StaffBroadcastResponse(value.success());
            }
        };
    }

    public record StaffBroadcastMessage(UUID sender, String senderName, String message, String server) {}
    public record StaffBroadcastResponse(boolean success) {}
}

