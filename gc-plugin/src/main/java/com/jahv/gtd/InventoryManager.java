package com.jahv.gtd;

import emu.grasscutter.game.avatar.Avatar;
import emu.grasscutter.game.entity.EntityAvatar;
import emu.grasscutter.game.inventory.GameItem;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.props.FightProperty;
import emu.grasscutter.server.packet.send.PacketAvatarFightPropUpdateNotify;
import emu.grasscutter.server.packet.send.PacketAvatarLifeStateChangeNotify;

public class InventoryManager {
    public static GameItem useItem(Player player, long targetGuid, long itemGuid, int count) {
        Avatar target = player.getAvatars().getAvatarByGuid(targetGuid);
        GameItem useItem = player.getInventory().getItemByGuid(itemGuid);

        if (useItem == null) {
            return null;
        }

        int used = 0;

        // Use
        switch (useItem.getItemData().getMaterialType()) {
            case MATERIAL_NOTICE_ADD_HP:
            case MATERIAL_FOOD:
                if (useItem.getItemData().getUseTarget().equals("ITEM_USE_TARGET_SPECIFY_ALIVE_AVATAR")) {
                    if (target == null) {
                        break;
                    }
                    for (EntityAvatar entity : player.getTeamManager().getActiveTeam()) {
                        if (entity.getAvatar() != target) continue;
                        boolean isAlive = entity.isAlive();
                        var per = useItem.getItemData().getRankLevel() * 10;
                        var extra = useItem.getItemData().getRankLevel() * 1000;
                        var maxHp = entity.getFightProperty(FightProperty.FIGHT_PROP_MAX_HP);
                        var newHp = maxHp / 100 * per + extra;
                        var nowHp = entity.getFightProperty(FightProperty.FIGHT_PROP_CUR_HP);
                        entity.setFightProperty(
                                FightProperty.FIGHT_PROP_CUR_HP,
                                Math.min(maxHp, nowHp + newHp)
                        );
                        entity.getWorld().broadcastPacket(new PacketAvatarFightPropUpdateNotify(entity.getAvatar(), FightProperty.FIGHT_PROP_CUR_HP));
                        if (!isAlive) {
                            entity.getWorld().broadcastPacket(new PacketAvatarLifeStateChangeNotify(entity.getAvatar()));
                        }
                        used = 1;
                    }

                }
                break;
            default:
                break;
        }

        if (used > 0) {
            player.getInventory().removeItem(useItem, used);
            return useItem;
        }

        return null;
    }
}
