package net.critical.orientation;

import net.fabricmc.api.ClientModInitializer;

public class OrientationClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        OrientationKeyBind.register();
    }
}
