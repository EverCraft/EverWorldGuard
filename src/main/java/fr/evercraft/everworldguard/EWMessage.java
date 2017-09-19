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
package fr.evercraft.everworldguard;

import com.google.common.base.Preconditions;

import fr.evercraft.everapi.message.EMessageBuilder;
import fr.evercraft.everapi.message.EMessageFormat;
import fr.evercraft.everapi.message.format.EFormatString;
import fr.evercraft.everapi.plugin.file.EMessage;
import fr.evercraft.everapi.plugin.file.EnumMessage;
import fr.evercraft.everapi.services.worldguard.WorldGuardService.Priorities;

public class EWMessage extends EMessage<EverWorldGuard> {

	public EWMessage(final EverWorldGuard plugin) {
		super(plugin, EWMessages.values());
	}
	
	public enum EWMessages implements EnumMessage {
		PREFIX(										"[&4Ever&6&lWG&f] "),
		DESCRIPTION(								"Plugin de protection des régions"), 
		
		MIGRATE_DESCRIPTION(						"Transfère les données"),
		MIGRATE_SQL_CONFIRMATION(					"&7Souhaitez-vous vraiment transférer les données dans une base de données &6SQL&7 ? {confirmation}"),
		MIGRATE_SQL_CONFIRMATION_VALID(				"&2&n[Confirmer]"),
		MIGRATE_SQL_CONFIRMATION_VALID_HOVER(		"&cCliquez ici pour réaliser le transfert"),
		MIGRATE_CONF_CONFIRMATION(					"&7Souhaitez-vous vraiment transférer les données dans des &6fichiers de configuration&7 ? {confirmation}"),
		MIGRATE_CONF_CONFIRMATION_VALID(			"&2&n[Confirmer]"),
		MIGRATE_CONF_CONFIRMATION_VALID_HOVER(		"&cCliquez ici pour réaliser le transfert"),
		MIGRATE_SQL(								"&7Les données ont bien été transférées dans la base de données."),
		MIGRATE_SQL_LOG(							"Les données ont été transférées dans la base de données par {player}."),
		MIGRATE_CONF(								"&7Les données ont bien été transférées dans les fichiers de configurations."),
		MIGRATE_CONF_LOG(							"Les données ont été transférées dans les fichiers de configurations par {player}."),
		MIGRATE_DISABLE(							"&cErreur : Vous devez être connecté à une base de données pour faire le transfert des données."),
		
		CLEAR_DESCRIPTION(							"Supprimé toutes les régions d'un monde"),
		CLEAR_WORLD_CONFIRMATION(					"&7Souhaitez-vous vraiment supprimer tous les régions du monde &6{world} &7? {confirmation}"),
		CLEAR_WORLD_CONFIRMATION_VALID(				"&2&n[Confirmer]"),
		CLEAR_WORLD_CONFIRMATION_VALID_HOVER(		"&cCliquez ici pour valider la suppression"),
		CLEAR_WORLD_PLAYER(							"&7Tous les régions du monde {world} ont bien été supprimées."),
		CLEAR_WORLD_LOG(							"Tous les régions du monde {world} ont été supprimées par {player}."),
		CLEAR_ALL_CONFIRMATION(						"&7Souhaitez-vous vraiment supprimer tous les régions du serveur &7? {confirmation}"),
		CLEAR_ALL_CONFIRMATION_VALID(				"&2&n[Confirmer]"),
		CLEAR_ALL_CONFIRMATION_VALID_HOVER(			"&cCliquez ici pour valider la suppression"),
		CLEAR_ALL_PLAYER(							"&7Tous les régions du serveur ont bien été supprimées."),
		CLEAR_ALL_LOG(								"Tous les régions du serveur ont été supprimées par {player}."),
		
		GROUP_NOT_FOUND(							"&cErreur : Le group '&6{group}&c' est introuvable."), 
		GROUP_INCOMPATIBLE(							"&cErreur : Le group '&6{group}&c' est incompatible avec le flag {flag}."), 
		FLAG_NOT_FOUND(								"&cErreur : Le flag '&6{flag}&c' est introuvable."), 
		
		SELECT_DESCRIPTION(							"Permet de sélectionner une région"),
		
		SELECT_CUI_DESCRIPTION(						"Permet de voir les régions (Require : WorldEdit CUI)"),
		
		SELECT_INFO_DESCRIPTION(					"Affiche les informations sur la région sélectionnée"),
		SELECT_INFO_POS(							"&7(&6{x}&7, &6{y}&7, &6{z}&7)"),
		SELECT_INFO_POS_HOVER(						"&7X : &6{x}[RT]&7Y : &6{y}[RT]&7Z : &6{z}"),
		SELECT_INFO_CUBOID_TITLE(					"&7Votre sélection &6CUDOID&7"),
		SELECT_INFO_CUBOID_POS1_AND_POS2(			"    &6&l➤  &6Position 1 : &c{pos1}[RT]"
												  + "    &6&l➤  &6Position 2 : &c{pos2}[RT]"
												  + "    &6&l➤  &6Volume : &7{area}"),
		SELECT_INFO_CUBOID_POS1(					"    &6&l➤  &6Position 1 : &7{pos1}"),
		SELECT_INFO_CUBOID_POS2(					"    &6&l➤  &6Position 2 : &7{pos2}"),
		SELECT_INFO_CUBOID_EMPTY(					"    &cAucune position sélectionnée."),
		SELECT_INFO_EXTEND_TITLE(					"&7Votre sélection &6EXTEND&7"),
		SELECT_INFO_EXTEND_POS1_AND_POS2(			"    &6&l➤  &6Position 1 : &7{pos1}[RT]"
												  + "    &6&l➤  &6Position 2 : &7{pos2}[RT]"
												  + "    &6&l➤  &6Volume : &7{area}"),	
		SELECT_INFO_EXTEND_POS1(					"    &6&l➤  &6Position 1 : &7{pos1}"),
		SELECT_INFO_EXTEND_POS2(					"    &6&l➤  &6Position 2 : &7{pos2}"),
		SELECT_INFO_EXTEND_EMPTY(					"    &cAucune position sélectionnée."),
		SELECT_INFO_POLY_TITLE(						"&7Votre sélection &6POLYGONAL &7(&6{area}&7)"),
		SELECT_INFO_POLY_LINE(						"    &6&l➤  &6#{num} : &7{pos}"),
		SELECT_INFO_POLY_EMPTY(						"    Aucune position sélectionnée."),
		SELECT_INFO_CYLINDER_TITLE(					"&7Votre sélection &6CYLINDER&7"),
		SELECT_INFO_CYLINDER_CENTER_AND_RADIUS(		"    &6&l➤  &6Centre : &7{center}[RT]"
												  + "    &6&l➤  &6Longueur : &7{width}[RT]"
												  + "    &6&l➤  &6Hauteur : &7{height}[RT]"
												  + "    &6&l➤  &6Profondeur : &7{length}[RT]"
												  + "    &6&l➤  &6Volume : &7{area}"),
		SELECT_INFO_CYLINDER_CENTER(				"    &6&l➤  &6Centre : &7{center}"),
		SELECT_INFO_CYLINDER_EMPTY(					"&cAucune position sélectionnée."),
		SELECT_INFO_ELLIPSOID_TITLE(				"&7Votre sélection &6ELLIPSOID&7"),
		SELECT_INFO_ELLIPSOID_CENTER_AND_RADIUS(	"    &6&l➤  &6Centre : &7{center}[RT]"
												  + "    &6&l➤  &6Longueur : &7{width}[RT]"
												  + "    &6&l➤  &6Hauteur : &7{height}[RT]"
												  + "    &6&l➤  &6Profondeur : &7{length}[RT]"
												  + "    &6&l➤  &6Volume : &7{area}"),
		SELECT_INFO_ELLIPSOID_CENTER(				"    &6&l➤  &6Centre : &7{center}"),
		SELECT_INFO_ELLIPSOID_EMPTY(				"    Aucune position sélectionnée."),
		SELECT_INFO_SPHERE_TITLE(					"&7Votre sélection &6SPHERE&7"),
		SELECT_INFO_SPHERE_CENTER_AND_RADIUS(		"    &6&l➤  &6Centre : &7{center}[RT]"
												  + "    &6&l➤  &6Rayon : &7{radius}[RT]"
												  + "    &6&l➤  &6Volume : &7{area}"),
		SELECT_INFO_SPHERE_CENTER(					"    &6&l➤  &6Centre : &7{center}"),
		SELECT_INFO_SPHERE_EMPTY(					"    Aucune position sélectionnée."),
		
