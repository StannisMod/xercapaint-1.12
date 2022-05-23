package xerca.xercapaint.common;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xerca.xercapaint.common.command.CommandExport;
import xerca.xercapaint.common.command.CommandImport;
import xerca.xercapaint.common.entity.Entities;
import xerca.xercapaint.common.packets.*;

@Mod(modid = XercaPaint.MODID, name = XercaPaint.NAME)
public class XercaPaint {
    public static final String MODID = "xercapaint";
    public static final String NAME = "Xerca Paint";

    @Mod.Instance(XercaPaint.MODID)
    public static XercaPaint instance;

    @SidedProxy(clientSide = "xerca.xercapaint.client.ClientProxy", serverSide = "xerca.xercapaint.server.ServerProxy")
    public static Proxy proxy;
    public static SimpleNetworkWrapper network;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        int msg_id = 0;
        network = NetworkRegistry.INSTANCE.newSimpleChannel("XercaChannel");
        network.registerMessage(CanvasUpdatePacketHandler.class, CanvasUpdatePacket.class, msg_id++, Side.SERVER);
        network.registerMessage(PaletteUpdatePacketHandler.class, PaletteUpdatePacket.class, msg_id++, Side.SERVER);
        network.registerMessage(ExportPaintingPacketHandler.class, ExportPaintingPacket.class, msg_id++, Side.CLIENT);
        network.registerMessage(ImportPaintingPacketHandler.class, ImportPaintingPacket.class, msg_id++, Side.CLIENT);
        network.registerMessage(ImportPaintingSendPacketHandler.class, ImportPaintingSendPacket.class, msg_id++, Side.SERVER);
        proxy.preInit();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init();
        Entities.init();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit();
    }

    @EventHandler
    public void serverStart(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandExport());
        event.registerServerCommand(new CommandImport());
    }

    public static final Logger LOGGER = LogManager.getLogger();

}
