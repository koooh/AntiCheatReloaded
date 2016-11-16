package net.gravitydevelopment.anticheat.util;

import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_10_R1.CraftServer;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.mojang.authlib.GameProfile;

import net.gravitydevelopment.anticheat.AntiCheat;
import net.minecraft.server.v1_10_R1.EntityHuman;
import net.minecraft.server.v1_10_R1.EntityPlayer;
import net.minecraft.server.v1_10_R1.MinecraftServer;
import net.minecraft.server.v1_10_R1.NPC;
import net.minecraft.server.v1_10_R1.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_10_R1.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_10_R1.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_10_R1.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_10_R1.PlayerConnection;
import net.minecraft.server.v1_10_R1.PlayerInteractManager;
import net.minecraft.server.v1_10_R1.World;
import net.minecraft.server.v1_10_R1.WorldServer;

public class NPC_1_10 {

	private final UUID owner;
	private final String name;
	private UUID uuid;
	private EntityPlayer npc;
	private int entityId;
	private Location location;

	public int getID() {
		return this.entityId;
	}
	
	public NPC_1_10(Player owner) {
		this.owner = owner.getUniqueId();
		this.name = NameGenerator.generateName();
		Location eyelocation = owner.getEyeLocation();
		Vector vec = owner.getLocation().getDirection();
		vec.setX(-3 * vec.getX());
		vec.setZ(-3 * vec.getZ());
		Location back = eyelocation.add(vec);
		this.location = back; // TODO
	}

	public void spawn() {
		final MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
		final WorldServer nmsWorld = ((CraftWorld) this.getOwner().getWorld()).getHandle();
		this.uuid = UUID.randomUUID();
		(this.npc = new EntityPlayer(nmsServer, nmsWorld, new GameProfile(this.uuid, this.name),
				new PlayerInteractManager((World) nmsWorld))).setLocation(this.location.getX(), this.location.getY(),
						this.location.getZ(), 0.0f, 0.0f);
		this.npc.collides = false;
		this.npc.setInvisible(true);
		this.entityId = this.npc.getId();
		final PlayerConnection connection = ((CraftPlayer) this.getOwner()).getHandle().playerConnection;
		connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER,
				new EntityPlayer[] { this.npc }));
		connection.sendPacket(new PacketPlayOutNamedEntitySpawn((EntityHuman) this.npc));
	}
	
	public boolean up = true;

	public void move(PlayerMoveEvent e) {
		if (e.getTo().getPitch() <= -32.5) {
			if (e.getTo().getPitch() <= -82.5) {
				Location eyelocation = e.getPlayer().getEyeLocation();
				Vector vec = e.getPlayer().getLocation().getDirection();
				vec.setX(-3 * vec.getX());
				vec.setY(e.getPlayer().getLocation().getY() - 8.25);
				vec.setZ(-3 * vec.getZ());
				Location back = eyelocation.add(vec);
				this.location = back; // TODO
				this.npc.teleportTo(back, false);
				((CraftPlayer) this.getOwner()).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityTeleport(npc));
				if (new Random().nextInt(50) == 25 && this.npc.isInvisible()) {
					this.npc.setInvisible(false);
					((CraftPlayer) this.getOwner()).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityMetadata(entityId, npc.getDataWatcher(), false));
					new BukkitRunnable() {
						@Override
						public void run() {
							NPC_1_10.this.npc.setInvisible(true);
							((CraftPlayer) NPC_1_10.this.getOwner()).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityMetadata(entityId, npc.getDataWatcher(), false));
						}
					}.runTaskLater(AntiCheat.getPlugin(), 4);
				}
			}else {
				Location eyelocation = e.getPlayer().getEyeLocation();
				Vector vec = e.getPlayer().getLocation().getDirection();
				vec.setX(-3 * vec.getX());
				vec.setY(e.getPlayer().getLocation().getY());
				vec.setZ(-3 * vec.getZ());
				Location back = eyelocation.add(vec);
				this.location = back; // TODO
				this.npc.teleportTo(back, false);
				((CraftPlayer) this.getOwner()).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityTeleport(npc));
			}
		} else if (e.getTo().getPitch() >= 22) {
			Location eyelocation = e.getPlayer().getEyeLocation();
			Vector vec = e.getPlayer().getLocation().getDirection();
			vec.setX(-3 * vec.getX());
			vec.setY(e.getPlayer().getLocation().getY() - 0.4);
			vec.setZ(-3 * vec.getZ());
			Location back = eyelocation.add(vec);
			this.location = back; // TODO
			this.npc.teleportTo(back, false);
			((CraftPlayer) this.getOwner()).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityTeleport(npc));
		} else {
			Location eyelocation = e.getPlayer().getEyeLocation();
			Vector vec = e.getPlayer().getLocation().getDirection();
			vec.setX(-1.95 * vec.getX());
			if (up)
				vec.setY(vec.getY() + 0.5);
			else
				vec.setY(vec.getY() - 0.25);
			if (new Random().nextInt(10) == 5)
				up = !up;
			vec.setZ(-1.95 * vec.getZ());
			Location back = eyelocation.add(vec);
			this.location = back; // TODO
			this.npc.setPositionRotation(back.getX(), back.getY(), back.getZ(), (float)new Random().nextFloat() * 20, (float)new Random().nextFloat() * 20);
			this.npc.teleportTo(back, false);
			((CraftPlayer) this.getOwner()).getHandle().playerConnection
					.sendPacket(new PacketPlayOutEntityTeleport(npc));
			if (new Random().nextInt(1000) == 500 && this.npc.isInvisible()) {
				this.npc.setInvisible(false);
				((CraftPlayer) this.getOwner()).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityMetadata(entityId, npc.getDataWatcher(), false));
				new BukkitRunnable() {
					@Override
					public void run() {
						NPC_1_10.this.npc.setInvisible(true);
						((CraftPlayer) NPC_1_10.this.getOwner()).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityMetadata(entityId, npc.getDataWatcher(), false));
					}
				}.runTaskLater(AntiCheat.getPlugin(), 6);
			}
		}
	}

	public Player getOwner() {
		return Bukkit.getPlayer(owner);
	}

	public void damage(EntityDamageByEntityEvent e, Player p) {
		if (p != getOwner())
			return;
		if (new Random().nextBoolean() && this.npc.isInvisible()) {
			this.npc.setInvisible(false);
			((CraftPlayer) this.getOwner()).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityMetadata(entityId, npc.getDataWatcher(), false));
			new BukkitRunnable() {
				@Override
				public void run() {
				    if (NPC_1_10.this.npc != null && NPC_1_10.this.getOwner() != null) {
				        NPC_1_10.this.npc.setInvisible(true);
				        ((CraftPlayer) NPC_1_10.this.getOwner()).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityMetadata(entityId, npc.getDataWatcher(), false));
				    }
				}
			}.runTaskLater(AntiCheat.getPlugin(), 6 + new Random().nextInt(7));
		}
	}
	
	public void damage() {
		if (new Random().nextBoolean() && this.npc.isInvisible()) {
			this.npc.setInvisible(false);
			((CraftPlayer) this.getOwner()).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityMetadata(entityId, npc.getDataWatcher(), false));
			new BukkitRunnable() {
				@Override
				public void run() {
					NPC_1_10.this.npc.setInvisible(true);
					((CraftPlayer) NPC_1_10.this.getOwner()).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityMetadata(entityId, npc.getDataWatcher(), false));
				}
			}.runTaskLater(AntiCheat.getPlugin(), 6 + new Random().nextInt(8));
		}
	}

}
