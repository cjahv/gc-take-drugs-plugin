package com.jahv.gtd;

import com.google.protobuf.InvalidProtocolBufferException;
import emu.grasscutter.Grasscutter;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.UseItemReqOuterClass;
import emu.grasscutter.plugin.Plugin;
import emu.grasscutter.plugin.PluginManager;
import emu.grasscutter.server.event.EventHandler;
import emu.grasscutter.server.event.HandlerPriority;
import emu.grasscutter.server.event.game.ReceivePacketEvent;
import emu.grasscutter.server.packet.send.PacketUseItemRsp;

import java.util.Locale;
import java.util.ResourceBundle;

public class WakeUpPlugin extends Plugin {
    private static final ResourceBundle bundle = ResourceBundle.getBundle("locale/LAN_GcTakeDrugsPlugin", Locale.US);

    @Override
    public void onLoad() {
        Grasscutter.getLogger().debug(bundle.getString("plugin_load"));
    }

    @Override
    public void onEnable() {
        PluginManager pluginManager = Grasscutter.getPluginManager();

        var handler = new EventHandler<>(ReceivePacketEvent.class);
        handler.priority(HandlerPriority.LOW);
        handler.listener(event -> {
            try {
                if (event.getPacketId() != PacketOpcodes.UseItemReq) return;
                var payload = event.getPacketData();
                UseItemReqOuterClass.UseItemReq req = UseItemReqOuterClass.UseItemReq.parseFrom(payload);
                var session = event.getGameSession();
                var useItem = InventoryManager.useItem(session.getPlayer(), req.getTargetGuid(), req.getGuid(), req.getCount());

                if (useItem != null) {
                    event.cancel();
                    session.send(new PacketUseItemRsp(req.getTargetGuid(), useItem));
                }
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        });
        pluginManager.registerListener(handler);

        Grasscutter.getLogger().info(bundle.getString("plugin_enable"));
    }

    @Override
    public void onDisable() {
        Grasscutter.getLogger().info(bundle.getString("plugin_disable"));
    }
}
