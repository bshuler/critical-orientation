package net.critical.orientation;

//? if fabric {
import net.fabricmc.api.ClientModInitializer;
//?} elif neoforge {
/*import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
*///?} elif forge {
/*import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
*///?}

//? if fabric {
public class OrientationClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        OrientationCommon.init();
        OrientationKeyBind.register();
    }
}
//?} elif neoforge {
/*@Mod(OrientationCommon.MOD_ID)
public class OrientationClient {

    public OrientationClient(IEventBus modEventBus) {
        modEventBus.addListener(this::onClientSetup);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        OrientationCommon.init();
        OrientationKeyBind.register();
    }
}
*///?} elif forge {
/*@Mod(OrientationCommon.MOD_ID)
public class OrientationClient {

    public OrientationClient() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        OrientationCommon.init();
        OrientationKeyBind.register();
    }
}
*///?}
