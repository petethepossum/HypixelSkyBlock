package net.swofty.commons.protocol.objects.player;

import net.swofty.commons.protocol.ProtocolObject;
import net.swofty.commons.protocol.Serializer;
import org.json.JSONObject;

import java.util.UUID;

/**
 * Resolve a player's formatted display name (rank prefix + name) and raw rank string. //this probably shouldn't be in friends service. But it already had Mongo access so whatever.
 */
public class GetDisplayNameProtocolObject extends ProtocolObject<
        GetDisplayNameProtocolObject.GetDisplayNameMessage,
        GetDisplayNameProtocolObject.GetDisplayNameResponse> {

    @Override
    public Serializer<GetDisplayNameMessage> getSerializer() {
        return new Serializer<>() {
            @Override
            public String serialize(GetDisplayNameMessage value) {
                return new JSONObject()
                        .put("player", value.player().toString())
                        .toString();
            }

            @Override
            public GetDisplayNameMessage deserialize(String json) {
                JSONObject obj = new JSONObject(json);
                return new GetDisplayNameMessage(UUID.fromString(obj.getString("player")));
            }

            @Override
            public GetDisplayNameMessage clone(GetDisplayNameMessage value) {
                return new GetDisplayNameMessage(value.player());
            }
        };
    }

    @Override
    public Serializer<GetDisplayNameResponse> getReturnSerializer() {
        return new Serializer<>() {
            @Override
            public String serialize(GetDisplayNameResponse value) {
                return new JSONObject()
                        .put("player", value.player().toString())
                        .put("rank", value.rank())
                        .put("displayName", value.displayName())
                        .put("ign", value.ign())
                        .toString();
            }

            @Override
            public GetDisplayNameResponse deserialize(String json) {
                JSONObject obj = new JSONObject(json);
                return new GetDisplayNameResponse(
                        UUID.fromString(obj.getString("player")),
                        obj.optString("rank", "DEFAULT"),
                        obj.optString("displayName", obj.optString("ign", "")),
                        obj.optString("ign", "")
                );
            }

            @Override
            public GetDisplayNameResponse clone(GetDisplayNameResponse value) {
                return new GetDisplayNameResponse(value.player(), value.rank(), value.displayName(), value.ign());
            }
        };
    }

    public record GetDisplayNameMessage(UUID player) {}

    public record GetDisplayNameResponse(UUID player, String rank, String displayName, String ign) {}
}