		SELECT_POS1_DESCRIPTION(					"Défini  la première position"),
		SELECT_POS1_ONE(							"&7Sélection de la position 1 : {position}."),
		SELECT_POS1_TWO(							"&7Sélection de la position 1 : {position} &7(&6{area}&7)."),
		SELECT_POS1_POLY(							"&7Sélection de la position #1 : {position}."),
		SELECT_POS1_CENTER(							"&7Sélection du centre : {position}."),
		SELECT_POS1_EQUALS(							"&cErreur : Vous avez déjà sélectionnée une position."),
		SELECT_POS1_CANCEL(							"&cErreur : Impossible de sélectionner une position pour le moment."),
		
		SELECT_POS2_DESCRIPTION(					"Défini  la deuxième position"),
		SELECT_POS2_ONE(							"&7Sélection de la position 2 : {position}."),
		SELECT_POS2_TWO(							"&7Sélection de la position 2 : {position} &7(&6{area}&7)."),
		SELECT_POS2_POLY_ONE(						"&7Ajout de la position &6#{num} &7: {position}."),
		SELECT_POS2_POLY_ALL(						"&7Ajout de la position &6#{num} &7: {position} &7(&6{area}&7)."),
		SELECT_POS2_POLY_ERROR(						"&cErreur : Vous avez déjà sélectionné le nombre maximum de position."),
		SELECT_POS2_RADIUS(							"&7Sélection d'un rayon de {radius} &7block(s) : {position}."),
		SELECT_POS2_NO_CENTER(						"&cErreur : Aucune position centrale n'est sélectionnée."),
		SELECT_POS2_EQUALS(							"&cErreur : Vous avez déjà sélectionné cette position."),
		SELECT_POS2_CANCEL(							"&cErreur : Impossible de sélectionner une position pour le moment."),
		
		SELECT_CLEAR_DESCRIPTION(					"Supprime toutes les positions sélectionnées"),
		SELECT_CLEAR_PLAYER(						"&7Vous n'avez plus aucune position."),
		
		SELECT_REMOVE_DESCRIPTION(					"Supprime la dernière  position sélectionnée d'un polygone"),
		SELECT_REMOVE_PLAYER(						"&7Vous avez supprimée la position : {pos}."),
		SELECT_REMOVE_EMPTY(						"&4Erreur : Vous n'avez aucune position sélectionnée."),
		SELECT_REMOVE_ERROR(						"&4Erreur : Uniquement pour le type &62D Polygonal&c."),
		
		
		SELECT_EXPAND_DESCRIPTION(					"Permet d'étendre la sélection"),
		SELECT_EXPAND_VERT(							"&7Vous avez étendu votre sélection de &6{size} &7block(s) [Bas-En-Haut]."),
		SELECT_EXPAND_DIRECTION(					"&7Vous avez étendu votre sélection de &6{size} &7block(s) [{amount} {direction}]."),
		SELECT_EXPAND_DIRECTION_OPPOSITE(			"&7Vous avez étendu votre sélection de &6{size} &7block(s) [{amount} {direction}] [{amount_opposite} {direction_opposite}]."),
		SELECT_EXPAND_ERROR_OPERATION(				"&4Erreur : Vous ne pouvez pas faire cette opération sur ce type de région."),
		SELECT_EXPAND_ERROR_NO_REGION(				"&4Erreur : Vous devez d'abord sélectionner une région."),
		
		SELECT_CONTRACT_DESCRIPTION(				"Permet de réduire la sélection"),
		SELECT_CONTRACT_DIRECTION(					"&7Vous avez réduit votre sélection de &6{size} &7block(s) [{amount} {direction}]."),
		SELECT_CONTRACT_DIRECTION_OPPOSITE(			"&7Vous avez réduit votre sélection de &6{size} &7block(s) [{amount} {direction}] [{amount_opposite} {direction_opposite}]."),
		SELECT_CONTRACT_ERROR_OPERATION(			"&4Erreur : Vous ne pouvez pas faire cette opération sur ce type de région."),
		SELECT_CONTRACT_ERROR_NO_REGION(			"&4Erreur : Vous devez d'abord sélectionner une région."),
		
		SELECT_SHIFT_DESCRIPTION(					"Permet de déplacer la sélection"),
		SELECT_SHIFT_DIRECTION(						"&7Vous avez déplacé votre sélection de &6{amount} &7block(s) vers la &6{direction}&7."),
		SELECT_SHIFT_ERROR_OPERATION(				"&4Erreur : Vous ne pouvez pas faire cette opération sur ce type de région."),
		SELECT_SHIFT_ERROR_NO_REGION(				"&4Erreur : Vous devez d'abord sélectionner une région."),
		
		SELECT_TYPE_DESCRIPTION(					"&7Change le type de sélection"),
		
		SELECT_TYPE_CUBOID(							"&7Cuboid : Clic gauche pour définir le point N°1 et clic droit pour définir le point N°2.",
													"&7Cuboid : left click for point 1, right for point 2."),
		SELECT_TYPE_EXTEND(							"&7Extend : Clic gauche pour définir le point N°1 et clic droit pour définir le point N°2.",
													"&7Extend : left click for point 1, right for point 2."),
		SELECT_TYPE_POLYGONAL(						"&72D Polygonal : Clic gauche pour définir le premier point et clic droit pour définir les points suivants.",
													"&72D polygon selector: Left/right click to add a point."),
		SELECT_TYPE_CYLINDER(						"&7Cylindrique : Clic gauche pour définir le centre, clic droit pour définir le rayon.",
													"&7Cylindrical select: Left click=center, right click to extend."),
		SELECT_TYPE_ELLIPSOID(						"&7Ellipsoid : Clic gauche pour définir le centre, clic droit pour définir les formes.",
													"&7Ellipsoid select: Left click=center, right click to extend."),
		SELECT_TYPE_SPHERE(							"&7Sphere : Clic gauche pour définir le centre, clic droit pour définir les formes.",
													"&7Sphere select: Left click=center, right click to extend."),
		SELECT_TYPE_EQUALS(							"&cErreur : Sélection &6{type} &cdéjà activée"),
		SELECT_TYPE_CANCEL(							"&cErreur : Impossible de changer de type de sélection pour le moment."),
		
		REGION_DESCRIPTION(							"Gère les régions protégées"),
		REGION_NO_PERMISSION(						"&cErreur : Vous n'avez pas la permission pour cette région &6{region}&c."),
		
