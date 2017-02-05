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

public class EWMessage extends EMessage<EverWorldGuard> {

	public EWMessage(final EverWorldGuard plugin) {
		super(plugin, EWMessages.values());
	}
	
	public enum EWMessages implements EnumMessage {
		PREFIX("prefix", 				"[&4Ever&6&lWG&f] "),
		DESCRIPTION("description",		"Gestionnaire des régions"), 
		
		GROUP_NOT_FOUND("groupNotFound",		"&cErreur : Le group '&6<group>&c' est introuvable."), 
		FLAG_NOT_FOUND("flagNotFound",			"&cErreur : Le flag '&6<flag>&c' est introuvable."), 
		
		SELECT_DESCRIPTION("select.description",					"Permet de sélectionner une région"),
		
		SELECT_INFO_POS("select.info.pos",												"&7(&6<x>&7, &6<y>&7, &6<z>&7)"),
		SELECT_INFO_POS_HOVER("select.info.posHover",									"&7X : &6<x>[RT]&7Y : &6<y>[RT]&7Z : &6<z>"),
		SELECT_INFO_CUBOID_POS1_AND_POS2("select.info.cuboidPos1AndPos2",				"&7Position 1 : <pos1> &7, Position 2 : <pos2> &7(&6<area>&7)"),
		SELECT_INFO_CUBOID_POS1("select.info.cuboidPos1",								"&7Position 1 : <pos>"),
		SELECT_INFO_CUBOID_POS2("select.info.cuboidPos2",								"&7Position 2 : <pos>"),
		SELECT_INFO_CUBOID_EMPTY("select.info.cuboidEmpty",								"&cErreur : Aucune position sélectionnée."),
		SELECT_INFO_POLY_LINE("select.info.polyLine",									"    &7- <pos>"),
		SELECT_INFO_POLY_TITLE("select.info.polyTitle",									"&7Liste des positions &7(&6<area>&7)"),
		SELECT_INFO_POLY_EMPTY("select.info.polyEmpty",									"&cErreur : Aucune position sélectionnée."),
		SELECT_INFO_CYLINDER_CENTER_AND_RADIUS("select.info.cylinderCenterAndRadius",	"&7Centre : <pos1> &7, Radius : <pos2> &7(&6<area>&7)"),
		SELECT_INFO_CYLINDER_CENTER("select.info.cylinderCenter",						"&7Centre : <pos>"),
		SELECT_INFO_CYLINDER_RADIUS("select.info.cylinderRadius",						"&7Radius : <pos>"),
		SELECT_INFO_CYLINDER_EMPTY("select.info.cylinderEmpty",							"&cErreur : Aucune position sélectionnée."),
		
		SELECT_POS1_DESCRIPTION("select.pos1.description",			"Defini la première position"),
		SELECT_POS1_CUBOID_ONE("select.pos1.cuboidOne",				"&7Sélection de la première position : <pos>."),
		SELECT_POS1_CUBOID_TWO("select.pos1.cuboidTwo",				"&7Sélection de la première position : <pos> &7(&6<area>&7)."),
		SELECT_POS1_POLY("select.pos1.poly",						"&7Sélection de la première position : <pos>."),
		SELECT_POS1_CYLINDER_CENTER("select.pos1.cylinderCentor",	"&7Sélection du centre : <pos>."),
		SELECT_POS1_EQUALS("select.pos1.equals",					"&cErreur : Vous avez déjà sélectionné une position."),
		SELECT_POS1_CANCEL("select.pos1.cancel",					"&cErreur : Impossible de sélectionner une position pour le moment."),
		
