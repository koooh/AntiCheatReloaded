package net.gravitydevelopment.anticheat.check.combat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

import net.gravitydevelopment.anticheat.AntiCheat;
import net.gravitydevelopment.anticheat.check.CheckResult;
import net.gravitydevelopment.anticheat.check.CheckType;
import net.gravitydevelopment.anticheat.event.EventListener;
import net.gravitydevelopment.anticheat.util.NPC_1_10;
import net.gravitydevelopment.anticheat.util.VersionUtil;

/**
 * @author Rammelkast
 */
public class KillAuraCheck {
	
	private static final Map<UUID, NPC_1_10> NPC_LIST = new HashMap<UUID, NPC_1_10>();
	private static final Map<UUID, Integer> VL_COUNT = new HashMap<UUID, Integer>();
	
	public static void cleanPlayer(Player p) {
		if (!VersionUtil.getVersion().equals("v1_10_R1")) {
			return;
		}
		NPC_LIST.remove(p.getUniqueId());
	}
	
	public static void listenPackets() {
		if (!VersionUtil.getVersion().equals("v1_10_R1")) {
			return;
		}
		AntiCheat.getProtocolManager().addPacketListener(new PacketAdapter(AntiCheat.getPlugin(), ListenerPriority.NORMAL, new PacketType[] {PacketType.Play.Client.USE_ENTITY}) {
			@Override
			public void onPacketReceiving(PacketEvent e) {
				Player p = e.getPlayer();
				if (!NPC_LIST.containsKey(p.getUniqueId()))
					return;
				int id = e.getPacket().getIntegers().read(0);
				if (NPC_LIST.containsKey(p.getUniqueId())) {
					if (NPC_LIST.get(p.getUniqueId()).getID() != id) {
						return;
					}
				}
				if (!VL_COUNT.containsKey(p.getUniqueId())) {
					VL_COUNT.put(p.getUniqueId(), 1);
					NPC_LIST.get(p.getUniqueId()).damage();
				}else {
					VL_COUNT.put(p.getUniqueId(), VL_COUNT.get(p.getUniqueId()) + 1);
					if (VL_COUNT.get(p.getUniqueId()) >= 5) {
						EventListener.log(new CheckResult(CheckResult.Result.FAILED, p.getName() + " failed KillAura (botcheck), hit the bot " + VL_COUNT.get(p.getUniqueId()) + " times (max=5)").getMessage(), p, CheckType.BLINK);
						VL_COUNT.remove(p.getUniqueId());
					}
				}
			}
		});
	}

	public static void doDamageEvent(EntityDamageByEntityEvent e, Player p) {
		if (!VersionUtil.getVersion().equals("v1_10_R1")) {
			return;
		}
		if (NPC_LIST.containsKey(p.getUniqueId()))
			NPC_LIST.get(p.getUniqueId()).damage(e, p);
		else {
			NPC_1_10 npc = new NPC_1_10(p);
			NPC_LIST.put(p.getUniqueId(), npc);
			npc.spawn();
		}
	}
	
	public static void doMove(PlayerMoveEvent e) {
		if (!VersionUtil.getVersion().equals("v1_10_R1")) {
			return;
		}
		if (NPC_LIST.containsKey(e.getPlayer().getUniqueId()))
			NPC_LIST.get(e.getPlayer().getUniqueId()).move(e);
	}
	
}