		REGION_LOAD_DESCRIPTION(					"Rechargement des régions d'un monde"), 
		REGION_LOAD_MESSAGE(						"&7Rechargement du monde &6{world}&7."), 
			
		REGION_BYPASS_DESCRIPTION(					"Active/Désactive le mode admin"),
		
		REGION_BYPASS_ON_PLAYER(					"&7Vous avez activé le mode admin."),
		REGION_BYPASS_ON_PLAYER_ERROR(				"&cErreur : Le mode admin est déjà activé."),
		REGION_BYPASS_ON_OTHERS_PLAYER(				"&7Le mode admin est désormais activé grâce à &6{staff}&7."),
		REGION_BYPASS_ON_OTHERS_STAFF(				"&7Vous avez activé le mode admin pour &6{player}&7."),
		REGION_BYPASS_ON_OTHERS_ERROR(				"&cErreur : Le mode admin de &6{player} &cest déjà activé."),
		
		REGION_BYPASS_OFF_PLAYER(					"&7Vous avez désactivé le mode admin."),
		REGION_BYPASS_OFF_PLAYER_ERROR(				"&cErreur : Le mode admin est déjà désactivé."),
		REGION_BYPASS_OFF_OTHERS_PLAYER(			"&7Le mode admin est désormais désactivé à cause de &6{staff}&7."),
		REGION_BYPASS_OFF_OTHERS_STAFF(				"&7Vous avez désactivé le mode admin pour &6{player}&7."),
		REGION_BYPASS_OFF_OTHERS_ERROR(				"&7Erreur : Le mode admin de &6{player} &cest déjà désactivé."),
		
		REGION_BYPASS_STATUS_PLAYER_ON(				"&7Le mode admin est activé."),
		REGION_BYPASS_STATUS_PLAYER_OFF(			"&7Le mode admin est désactivé.."),
		REGION_BYPASS_STATUS_OTHERS_ON(				"&7Le mode admin de &6{player} &7est activé."),
		REGION_BYPASS_STATUS_OTHERS_OFF(			"&7Le mode admin de &6{player} &7est désactivé."),
		
		REGION_CHECK_DESCRIPTION(					"Permet de savoir la valeur de chaque flag"),
		
		REGION_CHECK_GROUP_TITLE(					"&aListe des flags : &6{group}"),
		REGION_CHECK_GROUP_LINE(					"        &a&l- {flag} : &c{value} &7(Région : &a{region}&7)"),
		REGION_CHECK_GROUP_LINE_DEFAULT(			"        &a&l- {flag} : &c{value}"),
		
		REGION_CHECK_FLAG_TITLE(					"&aListe des valeurs : &6{flag}"),
		REGION_CHECK_FLAG_DEFAULT(					"    &6&l➤   &6Default : &c{value} &7(Région : &a{region}&7)"),
		REGION_CHECK_FLAG_DEFAULT_DEFAULT(			"    &6&l➤   &6Default : &c{value}"),
		REGION_CHECK_FLAG_MEMBER(					"    &6&l➤   &6Member : &c{value} &7(Région : &a{region}&7)"),
		REGION_CHECK_FLAG_MEMBER_DEFAULT(			"    &6&l➤   &6Member : &c{value}"),
		REGION_CHECK_FLAG_OWNER(					"    &6&l➤   &6Owner : &c{value} &7(Région : &a{region}&7)"),
		REGION_CHECK_FLAG_OWNER_DEFAULT(			"    &6&l➤   &6Owner : &c{value}"),
		
		REGION_INFO_DESCRIPTION(					"Permet de voir la liste des régions sur votre position"),
		REGION_INFO_ONE_TITLE(						"&aRégion Info : &6{region}"),
		REGION_INFO_ONE_WORLD(						"    &6&l➤  &6World : &c{world}"),
		REGION_INFO_ONE_TYPE(						"    &6&l➤  &6Type : &c{type}"),
		REGION_INFO_ONE_POINTS(						"    &6&l➤  &6Points : &c{positions}"),
		REGION_INFO_ONE_POINTS_CUBOID(				"&6(&c{min_x}&6, &c{min_y}&6, &c{min_z}&6) (&c{max_x}&6, &c{max_y}&6, &c{max_z}&6)"),
		REGION_INFO_ONE_POINTS_CUBOID_HOVER(		"&6Min : (&c{min_x}&6, &c{min_y}&6, &c{min_z}&6)[RT]&6Max : (&c{max_x}&6, &c{max_y}&6, &c{max_z}&6)"),
		REGION_INFO_ONE_POINTS_POLYGONAL(			"&6(&c{min_x}&6, &c{min_y}&6, &c{min_z}&6) (&c{max_x}&6, &c{max_y}&6, &c{max_z}&6)"),
		REGION_INFO_ONE_POINTS_POLYGONAL_HOVER(		"&6Les positions : [RT]{positions}"),
		REGION_INFO_ONE_POINTS_POLYGONAL_HOVER_POSITIONS("&6#{num} : (&c{x}&6, &c{y}&6, &c{z}&6)"),
		REGION_INFO_ONE_POINTS_POLYGONAL_HOVER_JOIN("[RT]"),
		REGION_INFO_ONE_PRIORITY(					"    &6&l➤  &6Priorité : &c{prority}"),
		REGION_INFO_ONE_PARENT(						"    &6&l➤  &6Parent : &c{parent}"),
		REGION_INFO_ONE_HERITAGE(					"    &6&l➤  &6Héritage :"),
		REGION_INFO_ONE_HERITAGE_LINE(				"        &c└ {region} : &7{type}"),
		REGION_INFO_ONE_HERITAGE_PADDING(			"  "),
		REGION_INFO_ONE_OWNERS(						"    &6&l➤  &6Owners : &c{owners}"),
		REGION_INFO_ONE_OWNERS_JOIN(				"&6, &c"),
		REGION_INFO_ONE_GROUP_OWNERS(				"    &6&l➤  &6Groupes Owners : &c{owners}"),
		REGION_INFO_ONE_GROUP_OWNERS_JOIN(			"&6, &c"),
		REGION_INFO_ONE_MEMBERS(					"    &6&l➤  &6Members : &c{members}"),
		REGION_INFO_ONE_MEMBERS_JOIN(				"&6, &c"),
		REGION_INFO_ONE_GROUP_MEMBERS(				"    &6&l➤  &6Groupes Members : &c{members}"),
		REGION_INFO_ONE_GROUP_MEMBERS_JOIN(			"&6, &c"),
		REGION_INFO_ONE_FLAGS(						"    &6&l➤  &6Flag :"),
		REGION_INFO_ONE_FLAGS_LINE(					"            &a&l- {flag} : &c{value}"),
		REGION_INFO_ONE_FLAGS_DEFAULT(				"        &6&l●   &6Default:"),
		REGION_INFO_ONE_FLAGS_MEMBER(				"        &6&l●   &6Member:"),
		REGION_INFO_ONE_FLAGS_OWNER(				"        &6&l●   &6Owner:"),
		REGION_INFO_ONE_HERITAGE_FLAGS(				"    &6&l➤  Flag Héritage :"),
		REGION_INFO_ONE_HERITAGE_FLAGS_LINE(		"            &a&l- {flag} : &c{value}"),
		REGION_INFO_ONE_HERITAGE_FLAGS_DEFAULT(		"        &6&l●   &6Default:"),
		REGION_INFO_ONE_HERITAGE_FLAGS_MEMBER(		"        &6&l●   &6Member:"),
		REGION_INFO_ONE_HERITAGE_FLAGS_OWNER(		"        &6&l●   &6Owner:"),
		REGION_INFO_LIST_TITLE(						"&aListe des régions"),
		REGION_INFO_LIST_LINE(						"    &6&l➤  &6{region} : [RT]            &7(Type : &a{type}&7, Priorité : &a{priority}&7)"),
		REGION_INFO_EMPTY(							"&cErreur : Vous n'avez accès aux informations sur ces régions."),
		