		SELECT_POS2_DESCRIPTION("select.pos2.description",			"Defini la deuxième position"),
		SELECT_POS2_CUBOID_ONE("select.pos2.cuboidOne",				"&7Sélection de la deuxième position : <pos>."),
		SELECT_POS2_CUBOID_TWO("select.pos2.cuboidTwo",				"&7Sélection de la deuxième position : <pos> &7(&6<area>&7)."),
		SELECT_POS2_POLY_ONE("select.pos2.polyOne",					"&7Ajoute de la position &6#<num> &7: <pos>."),
		SELECT_POS2_POLY_ALL("select.pos2.polyAll",					"&7Ajoute de la position &6#<num> &7: <pos> &7(&6<area>&7)."),
		SELECT_POS2_RADIUS("select.pos2.radius",					"&7Sélection d'un rayon de <radius> &7block(s) : <pos>."),
		SELECT_POS2_NO_CENTER("select.pos2.noCenter",				"&cErreur : Aucune position centrale selectionnée."),
		SELECT_POS2_EQUALS("select.pos2.equals",					"&cErreur : Vous avez déjà selectionnée une position."),
		SELECT_POS2_CANCEL("select.pos2.cancel",					"&cErreur : Impossible de sélectionner une position pour le moment."),
		
		SELECT_CLEAR_DESCRIPTION("select.clear.description",		"Supprime toutes les positions selectionnée"),
		SELECT_CLEAR_PLAYER("select.clear.player",					"&7Vous n'avez plus aucune position sélectionnée."),
		
		SELECT_REMOVE_DESCRIPTION("select.remove.description",		"Supprime la denière position sélectionnée d'un polygone"),
		SELECT_REMOVE_PLAYER("select.remove.player",				"&7Vous avez supprimé la position : <pos>."),
		SELECT_REMOVE_EMPTY("select.remove.empty",					"&4Erreur : Vous n'avez aucune position sélectionnée."),
		SELECT_REMOVE_ERROR("select.remove.error",					"&4Erreur : Uniquement pour le type &62D Polygonal&c."),
		
		
		SELECT_EXPAND_DESCRIPTION("select.expand.description",	"Etend la zone sur toute la hauteur voulue"),
		
		SELECT_TYPE_DESCRIPTION("select.type.description",		"&7Change le type de selection"),
		SELECT_TYPE_CUBOID("select.type.cuboid",				"&7Cuboid : clique gauche pour définir le point 1 et clique droit pour définir le point 2.",
																"&7Cuboid: left click for point 1, right for point 2."),
		SELECT_TYPE_POLYGONAL("select.type.poly",				"&72D Polygonal : clique gauche pour définir le premier point et clique droit pour définir les points suivants.",
																"&72D polygon selector: Left/right click to add a point."),
		SELECT_TYPE_CYLINDER("select.type.cylinder",			"&7Cylindrique : clique gauche pour définir le centre, clique droit pour définir le rayon.",
																"&7Cylindrical select: Left click=center, right click to extend."),
		SELECT_TYPE_EQUALS("select.type.equals",				"&cErreur : Sélection &6<type> &cdéjà activée"),
		SELECT_TYPE_CANCEL("select.type.cancel",				"&cErreur : Impossible de changer de type de sélection pour le moment."),
		
		REGION_DESCRIPTION("region.description",				"Permet de gérer les régions protéger"),
		
