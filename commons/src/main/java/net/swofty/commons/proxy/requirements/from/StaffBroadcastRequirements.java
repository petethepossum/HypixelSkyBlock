package net.swofty.commons.proxy.requirements.from;

import java.util.List;

public class StaffBroadcastRequirements implements ProxyChannelRequirements {
    @Override
    public List<RequiredKey> getRequiredKeysForServer() {
        return List.of(
                new RequiredKey("sender"),
                new RequiredKey("senderName"),
                new RequiredKey("message"),
                new RequiredKey("server")
        );
    }

    @Override
    public List<RequiredKey> getRequiredKeysForProxy() {
        return List.of();
    }
}