		REGION_LIST_DESCRIPTION(					"Permet de voir la liste des régions dans le monde"),
		REGION_LIST_ALL_TITLE(						"&aLa liste des régions : &6{world}"),
		REGION_LIST_ALL_LINE(						"    &6&l➤  &6{region} : [RT]            &7(Type : &a{type}&7, Priorité : &a{priority}&7)"),
		REGION_LIST_ALL_EMPTY(						"    &7Aucune région"),
		REGION_LIST_PLAYER_TITLE_EQUALS(			"&aLa liste de vos régions : &6{world}"),
		REGION_LIST_PLAYER_TITLE_OTHERS(			"&aListe des régions : &6{world}"),
		REGION_LIST_PLAYER_LINE(					"    &6&l➤  &6{region} : [RT]            &7(Type : &a{type}&7, Priorité : &a{priority}&7)"),
		REGION_LIST_PLAYER_EMPTY(					"    &7Aucune région"),
		REGION_LIST_GROUP_TITLE(					"&aLa liste des régions du groupe &6{group} &7: &6{world}"),
		REGION_LIST_GROUP_LINE(						"    &6&l➤  &6{region} : [RT]            &7(Type : &a{type}&7, Priorité : &a{priority}&7)"),
		REGION_LIST_GROUP_EMPTY(					"    &7Aucune région"),
		
		REGION_DEFINE_DESCRIPTION(					"Permet de définir une nouvelle région"),
		REGION_DEFINE_CUBOID_CREATE(				"&7Vous venez de créer la région &6{points} &7de type &6{type}&7."),
		REGION_DEFINE_CUBOID_POINTS(				"&6{region}"),
		REGION_DEFINE_CUBOID_POINTS_HOVER(			"&6Min : (&c{min_x}&6, &c{min_y}&6, &c{min_z}&6)[RT]&6Max : (&c{max_x}&6, &c{max_y}&6, &c{max_z}&6)"),
		REGION_DEFINE_CUBOID_ERROR_POSITION(		"&cErreur : Vous devez sélectionner 2 positions pour définir une région &6{type}&c."),
		REGION_DEFINE_POLYGONAL_CREATE(				"&7Vous venez de créer la région &6{points} &7de type &6{type}&7."),
		REGION_DEFINE_POLYGONAL_POINTS(				"&6{region}"),
		REGION_DEFINE_POLYGONAL_POINTS_HOVER(		"&6Les positions : [RT]{positions}"),
		REGION_DEFINE_POLYGONAL_POINTS_HOVER_LINE(	"&6#{num} : (&c{x}&6, &c{y}&6, &c{z}&6)"),
		REGION_DEFINE_POLYGONAL_POINTS_HOVER_JOIN(	"[RT]"),
		REGION_DEFINE_POLYGONAL_ERROR_POSITION(		"&cErreur : Vous devez sélectionner au moins 2 positions pour définir une région &6{type}&c."),
		REGION_DEFINE_TEMPLATE_CREATE(				"&7Vous venez de créer la région &6{region} &7de type &6{type}&7."),
		REGION_DEFINE_ERROR_IDENTIFIER_EQUALS(		"&cErreur : &6{region} &cexiste déjà."),
		REGION_DEFINE_ERROR_IDENTIFIER_INVALID(		"&cErreur : Le nom de région &6'{region}' &cest invalide."),
		REGION_DEFINE_ERROR_SELECT_TYPE(			"&cErreur : Impossible de créer une région de type &6{type}&c."),
		
		REGION_REDEFINE_DESCRIPTION(				"Permet de redéfinir une région"),
		REGION_REDEFINE_CUBOID_CREATE(				"&7Vous venez de redéfinir la région &6{region} &7en type &6{type}&7."),
		REGION_REDEFINE_CUBOID_POINTS(				"&6(&c{min_x}&6, &c{min_y}&6, &c{min_z}&6) (&c{max_x}&6, &c{max_y}&6, &c{max_z}&6)"),
		REGION_REDEFINE_CUBOID_POINTS_HOVER(		"&6Min : (&c{min_x}&6, &c{min_y}&6, &c{min_z}&6)[RT]&6Max : (&c{max_x}&6, &c{max_y}&6, &c{max_z}&6)"),
		REGION_REDEFINE_CUBOID_ERROR_POSITION(		"&cErreur : Vous devez sélectionner 2 positions pour redéfinir la région &6{region}&c."),
		REGION_REDEFINE_POLYGONAL_CREATE(			"&7Vous venez de redéfinir la région &6{region} &7de type &6{type}."),
		REGION_REDEFINE_POLYGONAL_POINTS(			"&6(&c{min_x}&6, &c{min_y}&6, &c{min_z}&6) (&c{max_x}&6, &c{max_y}&6, &c{max_z}&6)"),
		REGION_REDEFINE_POLYGONAL_POINTS_HOVER(		"&6Les positions : [RT]<positions"),
		REGION_REDEFINE_POLYGONAL_POINTS_HOVER_LINE("&6#{num} : (&c{x}&6, &c{y}&6, &c{z}&6)"),
		REGION_REDEFINE_POLYGONAL_POINTS_HOVER_JOIN("[RT]"),
		REGION_REDEFINE_POLYGONAL_ERROR_POSITION(	"&cErreur : Vous devez sélectionner au moins 3 positions pour redéfinir la région &6{region}&c."),
		REGION_REDEFINE_TEMPLATE_CREATE(			"&7Vous venez de redéfinir la région &6{region} &7de type &6{type}."),
		REGION_REDEFINE_ERROR_GLOBAL(				"&cErreur : Impossible de redéfinir la région &6{region} &ccar elle est de type &6{type}&c."),
		REGION_REDEFINE_ERROR_SELECT_TYPE(			"&cErreur : Impossible de redéfinir une région en type &6{type}&c."),
		
		REGION_RENAME_DESCRIPTION(					"Renomme une région"),
		REGION_RENAME_SET(							"&7Vous avez renommé la région &6{region} &7en &6{identifier} &7dans le monde &6{world}&7."),
		REGION_RENAME_ERROR_IDENTIFIER_EQUALS(		"&cErreur : Impossible de renommer la région &6{region} &cen &6{identifier} &ccar une autre région porte déjà ce nom."),
		REGION_RENAME_ERROR_IDENTIFIER_INVALID(		"&cErreur : Impossible de renommer la région &6{region} &cen &6{identifier} &ccar le nom est invalide."),
		REGION_RENAME_ERROR_GLOBAL(					"&cErreur : Impossible de renommer la région &6{region} &cde type &6{type}&c."),
		
		REGION_FLAGS_DESCRIPTION(					"Affiche la liste des flags"),
		REGION_FLAGS_LIST_TITLE(					"&aListe des flags"),
		REGION_FLAGS_LIST_LINE(						"    &6&l➤  &6{flag} : &7{description}"),
		REGION_FLAGS_MESSAGE(						"&6{flag} : &7{description}"),
		