		REGION_INFO_DESCRIPTION("region.info.description",													"Permet de voir la liste des régions sur votre position"),
		REGION_INFO_ONE_TITLE("region.info.one.title",														"&aRégion Info : &6<region>"),
		REGION_INFO_ONE_WORLD("region.info.one.world",														"    &6&l➤  &6World : &c<world>"),
		REGION_INFO_ONE_TYPE("region.info.one.type",														"    &6&l➤  &6Type : &c<type>"),
		REGION_INFO_ONE_POINTS("region.info.one.points",													"    &6&l➤  &6Points : &c<positions>"),
		REGION_INFO_ONE_POINTS_CUBOID("region.info.one.pointsCuboid",										"&6(&c<min_x>&6, &c<min_y>&6, &c<min_z>&6) (&c<max_x>&6, &c<max_y>&6, &c<max_z>&6)"),
		REGION_INFO_ONE_POINTS_CUBOID_HOVER("region.info.one.pointsCuboidHover",							"&6Min : (&c<min_x>&6, &c<min_y>&6, &c<min_z>&6)[RT]&6Max : (&c<max_x>&6, &c<max_y>&6, &c<max_z>&6)"),
		REGION_INFO_ONE_POINTS_POLYGONAL("region.info.one.pointsPolygonal",									"&6(&c<min_x>&6, &c<min_y>&6, &c<min_z>&6) (&c<max_x>&6, &c<max_y>&6, &c<max_z>&6)"),
		REGION_INFO_ONE_POINTS_POLYGONAL_HOVER("region.info.one.pointsPolygonalHover",						"&6Les positions : [RT]<positions"),
		REGION_INFO_ONE_POINTS_POLYGONAL_HOVER_POSITIONS("region.info.one.pointsPolygonalHoverPositions",	"&6#<num> : (&c<x>&6, &c<y>&6, &c<z>&6)"),
		REGION_INFO_ONE_POINTS_POLYGONAL_HOVER_JOIN("region.info.one.pointsPolygonalHoverJoin",				"[RT]"),
		REGION_INFO_ONE_PRIORITY("region.info.one.priority",												"    &6&l➤  &6Priorité : &c<prority>"),
		REGION_INFO_ONE_PARENT("region.info.one.parent",													"    &6&l➤  &6Parent : &c<parent>"),
		REGION_INFO_ONE_HERITAGE("region.info.one.heritage",												"    &6&l➤  &6Héritage :"),
		REGION_INFO_ONE_HERITAGE_LINE("region.info.one.heritageLine",										"        &c└ <region> : &7<type>"),
		REGION_INFO_ONE_HERITAGE_PADDING("region.info.one.heritagePadding",									"  "),
		REGION_INFO_ONE_OWNERS("region.info.one.owners",													"    &6&l➤  &6Owners : &c<owners>"),
		REGION_INFO_ONE_OWNERS_JOIN("region.info.one.ownersJoin",											"&6, &c"),
		REGION_INFO_ONE_GROUP_OWNERS("region.info.one.groupOwners",											"    &6&l➤  &6Groupes Owners : &c<owners>"),
		REGION_INFO_ONE_GROUP_OWNERS_JOIN("region.info.one.groupOwnersJoin",								"&6, &c"),
		REGION_INFO_ONE_MEMBERS("region.info.one.members",													"    &6&l➤  &6Members : &c<members>"),
		REGION_INFO_ONE_MEMBERS_JOIN("region.info.one.membersJoin",											"&6, &c"),
		REGION_INFO_ONE_GROUP_MEMBERS("region.info.one.groupMembers",										"    &6&l➤  &6Groupes Members : &c<members>"),
		REGION_INFO_ONE_GROUP_MEMBERS_JOIN("region.info.one.membersJoin",									"&6, &c"),
		REGION_INFO_ONE_FLAGS("region.info.one.flags",														"    &6&l➤ &6Flag :"),
		REGION_INFO_ONE_FLAGS_LINE("region.info.one.flagsLine",												"            &a&l- <flag> : &c<value>"),
		REGION_INFO_ONE_FLAGS_DEFAULT("region.info.one.flagsDefault",										"        &6&l●   &6Default:"),
		REGION_INFO_ONE_FLAGS_MEMBER("region.info.one.flagsMember",											"        &6&l●   &6Member:"),
		REGION_INFO_ONE_FLAGS_OWNER("region.info.one.flagsOwner",											"        &6&l●   &6Owner:"),
		REGION_INFO_ONE_HERITAGE_FLAGS("region.info.one.heritageFlags",										"    &6&l➤  Flag Héritage :"),
		REGION_INFO_ONE_HERITAGE_FLAGS_LINE("region.info.one.heritageFlagsLine",							"            &a&l- <flag> : &c<value>"),
		REGION_INFO_ONE_HERITAGE_FLAGS_DEFAULT("region.info.one.heritageFlagsDefault",						"        &6&l●   &6Default:"),
		REGION_INFO_ONE_HERITAGE_FLAGS_MEMBER("region.info.one.heritageFlagsMember",						"        &6&l●   &6Member:"),
		REGION_INFO_ONE_HERITAGE_FLAGS_OWNER("region.info.one.heritageFlagsOwner",							"        &6&l●   &6Owner:"),
		REGION_INFO_LIST_TITLE("region.info.list.title",													"&aListe des régions"),
		REGION_INFO_LIST_LINE("region.info.list.line",														"    &6&l➤  &6<region> : (Type : &7<type>&6, Priorité : &7<priority>&6)"),
		REGION_INFO_NO_PERMISSION("region.info.noPermission",												"&cErreur : Vous n'avez accès aux informations de la région &6<region>&c."),
		REGION_INFO_EMPTY("region.info.empty",																"&cErreur : Vous n'avez accès aux informations sur ces régions."),
		
