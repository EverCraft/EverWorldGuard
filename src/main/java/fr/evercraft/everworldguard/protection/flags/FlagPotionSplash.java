/*
 * This file is part of EverWorldGuard.
 *
 * EverWorldGuard is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EverWorldGuard is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EverWorldGuard.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.evercraft.everworldguard.protection.flags;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.carrier.TileEntityCarrier;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.ThrownPotion;
import org.spongepowered.api.event.action.CollideEvent;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.BlockSpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnCause;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3i;

import fr.evercraft.everapi.services.worldguard.WorldGuardWorld;
import fr.evercraft.everapi.services.worldguard.flag.CatalogTypeFlag;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.protection.EProtectionService;
import fr.evercraft.everworldguard.protection.index.EWWorld;
import fr.evercraft.everworldguard.EWMessage.EWMessages;

public class FlagPotionSplash extends CatalogTypeFlag<PotionEffectType> {

	private final EverWorldGuard plugin;

	public FlagPotionSplash(EverWorldGuard plugin) {
		super("POTION_SPLASH");
		
		this.plugin = plugin;
		
		this.reload();
	}
	
	@Override
	protected Map<String, Set<PotionEffectType>> getConfig() {
		return this.plugin.getConfigFlags().get(this.getName(), PotionEffectType.class);
	}
	
	@Override
	public String getDescription() {
		return EWMessages.FLAG_POTION_SPLASH_DESCRIPTION.getString();
	}
	
	public boolean sendMessage(Player player, Vector3i position, PotionEffectType type) {
		return this.plugin.getProtectionService().sendMessage(player, this,
				EWMessages.FLAG_POTION_SPLASH_MESSAGE.sender()
					.replace("{x}", position.getX())
					.replace("{y}", position.getY())
					.replace("{z}", position.getZ())
					.replace("{potion}", type.getTranslation()));
	}

	/*
	 * InteractItemEvent.Secondary
	 */
	
	public void onInteractItemSecondary(InteractItemEvent.Secondary event) {
		if (event.isCancelled()) return;
		
		Optional<List<PotionEffect>> potions = event.getItemStack().get(Keys.POTION_EFFECTS);
		if (!potions.isPresent() || potions.get().isEmpty()) return;
		
		Optional<Player> optPlayer = event.getCause().get(NamedCause.SOURCE, Player.class);
		if (!optPlayer.isPresent()) return;
		Player player = optPlayer.get();
		
		// Bypass
		if (this.plugin.getProtectionService().hasBypass(player)) return;
		
		WorldGuardWorld world = this.plugin.getProtectionService().getOrCreateEWorld(player.getWorld());
		for (PotionEffectType potion : potions.get().stream().map(potion -> potion.getType()).collect(Collectors.toSet())) {
			if (this.getDefault().containsValue(potion) && !world.getRegions(player.getLocation().getPosition()).getFlag(player, player.getLocation(), this).containsValue(potion)) {
				event.setCancelled(true);
				
				// Message
				this.sendMessage(player, player.getLocation().getPosition().toInt(), potion);
				return;
			}
		}
	}
	
	/*
	 * SpawnEntityEvent
	 */
	
	public void onSpawnEntity(SpawnEntityEvent event) {
		if (event.isCancelled()) return;
		
		Optional<SpawnCause> optSpawn = event.getCause().get(NamedCause.SOURCE, SpawnCause.class);
		if (!optSpawn.isPresent()) return;
		SpawnCause spawn = optSpawn.get();
		
		Optional<Player> optNotifier = event.getCause().get(NamedCause.NOTIFIER, Player.class);
		if (optNotifier.isPresent()) {
			this.onSpawnEntityPlayer(this.plugin.getProtectionService(), event, spawn, optNotifier.get());
			return;
		} 
		
		Optional<Player> optPlayer = event.getCause().get(NamedCause.OWNER, Player.class);
		if (optPlayer.isPresent()) {
			this.onSpawnEntityPlayer(this.plugin.getProtectionService(), event, spawn, optPlayer.get());
			return;
		}
	}
	
	
	private void onSpawnEntityPlayer(EProtectionService service, SpawnEntityEvent event, SpawnCause spawn, Player player) {
		// Bypass
		if (this.plugin.getProtectionService().hasBypass(player)) return;
		
		List<? extends Entity> filter = event.filterEntities(entity -> {
			Optional<List<PotionEffect>> potions = entity.get(Keys.POTION_EFFECTS);
			if (!potions.isPresent() || potions.get().isEmpty()) return true;
			
			EWWorld world = this.plugin.getProtectionService().getOrCreateEWorld(entity.getWorld());
			for (PotionEffectType potion : potions.get().stream().map(potion -> potion.getType()).collect(Collectors.toSet())) {
				if (this.getDefault().containsValue(potion) && !world.getRegions(entity.getLocation().getPosition()).getFlag(player, entity.getLocation(), this).containsValue(potion)) {
					if (spawn instanceof EntitySpawnCause && ((EntitySpawnCause) spawn).getEntity().equals(player)) {			
						this.sendMessage(player, entity.getLocation().getPosition().toInt(), potion);
					}
					return false;
				}
			}
			return true;
		});
		if (filter.isEmpty()) return;
		
		if (spawn instanceof BlockSpawnCause) {
			this.onSpawnEntityDispense((BlockSpawnCause) spawn, filter);
		}
	}
	
	// Remet les items dans le dispenser
	private void onSpawnEntityDispense(BlockSpawnCause spawn, List<? extends Entity> filter) {		
		Optional<Location<World>> location = spawn.getBlockSnapshot().getLocation();
		if (!location.isPresent() || !location.get().getTileEntity().isPresent()) return;
		
		TileEntity tile = location.get().getTileEntity().get();
		if (!(tile instanceof TileEntityCarrier)) return;
		
		Inventory inventory = ((TileEntityCarrier) tile).getInventory();
		filter.forEach(entity -> {
			// Bug : Keys.REPRESENTED_ITEM est EMPTY
			Optional<ItemStackSnapshot> item = entity.get(Keys.REPRESENTED_ITEM);
			if (!item.isPresent()) return;
			
			inventory.offer(ItemStack.builder()
				.fromSnapshot(item.get())
				.build());
		});
	}
	
	/*
	 * CollideEvent.Impact
	 */

	public void onCollideImpact(WorldGuardWorld world, CollideEvent.Impact event) {
		if (event.isCancelled()) return;
				
		Optional<ThrownPotion> optPotion = event.getCause().get(NamedCause.SOURCE, ThrownPotion.class);
		if (!optPotion.isPresent()) return;
		ThrownPotion entity = optPotion.get();
				
		Optional<List<PotionEffect>> potions = entity.get(Keys.POTION_EFFECTS);
		if (!potions.isPresent() || potions.get().isEmpty()) return;
				
		Optional<Player> optPlayer = event.getCause().get(NamedCause.OWNER, Player.class);
		if (optPlayer.isPresent()) {
			this.onCollideImpactPlayer(world, event, entity, potions.get(), optPlayer.get());
		} else {
			this.onCollideImpactNatural(world, event, entity, potions.get());
		}
		
	}
		
	public void onCollideImpactPlayer(WorldGuardWorld world, CollideEvent.Impact event, ThrownPotion entity, List<PotionEffect> potions, Player player) {
		// Bypass
		if (this.plugin.getProtectionService().hasBypass(player)) return;
		
		for (PotionEffectType potion : potions.stream().map(potion -> potion.getType()).collect(Collectors.toSet())) {
			if (this.getDefault().containsValue(potion) && !world.getRegions(event.getImpactPoint().getPosition()).getFlag(player, event.getImpactPoint(), this).containsValue(potion)) {
				event.setCancelled(true);
				entity.remove();
				return;
			}
		}
	}
	
	
	public void onCollideImpactNatural(WorldGuardWorld world, CollideEvent.Impact event, ThrownPotion entity, List<PotionEffect> potions) {
		for (PotionEffectType potion : potions.stream().map(potion -> potion.getType()).collect(Collectors.toSet())) {
			if (this.getDefault().containsValue(potion) && !world.getRegions(event.getImpactPoint().getPosition()).getFlagDefault(this).containsValue(potion)) {
				event.setCancelled(true);
				entity.remove();
				return;
			}
		}
	}
}