		REGION_FLAG_ADD_DESCRIPTION(				"Défini un flag d'une région"),
		REGION_FLAG_ADD_PLAYER(						"&7Vous avez défini le flag &6{flag} &7sur la région &6{region} &7dans le monde &6{world} &7pour le groupe &6{group} &7avec la valeur &6{value}&7."),
		REGION_FLAG_ADD_ERROR(						"&cErreur : La valeur est &6'{value}&6' &cest invalide."),
		
		REGION_FLAG_REMOVE_DESCRIPTION(				"Supprime un flag d'une région"),
		REGION_FLAG_REMOVE_UPDATE(					"&7Vous avez défini le flag &6{flag} &7sur la région &6{region} &7dans le monde &6{world} &7pour le groupe &6{group} &7avec la valeur &6{value}&7."),
		REGION_FLAG_REMOVE_PLAYER(					"&7Vous avez supprimé le flag &6{flag} &7sur la région &6{region} &7dans le monde &6{world} &7pour le groupe &6{group}&7."),
		REGION_FLAG_REMOVE_EMPTY(					"&cErreur : Il n'y a pas de flag &6{flag} &csur la région &6{region} &cdans le monde &6{world} &cpour le groupe &6{group}&c."),
		REGION_FLAG_REMOVE_ERROR(					"&cErreur : La valeur est &6'{value}&6' &cest invalide."),
		
		REGION_OWNER_ADD_DESCRIPTION(				"Ajoute un owner à une région"),
		REGION_OWNER_ADD_PLAYER(					"&7Vous avez ajouté le joueur &6{player} &7en tant que &6OWNER &7de la région &6{region}&7."),
		REGION_OWNER_ADD_PLAYER_ERROR(				"&cErreur : Le joueur &6{player} &cest déjà &6OWNER &cde la région &6{region}&c."),
		REGION_OWNER_ADD_PLAYERS(					"&7Vous avez ajouté les joueur(s) &6{players} &7en tant que &6OWNER &7de la région &6{region}&7."),
		REGION_OWNER_ADD_PLAYERS_JOIN(				"&7, &6"),
		REGION_OWNER_ADD_ERROR_MAX(					"&cErreur : La région &6{region} &ccontient déjà le nombre maximum de &6OWNER&c."),
		REGION_OWNER_ADD_GROUP(						"&7Vous avez ajouté le groupe &6{group} &7en tant que &6OWNER &7de la région &6{region}&7."),
		REGION_OWNER_ADD_GROUP_ERROR(				"&cErreur : Le groupe &6{group} &cest déjà &6OWNER &cde la région &6{region}&c."),
		REGION_OWNER_ADD_GROUPS(					"&7Vous avez ajouté les groupes &6{groups} &7en tant que &6OWNER &7de la région &6{region}&7."),
		REGION_OWNER_ADD_GROUPS_JOIN(				"&7, &6"),
		
		REGION_OWNER_REMOVE_DESCRIPTION(			"Supprime un owner à une région"),
		REGION_OWNER_REMOVE_PLAYER(					"&7Vous avez supprimé le joueur &6{player} &7en tant que &6OWNER &7de la région &6{region}&7."),
		REGION_OWNER_REMOVE_PLAYER_ERROR(			"&cErreur : Le joueur &6{player} &cn'est pas &6OWNER &cde la région &6{region}&c."),
		REGION_OWNER_REMOVE_PLAYERS(				"&7Vous avez supprimé les joueur(s) &6{players} &7en tant que &6OWNER &7de la région &6{region}&7."),
		REGION_OWNER_REMOVE_PLAYERS_JOIN(			"&7, &6"),
		REGION_OWNER_REMOVE_GROUP(					"&7Vous avez supprimé le groupe &6{group} &7en tant que &6OWNER &7de la région &6{region}&7."),
		REGION_OWNER_REMOVE_GROUP_ERROR(			"&cErreur : Le groupe &6{group} &cn'est pas &6OWNER &cde la région &6{region}&c."),
		REGION_OWNER_REMOVE_GROUPS(					"&7Vous avez supprimé les groupes &6{groups} &7en tant que &6OWNER &7de la région &6{region}&7."),
		REGION_OWNER_REMOVE_GROUPS_JOIN(			"&7, &6"),

		REGION_MEMBER_ADD_DESCRIPTION(				"Ajoute un member à une région"),
		REGION_MEMBER_ADD_PLAYER(					"&7Vous avez ajouté le joueur &6{player} &7en tant que &6MEMBER &7de la région &6{region}&7."),
		REGION_MEMBER_ADD_PLAYER_ERROR(				"&cErreur : Le joueur &6{player} &cest déjà &6MEMBER &cde la région &6{region}&c."),
		REGION_MEMBER_ADD_PLAYERS(					"&7Vous avez ajouté les joueur(s) &6{players} &7en tant que &6MEMBER &7de la région &6{region}&7."),
		REGION_MEMBER_ADD_PLAYERS_JOIN(				"&7, &6"),
		REGION_MEMBER_ADD_ERROR_MAX(				"&cErreur : La région &6{region} &ccontient déjà le nombre maximum de &6MEMBER&c."),
		REGION_MEMBER_ADD_GROUP(					"&7Vous avez ajouté le groupe &6{group} &7en tant que &6MEMBER &7de la région &6{region}&7."),
		REGION_MEMBER_ADD_GROUP_ERROR(				"&cErreur : Le groupe &6{group} &cest déjà &6MEMBER &cde la région &6{region}&c."),
		REGION_MEMBER_ADD_GROUPS(					"&7Vous avez ajouté les groupes &6{groups} &7en tant que &6MEMBER &7de la région &6{region}&7."),
		REGION_MEMBER_ADD_GROUPS_JOIN(				"&7, &6"),
		
		REGION_MEMBER_REMOVE_DESCRIPTION(			"Supprime un member à une région"),
		REGION_MEMBER_REMOVE_PLAYER(				"&7Vous avez supprimé le joueur &6{player} &7en tant que &6MEMBER &7de la région &6{region}&7."),
		REGION_MEMBER_REMOVE_PLAYER_ERROR(			"&cErreur : Le joueur &6{player} &cn'est pas &6MEMBER &cde la région &6{region}&c."),
		REGION_MEMBER_REMOVE_PLAYERS(				"&7Vous avez supprimé les joueur(s) &6{players} &7en tant que &6MEMBER &7de la région &6{region}&7."),
		REGION_MEMBER_REMOVE_PLAYERS_JOIN(			"&7, &6"),
		REGION_MEMBER_REMOVE_GROUP(					"&7Vous avez supprimé le groupe &6{group} &7en tant que &6MEMBER &7de la région &6{region}&7."),
		REGION_MEMBER_REMOVE_GROUP_ERROR(			"&cErreur : Le groupe &6{group} &cn'est pas &6MEMBER &cde la région &6{region}&c."),
		REGION_MEMBER_REMOVE_GROUPS(				"&7Vous avez supprimé les groupes &6{groups} &7en tant que &6MEMBER &7de la région &6{region}&7."),
		REGION_MEMBER_REMOVE_GROUPS_JOIN(			"&7, &6"),
		