		REGION_LIST_DESCRIPTION("region.list.description",								"Permet de voir la liste des régions dans le monde"),
		REGION_LIST_ALL_TITLE("region.list.allTitle",									"&aLa liste des régions : &6<world>"),
		REGION_LIST_ALL_LINE("region.list.allLine",										"    &6&l➤  &6<region> : [RT]            &7(Type : &a<type>&7, Priorité : &a<priority>&7)"),
		REGION_LIST_ALL_EMPTY("region.list.allEmpty",									"    &7Aucune région"),
		REGION_LIST_PLAYER_TITLE_EQUALS("region.list.playerTitleEquals",				"&aLa liste de vos régions : &6<world>"),
		REGION_LIST_PLAYER_TITLE_OTHERS("region.list.playerTitleOthers",				"&aListe des régions : &6<world>"),
		REGION_LIST_PLAYER_LINE("region.list.playerLine",								"    &6&l➤  &6<region> : [RT]            &7(Type : &a<type>&7, Priorité : &a<priority>&7)"),
		REGION_LIST_PLAYER_EMPTY("region.list.playerEmpty",								"    &7Aucune région"),
		REGION_LIST_GROUP_TITLE("region.list.groupTitle",								"&aLa liste des régions du groupe &6<group> &7: &6<world>"),
		REGION_LIST_GROUP_LINE("region.list.groupLine",									"    &6&l➤  &6<region> : [RT]            &7(Type : &a<type>&7, Priorité : &a<priority>&7)"),
		REGION_LIST_GROUP_EMPTY("region.list.groupLine",								"    &7Aucune région"),
		
		REGION_DEFINE_DESCRIPTION("region.define.description",									"Permet de définir une nouvelle région"),
		REGION_DEFINE_CUBOID_CREATE("region.define.cuboid.create",								"&7Création de la région &6<region> &7de type &6<type>."),
		REGION_DEFINE_CUBOID_POINTS("region.define.cuboid.points",								"&6(&c<min_x>&6, &c<min_y>&6, &c<min_z>&6) (&c<max_x>&6, &c<max_y>&6, &c<max_z>&6)"),
		REGION_DEFINE_CUBOID_POINTS_HOVER("region.define.cuboid.pointsHover",					"&6Min : (&c<min_x>&6, &c<min_y>&6, &c<min_z>&6)[RT]&6Max : (&c<max_x>&6, &c<max_y>&6, &c<max_z>&6)"),
		REGION_DEFINE_CUBOID_ERROR_POSITION("region.define.cuboid.errorPosition",				"&cErreur : Vous devez sélectionner 2 positions pour définir une région &6<type>&c."),
		REGION_DEFINE_POLYGONAL_CREATE("region.define.polygonal.create",						"&7Création de la région &6<region> &7de type &6<type>."),
		REGION_DEFINE_POLYGONAL_POINTS("region.define.polygonal.points",						"&6(&c<min_x>&6, &c<min_y>&6, &c<min_z>&6) (&c<max_x>&6, &c<max_y>&6, &c<max_z>&6)"),
		REGION_DEFINE_POLYGONAL_POINTS_HOVER("region.define.polygonal.pointsHover",				"&6Les positions : [RT]<positions"),
		REGION_DEFINE_POLYGONAL_POINTS_HOVER_LINE("region.define.polygonal.pointsHoverLine",	"&6#<num> : (&c<x>&6, &c<y>&6, &c<z>&6)"),
		REGION_DEFINE_POLYGONAL_POINTS_HOVER_JOIN("region.define.polygonal.pointsHoverJoin",	"[RT]"),
		REGION_DEFINE_POLYGONAL_ERROR_POSITION("region.define.polygonal.errorPosition",			"&cErreur : Vous devez sélectionner au moins 3 positions pour définir une région &6<type>&c."),
		REGION_DEFINE_TEMPLATE_CREATE("region.define.template.create",							"&7Création de la région &6<region> &7de type &6<type>."),
		REGION_DEFINE_ERROR_NAME("region.define.errorName",										"&cErreur : Il y a déjà une région qui porte le nom &6<region>&c."),
		REGION_DEFINE_ERROR_SELECT_TYPE("region.define.errorSelectType",						"&cErreur : Il posible de créer une région de type &6<type>&c."),
		
