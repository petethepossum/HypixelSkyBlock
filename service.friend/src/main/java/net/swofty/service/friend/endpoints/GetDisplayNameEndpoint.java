package net.swofty.service.friend.endpoints;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import net.swofty.commons.impl.ServiceProxyRequest;
import net.swofty.commons.protocol.objects.player.GetDisplayNameProtocolObject;
import net.swofty.service.friend.FriendDatabase;
import net.swofty.service.generic.redis.ServiceEndpoint;
import org.bson.Document;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GetDisplayNameEndpoint implements ServiceEndpoint<
        GetDisplayNameProtocolObject.GetDisplayNameMessage,
        GetDisplayNameProtocolObject.GetDisplayNameResponse> {

    private static final Map<String, String> RANK_PREFIX = new HashMap<>() {{ //this is a bit silly but it works
        put("OWNER", "§c[OWNER] ");
        put("ADMIN", "§c[ADMIN] ");
        put("DEVELOPER", "§d[DEV] ");
        put("GAMEMASTER", "§2[GM] ");
        put("MOD", "§2[MOD] ");
        put("HELPER", "§9[HELPER] ");
        put("JRHELPER", "§9[JR HELPER] ");
        put("BT", "§d[BT] ");
        put("YOUTUBE", "§c[§fYOUTUBE§c] ");
        put("MVP_PLUS", "§b[MVP§c+§b] ");
        put("MVP", "§b[MVP] ");
        put("VIP_PLUS", "§a[VIP§6+§a] ");
        put("VIP", "§a[VIP] ");
        put("DEFAULT", "§7");
    }};

    @Override
    public GetDisplayNameProtocolObject associatedProtocolObject() {
        return new GetDisplayNameProtocolObject();
    }

    @Override
    public GetDisplayNameProtocolObject.GetDisplayNameResponse onMessage(
            ServiceProxyRequest message,
            GetDisplayNameProtocolObject.GetDisplayNameMessage messageObject) {

        UUID target = messageObject.player();
        if (FriendDatabase.database == null) {
            return new GetDisplayNameProtocolObject.GetDisplayNameResponse(target, "DEFAULT", "§7Unknown", "Unknown");
        }

        Document doc = fetchDoc("data", target);
        if (doc == null) {
            doc = fetchDoc("profiles", target);
        }

        String ign = extractName(doc);
        String rank = extractRank(doc);
        String prefix = RANK_PREFIX.getOrDefault(rank.toUpperCase(), RANK_PREFIX.get("DEFAULT"));
        String display = prefix + ign;

        return new GetDisplayNameProtocolObject.GetDisplayNameResponse(target, rank, display, ign);
    }

    private Document fetchDoc(String collection, UUID uuid) {
        try {
            MongoCollection<Document> col = FriendDatabase.database.getCollection(collection);
            if (col == null) return null;
            return col.find(Filters.eq("_id", uuid.toString())).first();
        } catch (Exception e) {
            Logger.error(e, "Failed to fetch display name doc from {}", collection);
            return null;
        }
    }

    private String extractName(Document doc) {
        if (doc == null) return "Unknown";
        String ign = parse(doc.getString("ign"));
        if (ign == null && doc.containsKey("ignLowercase")) {
            ign = parse(doc.getString("ignLowercase"));
        }
        return ign != null ? ign : "Unknown";
    }

    private String extractRank(Document doc) {
        if (doc == null) return "DEFAULT";
        String rank = parse(doc.getString("rank"));
        return rank != null ? rank : "DEFAULT";
    }

    private String parse(String raw) {
        if (raw == null) return null;
        raw = raw.trim();
        if (raw.startsWith("\"") && raw.endsWith("\"") && raw.length() >= 2) {
            raw = raw.substring(1, raw.length() - 1);
        }
        return raw.isEmpty() ? null : raw;
    }
}