		REGION_PARENT_DESCRIPTION(					"Défini le parent d'une région"),
		REGION_PARENT_SET(							"&7Vous avez défini la région &6{parent} &7en tant que parent de la région &6{region}&7."),
		REGION_PARENT_SET_HERITAGE(					"&7Vous avez défini la région &6{parent} &7en tant que parent de la région &6{region} : [RT]{heritage}"),
		REGION_PARENT_SET_HERITAGE_LINE(			"    &c└ {region} : &7{type}"),
		REGION_PARENT_SET_HERITAGE_PADDING(			"  "),
		REGION_PARENT_SET_CIRCULAR(					"&cErreur : Impossible de définir la région &6{parent} &cen tant que parent de &6{region}&c."),
		REGION_PARENT_SET_EQUALS(					"&cErreur : Impossible de définir une région parent."),
		REGION_PARENT_SET_EQUALS_PARENT(			"&cErreur : La région &6{parent} &cest déjà la région parent de &6{region}&c."),
		REGION_PARENT_REMOVE(						"&7Vous avez supprimé le parent de la région &6{region}&7."),
		REGION_PARENT_REMOVE_EMPTY(					"&cErreur : La région &6{region} &cn'a déjà aucun parent."),
		
		REGION_PRIORITY_DESCRIPTION(				"Défini la priorité d'une région"),
		REGION_PRIORITY_SET(						"&7Vous avez défini la priorité de la région &6{region} &7à &6{priority}&7."),
		
		REGION_REMOVE_DESCRIPTION(					"Supprime une région"),
		REGION_REMOVE_REGION(						"&7Vous avez supprimé la région &6{region} &7dans le monde &6{world}&7."),
		REGION_REMOVE_CHILDREN_REMOVE(				"&7Vous avez supprimé la région &6{region} &7et ces enfants &7dans le monde &6{world}&7."),
		REGION_REMOVE_CHILDREN_UNSET(				"&7Vous avez supprimé la région &6{region} &7et gardé ces enfants &7dans le monde &6{world}&7."),
		REGION_REMOVE_ERROR_GLOBAL(					"&cErreur : Impossible de supprimer la région &6{region} &ccar elle est de type &6{type}&c."),
		REGION_REMOVE_ERROR_CHILDREN(				"&cErreur : La région &6{region} &cpossède au moins une région enfant :[RT]"
												  + "    -f : Permet de supprimer aussi les régions enfants[RT]"
												  + "    -u : Permet de supprimer uniquement la région parent"),
		
		REGION_TELEPORT_DESCRIPTION(				"Téléporte le joueur à la région séléctionnée"),
		REGION_TELEPORT_TELEPORT(					"&7Vous avez été {position} &7à la région &6{region}&7."),
		REGION_TELEPORT_TELEPORT_POSITION(			"&6téléporté"),
		REGION_TELEPORT_TELEPORT_POSITION_HOVER(	"&7World : &6{world}[RT]&7X : &6{x}[RT]&7Y : &6{y}[RT]&7Z : &6{z}"),
		REGION_TELEPORT_TELEPORT_ERROR(				"&cErreur : La position de téléportation de la région &6{region} &cest invalide."),
		REGION_TELEPORT_SPAWN(						"&7Vous avez été téléporté {position} &7de la région &6{region}&7."),
		REGION_TELEPORT_SPAWN_POSITION(				"&6spawn"),
		REGION_TELEPORT_SPAWN_POSITION_HOVER(		"&7World : &6{world}[RT]&7X : &6{x}[RT]&7Y : &6{y}[RT]&7Z : &6{z}"),
		REGION_TELEPORT_SPAWN_ERROR(				"&cErreur : La position du spawn de la région &6{region} &cest invalide."),
		REGION_TELEPORT_SPAWN_EMPTY(				"&cErreur : La région &6{region} &cn'a aucun spawn."),
		
		REGION_SELECT_DESCRIPTION(					"Permet de sélectionner une région"),
		REGION_SELECT_CUBOID(						"&7Vous venez de sélectionner la région &6{positions} &7de type &6{type}&7."),
		REGION_SELECT_CUBOID_POINTS(				"&6{region}"),
		REGION_SELECT_CUBOID_POINTS_HOVER(			"&6Min : (&c{min_x}&6, &c{min_y}&6, &c{min_z}&6)[RT]&6Max : (&c{max_x}&6, &c{max_y}&6, &c{max_z}&6)"),
		REGION_SELECT_POLYGONAL(					"&7Vous venez de sélectionner la région &6{positions} &7de type &6{type}&7."),
		REGION_SELECT_POLYGONAL_POINTS(				"&6{region}"),
		REGION_SELECT_POLYGONAL_POINTS_HOVER(		"&6Les positions : [RT]{positions}"),
		REGION_SELECT_POLYGONAL_POINTS_HOVER_LINE(	"&6#{num} : (&c{x}&6, &c{y}&6, &c{z}&6)"),
		REGION_SELECT_POLYGONAL_POINTS_HOVER_JOIN(	"[RT]"),
		REGION_SELECT_GLOBAL(						"&cErreur : Impossible de sélectionner une région de type &6{type}&c."),
		REGION_SELECT_TEMPLATE(						"&cErreur : Impossible de sélectionner une région de type &6{type}&c."),
		
		
		FLAG_BLOCK_PLACE_DESCRIPTION(				"Autorise/Interdit de placer des blocs"),
		FLAG_BLOCK_PLACE_MESSAGE(					EMessageFormat.builder()
															.actionbarMessageString("&cPlacer des blocs de {block} est interdit ({x}, {y}, {z})")
															.actionbarStay(3 * 1000)
															.actionbarPriority(Priorities.FLAG)),
		
		FLAG_BLOCK_BREAK_DESCRIPTION(				"Autorise/Interdit de détruire des blocs"),
		FLAG_BLOCK_BREAK_MESSAGE(					EMessageFormat.builder()
															.actionbarMessageString("&cCasser des blocs de {block} est interdit ({x}, {y}, {z})")
															.actionbarStay(3 * 1000)
															.actionbarPriority(Priorities.FLAG)),
		
		FLAG_BUILD_DESCRIPTION(						"Autorise/Interdit les constructions"),
		FLAG_BUILD_MESSAGE(							EMessageFormat.builder()
															.actionbarMessageString("&cConstruction désactivée ({x}, {y}, {z})")
															.actionbarStay(3 * 1000)
															.actionbarPriority(Priorities.FLAG)),
				
		FLAG_CHAT_DESCRIPTION(						"Active/Désactive le chat"),
		FLAG_CHAT_SEND_MESSAGE(						"&cL'envoi de message est désactivé dans cette région ({x}, {y}, {z})"),
		
		FLAG_COMMAND_DESCRIPTION(					"Active/Désactive l'execution des commandes"),
		FLAG_COMMAND_MESSAGE(						"&cLa commande {command} est désactivé dans cette région ({x}, {y}, {z})"),
		
		FLAG_DAMAGE_ENTITY_DESCRIPTION(				"Active/Désactive les dégats infligés aux entités"),
		FLAG_DAMAGE_ENTITY_MESSAGE(					EMessageFormat.builder()
															.actionbarMessageString("&cLes dégats infligés aux entités sont désactivé ({x}, {y}, {z})")
															.actionbarStay(3 * 1000)
															.actionbarPriority(Priorities.FLAG)),
		
		FLAG_ENDERDRAGON_GRIEF_DESCRIPTION(			"Active/Désactive la destruction des blocs par l'EnderDragon"),
		
		FLAG_ENDERPEARL_DESCRIPTION(				"Active/Désactive la téléportation avec une enderperle"),
		FLAG_ENDERPEARL_MESSAGE(					EMessageFormat.builder()
															.actionbarMessageString("&cL'utilisation des enderperles est désactivé ({x}, {y}, {z})")
															.actionbarStay(3 * 1000)
															.actionbarPriority(Priorities.FLAG)),
		