		REGION_FLAG_ADD_DESCRIPTION("region.flag.add.description",								"Permet de définir un flag d'une région"),
		REGION_FLAG_ADD_PLAYER("region.flag.add.player",										"&7Vous avez défini le flag &6<flag> &7sur la région &6<region> &7dans le monde &6<world> pour le groupe &6<group> &7à la valeur &6<value>&7."),
		REGION_FLAG_ADD_ERROR("region.flag.add.error",											"&cErreur : La valeur est &6'<value>&6' &7est invalide."),
		
		REGION_FLAG_REMOVE_DESCRIPTION("region.flag.remove.description",						"Permet de supprimer un flag d'une région"),
		REGION_FLAG_REMOVE_UPDATE("region.flag.remove.update",									"&7Vous avez défini le flag &6<flag> &7sur la région &6<region> &7dans le monde &6<world> pour le groupe &6<group> &7à la valeur &6<value>&7."),
		REGION_FLAG_REMOVE_PLAYER("region.flag.remove.player",									"&7Vous avez défini le flag &6<flag> &7sur la région &6<region> &7dans le monde &6<world> pour le groupe &6<group> &7à la valeur &6<value>&7."),
		REGION_FLAG_REMOVE_ERROR("region.flag.add.error",										"&cErreur : La valeur est &6'<value>&6' &7est invalide."),
		
		FLAG_BUILD("flag.build",						"Permet de désactiver les constructions"),
		FLAG_PVP("flag.pvp",							"Permet de désactiver le PVP"),
		FLAG_TELEPORT("flag.teleport",					"Permet de sauvegarde une position pour ce téléporter à la région"),
		FLAG_SPAWN("flag.spawn",						"Permet de définir un spawn pour la région");
		
		private final String path;
	    private final EMessageBuilder french;
	    private final EMessageBuilder english;
	    private EMessageFormat message;
	    
	    private EWMessages(final String path, final String french) {   	
	    	this(path, EMessageFormat.builder().chat(new EFormatString(french), true));
	    }
	    
	    private EWMessages(final String path, final String french, final String english) {   	
	    	this(path, 
	    		EMessageFormat.builder().chat(new EFormatString(french), true), 
	    		EMessageFormat.builder().chat(new EFormatString(english), true));
	    }
	    
	    private EWMessages(final String path, final EMessageBuilder french) {   	
	    	this(path, french, french);
	    }
	    
	    private EWMessages(final String path, final EMessageBuilder french, final EMessageBuilder english) {
	    	Preconditions.checkNotNull(french, "Le message '" + this.name() + "' n'est pas définit");
	    	
	    	this.path = path;	    	
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
		
		public void set(EMessageFormat message) {
			this.message = message;
		}
	}
}