		FLAG_ENTITY_DAMAGE_DESCRIPTION(				"Active/Désactive les dégats infligés par une entité"),
		
		FLAG_ENDERMAN_GRIEF_DESCRIPTION(			"Active/Désactive la destruction des blocs de l'Enderman"),
			
		FLAG_INTERACT_BLOCK_DESCRIPTION(			"Active/Désactive l'interaction avec les blocs"),
		FLAG_INTERACT_BLOCK_MESSAGE(				EMessageFormat.builder()
															.actionbarMessageString("&cL'Interaction avec les blocs est désactivé ({x}, {y}, {z})")
															.actionbarStay(3 * 1000)
															.actionbarPriority(Priorities.FLAG)),
		
		FLAG_INTERACT_ENTITY_DESCRIPTION(			"Active/Désactive les interactions avec les entités"),
		FLAG_INTERACT_ENTITY_MESSAGE(				EMessageFormat.builder()
															.actionbarMessageString("&cL'Interaction avec les entités est désactivé ({x}, {y}, {z})")
															.actionbarStay(3 * 1000)
															.actionbarPriority(Priorities.FLAG)),
		
		FLAG_ENTRY_DESCRIPTION(						"Autorise/Interdit le joueur d'entrer dans la région"),
		
		FLAG_ENTRY_MESSAGE_DESCRIPTION(				"Autorise/Interdit le joueur d'entrer dans la région"),
		
		FLAG_ENTRY_DENY_MESSAGE_DESCRIPTION(		"Interdit le joueur d'entrer dans la région"),
		FLAG_ENTRY_DENY_MESSAGE_DEFAULT(			EMessageFormat.builder()
															.actionbarMessageString("&cImpossible d'entrer dans la région ({region})")
															.actionbarStay(3 * 1000)
															.actionbarPriority(Priorities.FLAG)),
		
		FLAG_EXIT_DESCRIPTION(						"Autorise/Interdit le joueur de sortir dans la région"),
		
		FLAG_EXIT_MESSAGE_DESCRIPTION(				"Autorise/Interdit le joueur de sortir dans la région"),
		
		FLAG_EXIT_DENY_MESSAGE_DESCRIPTION(			"Interdit le joueur de sortir dans la région"),
		FLAG_EXIT_DENY_MESSAGE_DEFAULT(				EMessageFormat.builder()
															.actionbarMessageString("&cImpossible de sortir dans la région ({region})")
															.actionbarStay(3 * 1000)
															.actionbarPriority(Priorities.FLAG)),
		
		FLAG_EXP_DROP_DESCRIPTION(					"Active/Désactive la perte d'expérience au sol"),
		
		FLAG_EXPLOSION_DESCRIPTION(					"Active/Désactive les explosions dans la région"),
		
		FLAG_EXPLOSION_BLOCK_DESCRIPTION(			"Active/Désactive la destruction des blocs lors d'une explosion"),
		
		FLAG_EXPLOSION_DAMAGE_DESCRIPTION(			"Active/Désactive les dégats lors d'une explosion"),
		
		FLAG_FIRE_DESCRIPTION(						"Active/Désactive le fait d'allumer un feu"),
		
		FLAG_FIRE_MESSAGE(							EMessageFormat.builder()
															.actionbarMessageString("&cLe feu est désactivé ({x}, {y}, {z})")
															.actionbarStay(3 * 1000)
															.actionbarPriority(Priorities.FLAG)),
		
		FLAG_ICE_DESCRIPTION(						"Active/Désactive la formation de glace"),
		
		FLAG_KEEP_INVENTORY_DESCRIPTION(			"Active/Désactive la perte de l'inventaire"),
		
		FLAG_INVINCIBILITY_DESCRIPTION(				"Active/Désactive l'invincibilité"),
		
		FLAG_ITEM_DROP_DESCRIPTION(					"Active/Désactive le fait de jeter des objets"),
		FLAG_ITEM_DROP_MESSAGE(						EMessageFormat.builder()
															.actionbarMessageString("&cJeter les objets de {item} est interdit ({x}, {y}, {z})")
															.actionbarStay(3 * 1000)
															.actionbarPriority(Priorities.FLAG)),
		
		FLAG_ITEM_PICKUP_DESCRIPTION(				"Active/Désactive le fait de rammasser des objets"),
		FLAG_ITEM_PICKUP_MESSAGE(					EMessageFormat.builder()
															.actionbarMessageString("&cRamasser les objets de {item} est interdit ({x}, {y}, {z})")
															.actionbarStay(3 * 1000)
															.actionbarPriority(Priorities.FLAG)),
		
		
		FLAG_LIGHTNING_DESCRIPTION(					"Active/Désactive la foudre"),
		
		FLAG_POTION_SPLASH_DESCRIPTION(				"Active/Désactive les potions splash"),
		FLAG_POTION_SPLASH_MESSAGE(					EMessageFormat.builder()
															.actionbarMessageString("&cL'utilisation des potions {potion} sont désactivé ({x}, {y}, {z})")
															.actionbarStay(3 * 1000)
															.actionbarPriority(Priorities.FLAG)),
		
		FLAG_PROPAGATION_DESCRIPTION(				"Active/Désactive la propagation"),
		FLAG_PROPAGATION_MESSAGE(					EMessageFormat.builder()
															.actionbarMessageString("&cPropagation désactivé ({x}, {y}, {z})")
															.actionbarStay(3 * 1000)
															.actionbarPriority(Priorities.FLAG)),
		
		FLAG_PVP_DESCRIPTION(						"Active/Désactive le PVP"),
		FLAG_PVP_MESSAGE(							EMessageFormat.builder()
															.actionbarMessageString("&cPVP désactivé ({x}, {y}, {z})")
															.actionbarStay(3 * 1000)
															.actionbarPriority(Priorities.FLAG)),
		
		FLAG_SNOW_DESCRIPTION(						"Active/Désactive la neige"),
		
		FLAG_TELEPORT_DESCRIPTION(					"Sauvegarde une position pour se téléporter à la région"),
		
		FLAG_SPAWN_DESCRIPTION(						"Défini un spawn pour la région"),
		
		FLAG_SPAWN_ENTITY_DESCRIPTION(				"Active/Désactive l'apparition des entités"),
		FLAG_SPAWN_ENTITY_MESSAGE(					EMessageFormat.builder()
															.actionbarMessageString("&cL'apparition de l'entité {entity} est désactivé ({x}, {y}, {z})")
															.actionbarStay(3 * 1000)
															.actionbarPriority(Priorities.FLAG)),
		
		PERMISSIONS_COMMANDS_EXECUTE(""),
		PERMISSIONS_COMMANDS_HELP(""),
		PERMISSIONS_COMMANDS_RELOAD(""),
		PERMISSIONS_COMMANDS_MIGRATE(""),
		PERMISSIONS_COMMANDS_BYPASS(""),
		PERMISSIONS_COMMANDS_CLEAR(""),
		PERMISSIONS_COMMANDS_SELECT_EXECUTE(""),
		PERMISSIONS_COMMANDS_SELECT_WAND(""),
		PERMISSIONS_COMMANDS_SELECT_POS(""),
		PERMISSIONS_COMMANDS_SELECT_EXPAND(""),
		PERMISSIONS_COMMANDS_SELECT_CUI(""),
		PERMISSIONS_COMMANDS_REGION_EXECUTE(""),
		PERMISSIONS_COMMANDS_REGION_LOAD(""),
		PERMISSIONS_COMMANDS_REGION_FLAGS(""),
		PERMISSIONS_COMMANDS_REGION_CHECK(""),
		PERMISSIONS_COMMANDS_REGION_INFO_EXECUTE(""),
		PERMISSIONS_COMMANDS_REGION_INFO_ITEM(""),
		PERMISSIONS_COMMANDS_REGION_INFO_OWNER(""),
		PERMISSIONS_COMMANDS_REGION_INFO_MEMBER(""),
		PERMISSIONS_COMMANDS_REGION_INFO_REGIONS(""),
		PERMISSIONS_COMMANDS_REGION_LIST_EXECUTE(""),
		PERMISSIONS_COMMANDS_REGION_LIST_OTHERS(""),
		PERMISSIONS_COMMANDS_REGION_DEFINE_EXECUTE(""),
		PERMISSIONS_COMMANDS_REGION_DEFINE_TEMPLATE(""),
		PERMISSIONS_COMMANDS_REGION_REMOVE_EXECUTE(""),
		PERMISSIONS_COMMANDS_REGION_REMOVE_OWNER(""),
		PERMISSIONS_COMMANDS_REGION_REMOVE_MEMBER(""),
		PERMISSIONS_COMMANDS_REGION_REMOVE_REGIONS(""),
		PERMISSIONS_COMMANDS_REGION_REDEFINE_EXECUTE(""),
		PERMISSIONS_COMMANDS_REGION_REDEFINE_OWNER(""),
		PERMISSIONS_COMMANDS_REGION_REDEFINE_MEMBER(""),
		PERMISSIONS_COMMANDS_REGION_REDEFINE_REGIONS(""),
		PERMISSIONS_COMMANDS_REGION_RENAME_EXECUTE(""),
		PERMISSIONS_COMMANDS_REGION_RENAME_OWNER(""),
		PERMISSIONS_COMMANDS_REGION_RENAME_MEMBER(""),
		PERMISSIONS_COMMANDS_REGION_RENAME_REGIONS(""),
		PERMISSIONS_COMMANDS_REGION_SELECT_EXECUTE(""),
		PERMISSIONS_COMMANDS_REGION_SELECT_OWNER(""),
		PERMISSIONS_COMMANDS_REGION_SELECT_MEMBER(""),
		PERMISSIONS_COMMANDS_REGION_SELECT_REGIONS(""),
		PERMISSIONS_COMMANDS_REGION_FLAG_ADD_EXECUTE(""),
		PERMISSIONS_COMMANDS_REGION_FLAG_ADD_OWNER(""),
		PERMISSIONS_COMMANDS_REGION_FLAG_ADD_MEMBER(""),
		PERMISSIONS_COMMANDS_REGION_FLAG_ADD_REGIONS(""),
		PERMISSIONS_COMMANDS_REGION_FLAG_REMOVE_EXECUTE(""),
		PERMISSIONS_COMMANDS_REGION_FLAG_REMOVE_OWNER(""),
		PERMISSIONS_COMMANDS_REGION_FLAG_REMOVE_MEMBER(""),
		PERMISSIONS_COMMANDS_REGION_FLAG_REMOVE_REGIONS(""),
		PERMISSIONS_COMMANDS_REGION_OWNER_ADD_EXECUTE(""),
		PERMISSIONS_COMMANDS_REGION_OWNER_ADD_OWNER(""),
		PERMISSIONS_COMMANDS_REGION_OWNER_ADD_MEMBER(""),
		PERMISSIONS_COMMANDS_REGION_OWNER_ADD_REGIONS(""),
		PERMISSIONS_COMMANDS_REGION_OWNER_REMOVE_EXECUTE(""),
		PERMISSIONS_COMMANDS_REGION_OWNER_REMOVE_OWNER(""),
		PERMISSIONS_COMMANDS_REGION_OWNER_REMOVE_MEMBER(""),
		PERMISSIONS_COMMANDS_REGION_OWNER_REMOVE_REGIONS(""),
		PERMISSIONS_COMMANDS_REGION_MEMBER_ADD_EXECUTE(""),
		PERMISSIONS_COMMANDS_REGION_MEMBER_ADD_OWNER(""),
		PERMISSIONS_COMMANDS_REGION_MEMBER_ADD_MEMBER(""),
		PERMISSIONS_COMMANDS_REGION_MEMBER_ADD_REGIONS(""),
		PERMISSIONS_COMMANDS_REGION_MEMBER_REMOVE_EXECUTE(""),
		PERMISSIONS_COMMANDS_REGION_MEMBER_REMOVE_OWNER(""),
		PERMISSIONS_COMMANDS_REGION_MEMBER_REMOVE_MEMBER(""),
		PERMISSIONS_COMMANDS_REGION_MEMBER_REMOVE_REGIONS(""),
		PERMISSIONS_COMMANDS_REGION_SETPARENT_EXECUTE(""),
		PERMISSIONS_COMMANDS_REGION_SETPARENT_OWNER(""),
		PERMISSIONS_COMMANDS_REGION_SETPARENT_MEMBER(""),
		PERMISSIONS_COMMANDS_REGION_SETPARENT_REGIONS(""),
		PERMISSIONS_COMMANDS_REGION_SETPRIORITY_EXECUTE(""),
		PERMISSIONS_COMMANDS_REGION_SETPRIORITY_OWNER(""),
		PERMISSIONS_COMMANDS_REGION_SETPRIORITY_MEMBER(""),
		PERMISSIONS_COMMANDS_REGION_SETPRIORITY_REGIONS(""),
		PERMISSIONS_COMMANDS_REGION_TELEPORT_EXECUTE(""),
		PERMISSIONS_COMMANDS_REGION_TELEPORT_SPAWN(""),
		PERMISSIONS_COMMANDS_REGION_TELEPORT_OWNER(""),
		PERMISSIONS_COMMANDS_REGION_TELEPORT_MEMBER(""),
		PERMISSIONS_COMMANDS_REGION_TELEPORT_REGIONS(""),
		PERMISSIONS_FLAGS("");
		
		private final String path;
	    private final EMessageBuilder french;
	    private final EMessageBuilder english;
	    private EMessageFormat message;
	    private EMessageBuilder builder;
	    
	    private EWMessages(final String french) {   	
	    	this(EMessageFormat.builder().chat(new EFormatString(french), true));
	    }
	    
	    private EWMessages(final String french, final String english) {   	
	    	this(EMessageFormat.builder().chat(new EFormatString(french), true), 
	    		EMessageFormat.builder().chat(new EFormatString(english), true));
	    }
	    
	    private EWMessages(final EMessageBuilder french) {   	
	    	this(french, french);
	    }
	    
	    private EWMessages(final EMessageBuilder french, final EMessageBuilder english) {
	    	Preconditions.checkNotNull(french, "Le message '" + this.name() + "' n'est pas définit");
	    	
	    	this.path = this.resolvePath();	    	
	    	this.french = french;
	    	this.english = english;
	    	this.message = french.build();
	    }

	    public String getName() {
			return this.name();
		}
	    
		public String getPath() {
			return this.path;
		}

		public EMessageBuilder getFrench() {
			return this.french;
		}

		public EMessageBuilder getEnglish() {
			return this.english;
		}
		
		public EMessageFormat getMessage() {
			return this.message;
		}
		
		public EMessageBuilder getBuilder() {
			return this.builder;
		}
		
		public void set(EMessageBuilder message) {
			this.message = message.build();
			this.builder = message;
		}
	}
	
	@Override
	public EnumMessage getPrefix() {
		return EWMessages.PREFIX;
	}
}
