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
		PREFIX("PREFIX", 				"[&4Ever&6&lWG&f] "),
		DESCRIPTION("DESCRIPTION",		"Plugin de protection des régions"), 
		
		MIGRATE_DESCRIPTION("migrateDescription",									"Transfère les données"),
		MIGRATE_SQL_CONFIRMATION("migrateSqlConfirmation", 						"&7Souhaitez-vous vraiment transférer les données dans une base de données &6SQL&7 ? <confirmation>"),
		MIGRATE_SQL_CONFIRMATION_VALID("migrateSqlConfirmationValid", 				"&2&n[Confirmer]"),
		MIGRATE_SQL_CONFIRMATION_VALID_HOVER("migrateSqlConfirmationValidHover", 	"&cCliquez ici pour réaliser le transfert"),
		MIGRATE_CONF_CONFIRMATION("migrateConfConfirmation", 						"&7Souhaitez-vous vraiment transférer les données dans des &6fichiers de configuration&7 ? <confirmation>"),
		MIGRATE_CONF_CONFIRMATION_VALID("migrateConfConfirmationValid", 			"&2&n[Confirmer]"),
		MIGRATE_CONF_CONFIRMATION_VALID_HOVER("migrateConfConfirmationValidHover", "&cCliquez ici pour réaliser le transfert"),
		MIGRATE_SQL("migrateSql", 													"&7Les données ont bien été transférées dans la base de données."),
		MIGRATE_SQL_LOG("migrateSqlLog", 											"Les données ont été transférées dans la base de données par <player>."),
		MIGRATE_CONF("migrateConf", 												"&7Les données ont bien été transférées dans les fichiers de configurations."),
		MIGRATE_CONF_LOG("migrateConfLog", 										"Les données ont été transférées dans les fichiers de configurations par <player>."),
		MIGRATE_DISABLE("migrateDisable", 											"&cErreur : Vous devez être connecté à une base de données pour faire le transfert des données."),
		
		CLEAR_DESCRIPTION("clearDescription",										"Supprimé toutes les régions d'un monde"),
		CLEAR_WORLD_CONFIRMATION("clearWorldConfirmation", 						"&7Souhaitez-vous vraiment supprimer tous les régions du monde &6<world> &7? <confirmation>"),
		CLEAR_WORLD_CONFIRMATION_VALID("clearWorldConfirmationValid", 				"&2&n[Confirmer]"),
		CLEAR_WORLD_CONFIRMATION_VALID_HOVER("clearWorldConfirmationValidHover", 	"&cCliquez ici pour valider la suppression"),
		CLEAR_WORLD_PLAYER("clearWorldPlayer", 									"&7Tous les régions du monde <world> ont bien été supprimées."),
		CLEAR_WORLD_LOG("clearWorldLog", 											"Tous les régions du monde <world> ont été supprimées par <player>."),
		CLEAR_ALL_CONFIRMATION("clearAllConfirmation", 							"&7Souhaitez-vous vraiment supprimer tous les régions du serveur &7? <confirmation>"),
		CLEAR_ALL_CONFIRMATION_VALID("clearAllConfirmationValid", 					"&2&n[Confirmer]"),
		CLEAR_ALL_CONFIRMATION_VALID_HOVER("clearAllConfirmationValidHover", 		"&cCliquez ici pour valider la suppression"),
		CLEAR_ALL_PLAYER("clearAllPlayer", 										"&7Tous les régions du serveur ont bien été supprimées."),
		CLEAR_ALL_LOG("clearAllLog", 												"Tous les régions du serveur ont été supprimées par <player>."),
		
		GROUP_NOT_FOUND("groupNotFound",											"&cErreur : Le group '&6<group>&c' est introuvable."), 
		GROUP_INCOMPATIBLE("groupIncompatible",										"&cErreur : Le group '&6<group>&c' est incompatible avec le flag <flag>."), 
		FLAG_NOT_FOUND("flagNotFound",												"&cErreur : Le flag '&6<flag>&c' est introuvable."), 
		
		SELECT_DESCRIPTION("selectDescription",									"Permet de sélectionner une région"),
		
		SELECT_CUI_DESCRIPTION("selectCuiDescription",								"Permet de voir les régions (Require : WorldEdit CUI)"),
		
		SELECT_INFO_DESCRIPTION("selectInfoDescription",								"Affiche les informations sur la région sélectionnée"),
		SELECT_INFO_POS("selectInfoPos",												"&7(&6<x>&7, &6<y>&7, &6<z>&7)"),
		SELECT_INFO_POS_HOVER("selectInfoPosHover",									"&7X : &6<x>[RT]&7Y : &6<y>[RT]&7Z : &6<z>"),
		SELECT_INFO_CUBOID_TITLE("selectInfoCuboidTitle",								"&7Votre sélection &6CUDOID&7"),
		SELECT_INFO_CUBOID_POS1_AND_POS2("selectInfoCuboidPos1AndPos2",				"    &6&l➤  &6Position 1 : &c<pos1>[RT]"
																					  + "    &6&l➤  &6Position 2 : &c<pos2>[RT]"
																					  + "    &6&l➤  &6Volume : &7<area>"),
		SELECT_INFO_CUBOID_POS1("selectInfoCuboidPos1",								"    &6&l➤  &6Position 1 : &7<pos1>"),
		SELECT_INFO_CUBOID_POS2("selectInfoCuboidPos2",								"    &6&l➤  &6Position 2 : &7<pos2>"),
		SELECT_INFO_CUBOID_EMPTY("selectInfoCuboidEmpty",								"    &cAucune position sélectionnée."),
		SELECT_INFO_EXTEND_TITLE("selectInfoExtendTitle",								"&7Votre sélection &6EXTEND&7"),
		SELECT_INFO_EXTEND_POS1_AND_POS2("selectInfoExtendPos1AndPos2",				"    &6&l➤  &6Position 1 : &7<pos1>[RT]"
																					  + "    &6&l➤  &6Position 2 : &7<pos2>[RT]"
																					  + "    &6&l➤  &6Volume : &7<area>"),	
		SELECT_INFO_EXTEND_POS1("selectInfoExtendPos1",								"    &6&l➤  &6Position 1 : &7<pos1>"),
		SELECT_INFO_EXTEND_POS2("selectInfoExtendPos2",								"    &6&l➤  &6Position 2 : &7<pos2>"),
		SELECT_INFO_EXTEND_EMPTY("selectInfoExtendEmpty",								"    &cAucune position sélectionnée."),
		SELECT_INFO_POLY_TITLE("selectInfoPolyTitle",									"&7Votre sélection &6POLYGONAL &7(&6<area>&7)"),
		SELECT_INFO_POLY_LINE("selectInfoPolyLine",									"    &6&l➤  &6#<num> : &7<pos>"),
		SELECT_INFO_POLY_EMPTY("selectInfoPolyEmpty",									"    Aucune position sélectionnée."),
		SELECT_INFO_CYLINDER_TITLE("selectInfoCylinderTitle",								"&7Votre sélection &6CYLINDER&7"),
		SELECT_INFO_CYLINDER_CENTER_AND_RADIUS("selectInfoCylinderCenterAndRadius",	"    &6&l➤  &6Centre : &7<center>[RT]"
																					  + "    &6&l➤  &6Longueur : &7<width>[RT]"
																					  + "    &6&l➤  &6Hauteur : &7<height>[RT]"
																					  + "    &6&l➤  &6Profondeur : &7<length>[RT]"
																					  + "    &6&l➤  &6Volume : &7<area>"),
		SELECT_INFO_CYLINDER_CENTER("selectInfoCylinderCenter",						"    &6&l➤  &6Centre : &7<center>"),
		SELECT_INFO_CYLINDER_EMPTY("selectInfoCylinderEmpty",							"&cAucune position sélectionnée."),
		SELECT_INFO_ELLIPSOID_TITLE("selectInfoEllipsoidTitle",						"&7Votre sélection &6ELLIPSOID&7"),
		SELECT_INFO_ELLIPSOID_CENTER_AND_RADIUS("selectInfoEllipsoidCenterAndRadius",	"    &6&l➤  &6Centre : &7<center>[RT]"
																					  + "    &6&l➤  &6Longueur : &7<width>[RT]"
																					  + "    &6&l➤  &6Hauteur : &7<height>[RT]"
																					  + "    &6&l➤  &6Profondeur : &7<length>[RT]"
																					  + "    &6&l➤  &6Volume : &7<area>"),
		SELECT_INFO_ELLIPSOID_CENTER("selectInfoEllipsoidCenter",						"    &6&l➤  &6Centre : &7<center>"),
		SELECT_INFO_ELLIPSOID_EMPTY("selectInfoEllipsoidEmpty",						"    Aucune position sélectionnée."),
		SELECT_INFO_SPHERE_TITLE("selectInfoSphereTitle",								"&7Votre sélection &6SPHERE&7"),
		SELECT_INFO_SPHERE_CENTER_AND_RADIUS("selectInfoSphereCenterAndRadius",		"    &6&l➤  &6Centre : &7<center>[RT]"
																					  + "    &6&l➤  &6Rayon : &7<radius>[RT]"
																					  + "    &6&l➤  &6Volume : &7<area>"),
		SELECT_INFO_SPHERE_CENTER("selectInfoSphereCenter",							"    &6&l➤  &6Centre : &7<center>"),
		SELECT_INFO_SPHERE_EMPTY("selectInfoSphereEmpty",								"    Aucune position sélectionnée."),
		
		SELECT_POS1_DESCRIPTION("selectPos1Description",			"Défini  la première position"),
		SELECT_POS1_ONE("selectPos1One",							"&7Sélection de la position 1 : <position>."),
		SELECT_POS1_TWO("selectPos1Two",							"&7Sélection de la position 1 : <position> &7(&6<area>&7)."),
		SELECT_POS1_POLY("selectPos1Poly",						"&7Sélection de la position #1 : <position>."),
		SELECT_POS1_CENTER("selectPos1Center",					"&7Sélection du centre : <position>."),
		SELECT_POS1_EQUALS("selectPos1Equals",					"&cErreur : Vous avez déjà sélectionnée une position."),
		SELECT_POS1_CANCEL("selectPos1Cancel",					"&cErreur : Impossible de sélectionner une position pour le moment."),
		
		SELECT_POS2_DESCRIPTION("selectPos2Description",			"Défini  la deuxième position"),
		SELECT_POS2_ONE("selectPos2One",							"&7Sélection de la position 2 : <position>."),
		SELECT_POS2_TWO("selectPos2Two",							"&7Sélection de la position 2 : <position> &7(&6<area>&7)."),
		SELECT_POS2_POLY_ONE("selectPos2PolyOnde",				"&7Ajout de la position &6#<num> &7: <position>."),
		SELECT_POS2_POLY_ALL("selectPos2PolyAll",					"&7Ajout de la position &6#<num> &7: <position> &7(&6<area>&7)."),
		SELECT_POS2_POLY_ERROR("selectPos2PolyError",				"&cErreur : Vous avez déjà sélectionné le nombre maximum de position."),
		SELECT_POS2_RADIUS("selectPos2Radius",					"&7Sélection d'un rayon de <radius> &7block(s) : <position>."),
		SELECT_POS2_NO_CENTER("selectPos2NoCenter",				"&cErreur : Aucune position centrale n'est sélectionnée."),
		SELECT_POS2_EQUALS("selectPos2Equals",					"&cErreur : Vous avez déjà sélectionné cette position."),
		SELECT_POS2_CANCEL("selectPos2Cancel",					"&cErreur : Impossible de sélectionner une position pour le moment."),
		
		SELECT_CLEAR_DESCRIPTION("selectClearDescription",		"Supprime toutes les positions sélectionnées"),
		SELECT_CLEAR_PLAYER("selectClearPlayer",					"&7Vous n'avez plus aucune position."),
		
		SELECT_REMOVE_DESCRIPTION("selectRemoveDescription",		"Supprime la dernière  position sélectionnée d'un polygone"),
		SELECT_REMOVE_PLAYER("selectRemovePlayer",				"&7Vous avez supprimée la position : <pos>."),
		SELECT_REMOVE_EMPTY("selectRemoveEmpty",					"&4Erreur : Vous n'avez aucune position sélectionnée."),
		SELECT_REMOVE_ERROR("selectRemoveError",					"&4Erreur : Uniquement pour le type &62D Polygonal&c."),
		
		
		SELECT_EXPAND_DESCRIPTION("selectExpandDescription",					"Permet d'étendre la sélection"),
		SELECT_EXPAND_VERT("selectExpandVert", 								"&7Vous avez étendu votre sélection de &6<size> &7block(s) [Bas-En-Haut]."),
		SELECT_EXPAND_DIRECTION("selectExpandDirection", 						"&7Vous avez étendu votre sélection de &6<size> &7block(s) [<amount> <direction>]."),
		SELECT_EXPAND_DIRECTION_OPPOSITE("selectExpandDirectionOpposite",		"&7Vous avez étendu votre sélection de &6<size> &7block(s) [<amount> <direction>] [<amount_opposite> <direction_opposite>]."),
		SELECT_EXPAND_ERROR_OPERATION("selectExpandErrorOperation",			"&4Erreur : Vous ne pouvez pas faire cette opération sur ce type de région."),
		SELECT_EXPAND_ERROR_NO_REGION("selectExpandErrorNoRegion",			"&4Erreur : Vous devez d'abord sélectionner une région."),
		
		SELECT_CONTRACT_DESCRIPTION("selectContractDescription",				"Permet de réduire la sélection"),
		SELECT_CONTRACT_DIRECTION("selectContractDirection", 					"&7Vous avez réduit votre sélection de &6<size> &7block(s) [<amount> <direction>]."),
		SELECT_CONTRACT_DIRECTION_OPPOSITE("selectContractDirectionOpposite",	"&7Vous avez réduit votre sélection de &6<size> &7block(s) [<amount> <direction>] [<amount_opposite> <direction_opposite>]."),
		SELECT_CONTRACT_ERROR_OPERATION("selectContractErrorOperation",		"&4Erreur : Vous ne pouvez pas faire cette opération sur ce type de région."),
		SELECT_CONTRACT_ERROR_NO_REGION("selectContractErrorNoRegion",		"&4Erreur : Vous devez d'abord sélectionner une région."),
		
		SELECT_SHIFT_DESCRIPTION("selectShiftDescription",					"Permet de déplacer la sélection"),
		SELECT_SHIFT_DIRECTION("selectShiftDirection", 						"&7Vous avez déplacé votre sélection de &6<amount> &7block(s) vers la &6<direction>&7."),
		SELECT_SHIFT_ERROR_OPERATION("selectShiftErrorOperation",				"&4Erreur : Vous ne pouvez pas faire cette opération sur ce type de région."),
		SELECT_SHIFT_ERROR_NO_REGION("selectShiftErrorNoRegion",				"&4Erreur : Vous devez d'abord sélectionner une région."),
		
		SELECT_TYPE_DESCRIPTION("selectTypeDescription",		"&7Change le type de sélection"),
		
		SELECT_TYPE_CUBOID("selectTypeCuboid",				"&7Cuboid : Clic gauche pour définir le point N°1 et clic droit pour définir le point N°2.",
																"&7Cuboid : left click for point 1, right for point 2."),
		SELECT_TYPE_EXTEND("selectTypeExtend",				"&7Extend : Clic gauche pour définir le point N°1 et clic droit pour définir le point N°2.",
																"&7Extend : left click for point 1, right for point 2."),
		SELECT_TYPE_POLYGONAL("selectTypePoly",				"&72D Polygonal : Clic gauche pour définir le premier point et clic droit pour définir les points suivants.",
																"&72D polygon selector: Left/right click to add a point."),
		SELECT_TYPE_CYLINDER("selectTypeCylinder",			"&7Cylindrique : Clic gauche pour définir le centre, clic droit pour définir le rayon.",
																"&7Cylindrical select: Left click=center, right click to extend."),
		SELECT_TYPE_ELLIPSOID("selectTypeEllipsoid",			"&7Ellipsoid : Clic gauche pour définir le centre, clic droit pour définir les formes.",
																"&7Ellipsoid select: Left click=center, right click to extend."),
		SELECT_TYPE_SPHERE("selectTypeSphere",				"&7Sphere : Clic gauche pour définir le centre, clic droit pour définir les formes.",
																"&7Sphere select: Left click=center, right click to extend."),
		SELECT_TYPE_EQUALS("selectTypeEquals",				"&cErreur : Sélection &6<type> &cdéjà activée"),
		SELECT_TYPE_CANCEL("selectTypeCancel",				"&cErreur : Impossible de changer de type de sélection pour le moment."),
		
		REGION_DESCRIPTION("regionDescription",				"Gère les régions protégées"),
		REGION_NO_PERMISSION("regionNoPermission",				"&cErreur : Vous n'avez pas la permission pour cette région &6<region>&c."),
		
		REGION_LOAD_DESCRIPTION("regionLoadDescription",		"Rechargement des régions d'un monde"), 
		REGION_LOAD_MESSAGE("regionLoadMessage",				"&7Rechargement du monde &6<world>&7."), 
		
		REGION_BYPASS_DESCRIPTION("regionBypassDescription", 						"Active/Désactive le mode admin"),
		
		REGION_BYPASS_ON_PLAYER("regionBypassOnPlayer", 							"&7Vous avez activé le mode admin."),
		REGION_BYPASS_ON_PLAYER_ERROR("regionBypassOnPlayerError", 				"&cErreur : Le mode admin est déjà activé."),
		REGION_BYPASS_ON_OTHERS_PLAYER("regionBypassOnOthersPlayer", 				"&7Le mode admin est désormais activé grâce à &6<staff>&7."),
		REGION_BYPASS_ON_OTHERS_STAFF("regionBypassOnOthersStaff", 				"&7Vous avez activé le mode admin pour &6<player>&7."),
		REGION_BYPASS_ON_OTHERS_ERROR("regionBypassOnOthersError", 				"&cErreur : Le mode admin de &6<player> &cest déjà activé."),
		
		REGION_BYPASS_OFF_PLAYER("regionBypassOffPlayer", 						"&7Vous avez désactivé le mode admin."),
		REGION_BYPASS_OFF_PLAYER_ERROR("regionBypassOffPlayerError", 				"&cErreur : Le mode admin est déjà désactivé."),
		REGION_BYPASS_OFF_OTHERS_PLAYER("regionBypassOffOthersPlayer", 			"&7Le mode admin est désormais désactivé à cause de &6<staff>&7."),
		REGION_BYPASS_OFF_OTHERS_STAFF("regionBypassOffOthersStaff", 				"&7Vous avez désactivé le mode admin pour &6<player>&7."),
		REGION_BYPASS_OFF_OTHERS_ERROR("regionBypassOffOthersError", 				"&7Erreur : Le mode admin de &6<player> &cest déjà désactivé."),
		
		REGION_BYPASS_STATUS_PLAYER_ON("regionBypassStatusPlayerOn", 				"&7Le mode admin est activé."),
		REGION_BYPASS_STATUS_PLAYER_OFF("regionBypassStatusPlayerOff", 			"&7Le mode admin est désactivé.."),
		REGION_BYPASS_STATUS_OTHERS_ON("regionBypassStatusOthersOn", 				"&7Le mode admin de &6<player> &7est activé."),
		REGION_BYPASS_STATUS_OTHERS_OFF("regionBypassStatusOthersOff", 			"&7Le mode admin de &6<player> &7est désactivé."),
		
		REGION_CHECK_DESCRIPTION("regionCheckDescription", 						"Permet de savoir la valeur de chaque flag"),
		
		REGION_CHECK_GROUP_TITLE("regionCheckGroupTitle", 						"&aListe des flags : &6<group>"),
		REGION_CHECK_GROUP_LINE("regionCheckGroupLine", 							"        &a&l- <flag> : &c<value> &7(Région : &a<region>&7)"),
		REGION_CHECK_GROUP_LINE_DEFAULT("regionCheckGroupLineDefault", 			"        &a&l- <flag> : &c<value>"),
		
		REGION_CHECK_FLAG_TITLE("regionCheckFlagTitle", 							"&aListe des valeurs : &6<flag>"),
		REGION_CHECK_FLAG_DEFAULT("regionCheckFlagDefault", 						"    &6&l➤   &6Default : &c<value> &7(Région : &a<region>&7)"),
		REGION_CHECK_FLAG_DEFAULT_DEFAULT("regionCheckFlagDefaultDefault", 		"    &6&l➤   &6Default : &c<value>"),
		REGION_CHECK_FLAG_MEMBER("regionCheckFlagMember", 						"    &6&l➤   &6Member : &c<value> &7(Région : &a<region>&7)"),
		REGION_CHECK_FLAG_MEMBER_DEFAULT("regionCheckFlagMemberDefault", 			"    &6&l➤   &6Member : &c<value>"),
		REGION_CHECK_FLAG_OWNER("regionCheckFlagOwner", 							"    &6&l➤   &6Owner : &c<value> &7(Région : &a<region>&7)"),
		REGION_CHECK_FLAG_OWNER_DEFAULT("regionCheckFlagOwneDefault", 			"    &6&l➤   &6Owner : &c<value>"),
		
		REGION_INFO_DESCRIPTION("regionInfoDescription",													"Permet de voir la liste des régions sur votre position"),
		REGION_INFO_ONE_TITLE("regionInfoOneTitle",														"&aRégion Info : &6<region>"),
		REGION_INFO_ONE_WORLD("regionInfoOneWorld",														"    &6&l➤  &6World : &c<world>"),
		REGION_INFO_ONE_TYPE("regionInfoOneType",														"    &6&l➤  &6Type : &c<type>"),
		REGION_INFO_ONE_POINTS("regionInfoOnePoints",													"    &6&l➤  &6Points : &c<positions>"),
		REGION_INFO_ONE_POINTS_CUBOID("regionInfoOnePointsCuboid",										"&6(&c<min_x>&6, &c<min_y>&6, &c<min_z>&6) (&c<max_x>&6, &c<max_y>&6, &c<max_z>&6)"),
		REGION_INFO_ONE_POINTS_CUBOID_HOVER("regionInfoOnePointsCuboidHover",							"&6Min : (&c<min_x>&6, &c<min_y>&6, &c<min_z>&6)[RT]&6Max : (&c<max_x>&6, &c<max_y>&6, &c<max_z>&6)"),
		REGION_INFO_ONE_POINTS_POLYGONAL("regionInfoOnePointsPolygonal",									"&6(&c<min_x>&6, &c<min_y>&6, &c<min_z>&6) (&c<max_x>&6, &c<max_y>&6, &c<max_z>&6)"),
		REGION_INFO_ONE_POINTS_POLYGONAL_HOVER("regionInfoOnePointsPolygonalHover",						"&6Les positions : [RT]<positions>"),
		REGION_INFO_ONE_POINTS_POLYGONAL_HOVER_POSITIONS("regionInfoOnePointsPolygonalHoverPositions",	"&6#<num> : (&c<x>&6, &c<y>&6, &c<z>&6)"),
		REGION_INFO_ONE_POINTS_POLYGONAL_HOVER_JOIN("regionInfoOnePointsPolygonalHoverJoin",				"[RT]"),
		REGION_INFO_ONE_PRIORITY("regionInfoOnePriority",												"    &6&l➤  &6Priorité : &c<prority>"),
		REGION_INFO_ONE_PARENT("regionInfoOneParent",													"    &6&l➤  &6Parent : &c<parent>"),
		REGION_INFO_ONE_HERITAGE("regionInfoOneHeritage",												"    &6&l➤  &6Héritage :"),
		REGION_INFO_ONE_HERITAGE_LINE("regionInfoOneHeritageLine",										"        &c└ <region> : &7<type>"),
		REGION_INFO_ONE_HERITAGE_PADDING("regionInfoOneHeritagePadding",									"  "),
		REGION_INFO_ONE_OWNERS("regionInfoOneOwners",													"    &6&l➤  &6Owners : &c<owners>"),
		REGION_INFO_ONE_OWNERS_JOIN("regionInfoOneOwnersJoin",											"&6, &c"),
		REGION_INFO_ONE_GROUP_OWNERS("regionInfoOneGroupOwners",											"    &6&l➤  &6Groupes Owners : &c<owners>"),
		REGION_INFO_ONE_GROUP_OWNERS_JOIN("regionInfoOneGroupOwnersJoin",								"&6, &c"),
		REGION_INFO_ONE_MEMBERS("regionInfoOneMembers",													"    &6&l➤  &6Members : &c<members>"),
		REGION_INFO_ONE_MEMBERS_JOIN("regionInfoOneMembersJoin",											"&6, &c"),
		REGION_INFO_ONE_GROUP_MEMBERS("regionInfoOneGroupMembers",										"    &6&l➤  &6Groupes Members : &c<members>"),
		REGION_INFO_ONE_GROUP_MEMBERS_JOIN("regionInfoOneMembersJoin",									"&6, &c"),
		REGION_INFO_ONE_FLAGS("regionInfoOneFlags",														"    &6&l➤  &6Flag :"),
		REGION_INFO_ONE_FLAGS_LINE("regionInfoOneFlagsLine",												"            &a&l- <flag> : &c<value>"),
		REGION_INFO_ONE_FLAGS_DEFAULT("regionInfoOneFlagsDefault",										"        &6&l●   &6Default:"),
		REGION_INFO_ONE_FLAGS_MEMBER("regionInfoOneFlagsMember",											"        &6&l●   &6Member:"),
		REGION_INFO_ONE_FLAGS_OWNER("regionInfoOneFlagsOwner",											"        &6&l●   &6Owner:"),
		REGION_INFO_ONE_HERITAGE_FLAGS("regionInfoOneHeritageFlags",										"    &6&l➤  Flag Héritage :"),
		REGION_INFO_ONE_HERITAGE_FLAGS_LINE("regionInfoOneHeritageFlagsLine",							"            &a&l- <flag> : &c<value>"),
		REGION_INFO_ONE_HERITAGE_FLAGS_DEFAULT("regionInfoOneHeritageFlagsDefault",						"        &6&l●   &6Default:"),
		REGION_INFO_ONE_HERITAGE_FLAGS_MEMBER("regionInfoOneHeritageFlagsMember",						"        &6&l●   &6Member:"),
		REGION_INFO_ONE_HERITAGE_FLAGS_OWNER("regionInfoOneHeritageFlagsOwner",							"        &6&l●   &6Owner:"),
		REGION_INFO_LIST_TITLE("regionInfoListTitle",													"&aListe des régions"),
		REGION_INFO_LIST_LINE("regionInfoListLine",														"    &6&l➤  &6<region> : [RT]            &7(Type : &a<type>&7, Priorité : &a<priority>&7)"),
		REGION_INFO_EMPTY("regionInfoEmpty",																"&cErreur : Vous n'avez accès aux informations sur ces régions."),
		
		REGION_LIST_DESCRIPTION("regionListDescription",										"Permet de voir la liste des régions dans le monde"),
		REGION_LIST_ALL_TITLE("regionListAllTitle",											"&aLa liste des régions : &6<world>"),
		REGION_LIST_ALL_LINE("regionListAllLine",												"    &6&l➤  &6<region> : [RT]            &7(Type : &a<type>&7, Priorité : &a<priority>&7)"),
		REGION_LIST_ALL_EMPTY("regionListAllEmpty",											"    &7Aucune région"),
		REGION_LIST_PLAYER_TITLE_EQUALS("regionListPlayerTitleEquals",						"&aLa liste de vos régions : &6<world>"),
		REGION_LIST_PLAYER_TITLE_OTHERS("regionListPlayerTitleOthers",						"&aListe des régions : &6<world>"),
		REGION_LIST_PLAYER_LINE("regionListPlayerLine",										"    &6&l➤  &6<region> : [RT]            &7(Type : &a<type>&7, Priorité : &a<priority>&7)"),
		REGION_LIST_PLAYER_EMPTY("regionListPlayerEmpty",										"    &7Aucune région"),
		REGION_LIST_GROUP_TITLE("regionListGroupTitle",										"&aLa liste des régions du groupe &6<group> &7: &6<world>"),
		REGION_LIST_GROUP_LINE("regionListGroupLine",											"    &6&l➤  &6<region> : [RT]            &7(Type : &a<type>&7, Priorité : &a<priority>&7)"),
		REGION_LIST_GROUP_EMPTY("regionListGroupLine",										"    &7Aucune région"),
		
		REGION_DEFINE_DESCRIPTION("regionDefineDescription",									"Permet de définir une nouvelle région"),
		REGION_DEFINE_CUBOID_CREATE("regionDefineCuboidCreate",								"&7Vous venez de créer la région &6<points> &7de type &6<type>&7."),
		REGION_DEFINE_CUBOID_POINTS("regionDefineCuboidPoints",								"&6<region>"),
		REGION_DEFINE_CUBOID_POINTS_HOVER("regionDefineCuboidPointsHover",					"&6Min : (&c<min_x>&6, &c<min_y>&6, &c<min_z>&6)[RT]&6Max : (&c<max_x>&6, &c<max_y>&6, &c<max_z>&6)"),
		REGION_DEFINE_CUBOID_ERROR_POSITION("regionDefineCuboidErrorPosition",				"&cErreur : Vous devez sélectionner 2 positions pour définir une région &6<type>&c."),
		REGION_DEFINE_POLYGONAL_CREATE("regionDefinePolygonalCreate",						"&7Vous venez de créer la région &6<points> &7de type &6<type>&7."),
		REGION_DEFINE_POLYGONAL_POINTS("regionDefinePolygonalPoints",						"&6<region>"),
		REGION_DEFINE_POLYGONAL_POINTS_HOVER("regionDefinePolygonalPointsHover",				"&6Les positions : [RT]<positions>"),
		REGION_DEFINE_POLYGONAL_POINTS_HOVER_LINE("regionDefinePolygonalPointsHoverLine",	"&6#<num> : (&c<x>&6, &c<y>&6, &c<z>&6)"),
		REGION_DEFINE_POLYGONAL_POINTS_HOVER_JOIN("regionDefinePolygonalPointsHoverJoin",	"[RT]"),
		REGION_DEFINE_POLYGONAL_ERROR_POSITION("regionDefinePolygonalErrorPosition",			"&cErreur : Vous devez sélectionner au moins 2 positions pour définir une région &6<type>&c."),
		REGION_DEFINE_TEMPLATE_CREATE("regionDefineTemplateCreate",							"&7Vous venez de créer la région &6<region> &7de type &6<type>&7."),
		REGION_DEFINE_ERROR_IDENTIFIER_EQUALS("regionDefineErrorIdentifierEquals",			"&cErreur : &6<region> &cexiste déjà."),
		REGION_DEFINE_ERROR_IDENTIFIER_INVALID("regionDefineCuboidErrorIdentifierInvalid",	"&cErreur : Le nom de région &6'<region>' &cest invalide."),
		REGION_DEFINE_ERROR_SELECT_TYPE("regionDefineErrorSelectType",						"&cErreur : Impossible de créer une région de type &6<type>&c."),
		
		REGION_REDEFINE_DESCRIPTION("regionRedefineDescription",									"Permet de redéfinir une région"),
		REGION_REDEFINE_CUBOID_CREATE("regionRedefineCuboidCreate",								"&7Vous venez de redéfinir la région &6<region> &7en type &6<type>&7."),
		REGION_REDEFINE_CUBOID_POINTS("regionRedefineCuboidPoints",								"&6(&c<min_x>&6, &c<min_y>&6, &c<min_z>&6) (&c<max_x>&6, &c<max_y>&6, &c<max_z>&6)"),
		REGION_REDEFINE_CUBOID_POINTS_HOVER("regionRedefineCuboidPointsHover",					"&6Min : (&c<min_x>&6, &c<min_y>&6, &c<min_z>&6)[RT]&6Max : (&c<max_x>&6, &c<max_y>&6, &c<max_z>&6)"),
		REGION_REDEFINE_CUBOID_ERROR_POSITION("regionRedefineCuboidErrorPosition",				"&cErreur : Vous devez sélectionner 2 positions pour redéfinir la région &6<region>&c."),
		REGION_REDEFINE_POLYGONAL_CREATE("regionRedefinePolygonalCreate",						"&7Vous venez de redéfinir la région &6<region> &7de type &6<type>."),
		REGION_REDEFINE_POLYGONAL_POINTS("regionRedefinePolygonalPoints",						"&6(&c<min_x>&6, &c<min_y>&6, &c<min_z>&6) (&c<max_x>&6, &c<max_y>&6, &c<max_z>&6)"),
		REGION_REDEFINE_POLYGONAL_POINTS_HOVER("regionRedefinePolygonalPointsHover",				"&6Les positions : [RT]<positions"),
		REGION_REDEFINE_POLYGONAL_POINTS_HOVER_LINE("regionRedefinePolygonalPointsHoverLine",	"&6#<num> : (&c<x>&6, &c<y>&6, &c<z>&6)"),
		REGION_REDEFINE_POLYGONAL_POINTS_HOVER_JOIN("regionRedefinePolygonalPointsHoverJoin",	"[RT]"),
		REGION_REDEFINE_POLYGONAL_ERROR_POSITION("regionRedefinePolygonalErrorPosition",			"&cErreur : Vous devez sélectionner au moins 3 positions pour redéfinir la région &6<region>&c."),
		REGION_REDEFINE_TEMPLATE_CREATE("regionRedefineTemplateCreate",							"&7Vous venez de redéfinir la région &6<region> &7de type &6<type>."),
		REGION_REDEFINE_ERROR_GLOBAL("regionRedefineErrorGlobal",									"&cErreur : Impossible de redéfinir la région &6<region> &ccar elle est de type &6<type>&c."),
		REGION_REDEFINE_ERROR_SELECT_TYPE("regionRedefineErrorSelectType",						"&cErreur : Impossible de redéfinir une région en type &6<type>&c."),
		
		REGION_RENAME_DESCRIPTION("regionRenameDescription",									"Renomme une région"),
		REGION_RENAME_SET("regionRenameSet",													"&7Vous avez renommé la région &6<region> &7en &6<identifier> &7dans le monde &6<world>&7."),
		REGION_RENAME_ERROR_IDENTIFIER_EQUALS("regionRenameErrorIdentifierEquals",			"&cErreur : Impossible de renommer la région &6<region> &cen &6<identifier> &ccar une autre région porte déjà ce nom."),
		REGION_RENAME_ERROR_IDENTIFIER_INVALID("regionRenameErrorIdentifierInvalid",			"&cErreur : Impossible de renommer la région &6<region> &cen &6<identifier> &ccar le nom est invalide."),
		REGION_RENAME_ERROR_GLOBAL("regionRenameErrorGlobal",									"&cErreur : Impossible de renommer la région &6<region> &cde type &6<type>&c."),
		
		REGION_FLAGS_DESCRIPTION("regionFlagsDescription",									"Affiche la liste des flags"),
		REGION_FLAGS_LIST_TITLE("regionFlagsListTitle",										"&aListe des flags"),
		REGION_FLAGS_LIST_LINE("regionFlagsListLine",											"    &6&l➤  &6<flag> : &7<description>"),
		REGION_FLAGS_MESSAGE("regionFlagsMessage",											"&6<flag> : &7<description>"),
		
		REGION_FLAG_ADD_DESCRIPTION("regionAddflagDescription",								"Défini un flag d'une région"),
		REGION_FLAG_ADD_PLAYER("regionAddflagPlayer",											"&7Vous avez défini le flag &6<flag> &7sur la région &6<region> &7dans le monde &6<world> &7pour le groupe &6<group> &7avec la valeur &6<value>&7."),
		REGION_FLAG_ADD_ERROR("regionAddflagError",											"&cErreur : La valeur est &6'<value>&6' &cest invalide."),
		
		REGION_FLAG_REMOVE_DESCRIPTION("regionRemoveflagDescription",							"Supprime un flag d'une région"),
		REGION_FLAG_REMOVE_UPDATE("regionRemoveflagUpdate",									"&7Vous avez défini le flag &6<flag> &7sur la région &6<region> &7dans le monde &6<world> &7pour le groupe &6<group> &7avec la valeur &6<value>&7."),
		REGION_FLAG_REMOVE_PLAYER("regionRemoveflagPlayer",									"&7Vous avez supprimé le flag &6<flag> &7sur la région &6<region> &7dans le monde &6<world> &7pour le groupe &6<group>&7."),
		REGION_FLAG_REMOVE_EMPTY("regionRemoveflagEmpty",										"&cErreur : Il n'y a pas de flag &6<flag> &csur la région &6<region> &cdans le monde &6<world> &cpour le groupe &6<group>&c."),
		REGION_FLAG_REMOVE_ERROR("regionRemoveflagError",										"&cErreur : La valeur est &6'<value>&6' &cest invalide."),
		
		REGION_OWNER_ADD_DESCRIPTION("regionAddownerDescription",								"Ajoute un owner à une région"),
		REGION_OWNER_ADD_PLAYER("regionAddownerPlayer",										"&7Vous avez ajouté le joueur &6<player> &7en tant que &6OWNER &7de la région &6<region>&7."),
		REGION_OWNER_ADD_PLAYER_ERROR("regionAddownerPlayerError",							"&cErreur : Le joueur &6<player> &cest déjà &6OWNER &cde la région &6<region>&c."),
		REGION_OWNER_ADD_PLAYERS("regionAddownerPlayers",										"&7Vous avez ajouté les joueur(s) &6<players> &7en tant que &6OWNER &7de la région &6<region>&7."),
		REGION_OWNER_ADD_PLAYERS_JOIN("regionAddownerPlayersJoin",							"&7, &6"),
		REGION_OWNER_ADD_ERROR_MAX("regionAddownerErrorMax",									"&cErreur : La région &6<region> &ccontient déjà le nombre maximum de &6OWNER&c."),
		REGION_OWNER_ADD_GROUP("regionAddownerGroup",											"&7Vous avez ajouté le groupe &6<group> &7en tant que &6OWNER &7de la région &6<region>&7."),
		REGION_OWNER_ADD_GROUP_ERROR("regionAddownerGroupError",								"&cErreur : Le groupe &6<group> &cest déjà &6OWNER &cde la région &6<region>&c."),
		REGION_OWNER_ADD_GROUPS("regionAddownerGroups",										"&7Vous avez ajouté les groupes &6<groups> &7en tant que &6OWNER &7de la région &6<region>&7."),
		REGION_OWNER_ADD_GROUPS_JOIN("regionAddownerGroupsJoin",								"&7, &6"),
		
		REGION_OWNER_REMOVE_DESCRIPTION("regionRemoveownerDescription",						"Supprime un owner à une région"),
		REGION_OWNER_REMOVE_PLAYER("regionRemoveownerPlayer",									"&7Vous avez supprimé le joueur &6<player> &7en tant que &6OWNER &7de la région &6<region>&7."),
		REGION_OWNER_REMOVE_PLAYER_ERROR("regionRemoveownerPlayerError",						"&cErreur : Le joueur &6<player> &cn'est pas &6OWNER &cde la région &6<region>&c."),
		REGION_OWNER_REMOVE_PLAYERS("regionRemoveownerPlayers",								"&7Vous avez supprimé les joueur(s) &6<players> &7en tant que &6OWNER &7de la région &6<region>&7."),
		REGION_OWNER_REMOVE_PLAYERS_JOIN("regionRemoveownerPlayersJoin",						"&7, &6"),
		REGION_OWNER_REMOVE_GROUP("regionRemoveownerGroup",									"&7Vous avez supprimé le groupe &6<group> &7en tant que &6OWNER &7de la région &6<region>&7."),
		REGION_OWNER_REMOVE_GROUP_ERROR("regionRemoveownerGroupError",						"&cErreur : Le groupe &6<group> &cn'est pas &6OWNER &cde la région &6<region>&c."),
		REGION_OWNER_REMOVE_GROUPS("regionRemoveownerGroups",									"&7Vous avez supprimé les groupes &6<groups> &7en tant que &6OWNER &7de la région &6<region>&7."),
		REGION_OWNER_REMOVE_GROUPS_JOIN("regionRemoveownerGroupsJoin",						"&7, &6"),

		REGION_MEMBER_ADD_DESCRIPTION("regionAddmemberDescription",							"Ajoute un member à une région"),
		REGION_MEMBER_ADD_PLAYER("regionAddmemberPlayer",										"&7Vous avez ajouté le joueur &6<player> &7en tant que &6MEMBER &7de la région &6<region>&7."),
		REGION_MEMBER_ADD_PLAYER_ERROR("regionAddmemberPlayerError",							"&cErreur : Le joueur &6<player> &cest déjà &6MEMBER &cde la région &6<region>&c."),
		REGION_MEMBER_ADD_PLAYERS("regionAddmemberPlayers",									"&7Vous avez ajouté les joueur(s) &6<players> &7en tant que &6MEMBER &7de la région &6<region>&7."),
		REGION_MEMBER_ADD_PLAYERS_JOIN("regionAddmemberPlayersJoin",							"&7, &6"),
		REGION_MEMBER_ADD_ERROR_MAX("regionAddmemberErrorMax",								"&cErreur : La région &6<region> &ccontient déjà le nombre maximum de &6MEMBER&c."),
		REGION_MEMBER_ADD_GROUP("regionAddmemberGroup",										"&7Vous avez ajouté le groupe &6<group> &7en tant que &6MEMBER &7de la région &6<region>&7."),
		REGION_MEMBER_ADD_GROUP_ERROR("regionAddmemberGroupError",							"&cErreur : Le groupe &6<group> &cest déjà &6MEMBER &cde la région &6<region>&c."),
		REGION_MEMBER_ADD_GROUPS("regionAddmemberGroups",										"&7Vous avez ajouté les groupes &6<groups> &7en tant que &6MEMBER &7de la région &6<region>&7."),
		REGION_MEMBER_ADD_GROUPS_JOIN("regionAddmemberGroupsJoin",							"&7, &6"),
		
		REGION_MEMBER_REMOVE_DESCRIPTION("regionRemovememberDescription",						"Supprime un member à une région"),
		REGION_MEMBER_REMOVE_PLAYER("regionRemovememberPlayer",								"&7Vous avez supprimé le joueur &6<player> &7en tant que &6MEMBER &7de la région &6<region>&7."),
		REGION_MEMBER_REMOVE_PLAYER_ERROR("regionRemovememberPlayerError",					"&cErreur : Le joueur &6<player> &cn'est pas &6MEMBER &cde la région &6<region>&c."),
		REGION_MEMBER_REMOVE_PLAYERS("regionRemovememberPlayers",								"&7Vous avez supprimé les joueur(s) &6<players> &7en tant que &6MEMBER &7de la région &6<region>&7."),
		REGION_MEMBER_REMOVE_PLAYERS_JOIN("regionRemovememberPlayersJoin",					"&7, &6"),
		REGION_MEMBER_REMOVE_GROUP("regionRemovememberGroup",									"&7Vous avez supprimé le groupe &6<group> &7en tant que &6MEMBER &7de la région &6<region>&7."),
		REGION_MEMBER_REMOVE_GROUP_ERROR("regionRemovememberGroupError",						"&cErreur : Le groupe &6<group> &cn'est pas &6MEMBER &cde la région &6<region>&c."),
		REGION_MEMBER_REMOVE_GROUPS("regionRemovememberGroups",								"&7Vous avez supprimé les groupes &6<groups> &7en tant que &6MEMBER &7de la région &6<region>&7."),
		REGION_MEMBER_REMOVE_GROUPS_JOIN("regionRemovememberGroupsJoin",						"&7, &6"),
		
		REGION_PARENT_DESCRIPTION("regionParentDescription",									"Défini le parent d'une région"),
		REGION_PARENT_SET("regionParentSet",													"&7Vous avez défini la région &6<parent> &7en tant que parent de la région &6<region>&7."),
		REGION_PARENT_SET_HERITAGE("regionParentSetHeritage",									"&7Vous avez défini la région &6<parent> &7en tant que parent de la région &6<region> : [RT]<heritage>"),
		REGION_PARENT_SET_HERITAGE_LINE("regionParentSetHeritageLine",						"    &c└ <region> : &7<type>"),
		REGION_PARENT_SET_HERITAGE_PADDING("regionParentSetHeritagePadding",					"  "),
		REGION_PARENT_SET_CIRCULAR("regionParentSetCircular",									"&cErreur : Impossible de définir la région &6<parent> &cen tant que parent de &6<region>&c."),
		REGION_PARENT_SET_EQUALS("regionParentSetEquals",										"&cErreur : Impossible de définir une région parent."),
		REGION_PARENT_SET_EQUALS_PARENT("regionParentSetEqualsParent",						"&cErreur : La région &6<parent> &cest déjà la région parent de &6<region>&c."),
		REGION_PARENT_REMOVE("regionParentRemove",											"&7Vous avez supprimé le parent de la région &6<region>&7."),
		REGION_PARENT_REMOVE_EMPTY("regionParentRemoveEmpty",									"&cErreur : La région &6<region> &cn'a déjà aucun parent."),
		
		REGION_PRIORITY_DESCRIPTION("regionPriorityDescription",								"Défini la priorité d'une région"),
		REGION_PRIORITY_SET("regionPrioritySet",												"&7Vous avez défini la priorité de la région &6<region> &7à &6<priority>&7."),
		
		REGION_REMOVE_DESCRIPTION("regionRemoveDescription",									"Supprime une région"),
		REGION_REMOVE_REGION("regionRemoveRegion",											"&7Vous avez supprimé la région &6<region> &7dans le monde &6<world>&7."),
		REGION_REMOVE_CHILDREN_REMOVE("regionRemoveChildrenRemove",							"&7Vous avez supprimé la région &6<region> &7et ces enfants &7dans le monde &6<world>&7."),
		REGION_REMOVE_CHILDREN_UNSET("regionRemoveChildrenUnset",								"&7Vous avez supprimé la région &6<region> &7et gardé ces enfants &7dans le monde &6<world>&7."),
		REGION_REMOVE_ERROR_GLOBAL("regionRemoveErrorGlobal",									"&cErreur : Impossible de supprimer la région &6<region> &ccar elle est de type &6<type>&c."),
		REGION_REMOVE_ERROR_CHILDREN("regionRemoveErrorChildren",								"&cErreur : La région &6<region> &cpossède au moins une région enfant :[RT]"
																							  + "    -f : Permet de supprimer aussi les régions enfants[RT]"
																							  + "    -u : Permet de supprimer uniquement la région parent"),
		
		REGION_TELEPORT_DESCRIPTION("regionTeleportDescription",								"Téléporte le joueur à la région séléctionnée"),
		REGION_TELEPORT_TELEPORT("regionTeleportTeleport",									"&7Vous avez été <position> &7à la région &6<region>&7."),
		REGION_TELEPORT_TELEPORT_POSITION("regionTeleportTeleportPosition",					"&6téléporté"),
		REGION_TELEPORT_TELEPORT_POSITION_HOVER("regionTeleportTeleportPositionHover",		"&7World : &6<world>[RT]&7X : &6<x>[RT]&7Y : &6<y>[RT]&7Z : &6<z>"),
		REGION_TELEPORT_TELEPORT_ERROR("regionTeleportTeleportError",							"&cErreur : La position de téléportation de la région &6<region> &cest invalide."),
		REGION_TELEPORT_SPAWN("regionTeleportSpawn",											"&7Vous avez été téléporté <position> &7de la région &6<region>&7."),
		REGION_TELEPORT_SPAWN_POSITION("regionTeleportSpawnPosition",							"&6spawn"),
		REGION_TELEPORT_SPAWN_POSITION_HOVER("regionTeleportSpawnPositionHover",				"&7World : &6<world>[RT]&7X : &6<x>[RT]&7Y : &6<y>[RT]&7Z : &6<z>"),
		REGION_TELEPORT_SPAWN_ERROR("regionTeleportSpawnError",								"&cErreur : La position du spawn de la région &6<region> &cest invalide."),
		REGION_TELEPORT_SPAWN_EMPTY("regionTeleportSpawnEmpty",								"&cErreur : La région &6<region> &cn'a aucun spawn."),
		
		REGION_SELECT_DESCRIPTION("regionSelectDescription",									"Permet de sélectionner une région"),
		REGION_SELECT_CUBOID("regionSelectCuboid",											"&7Vous venez de sélectionner la région &6<positions> &7de type &6<type>&7."),
		REGION_SELECT_CUBOID_POINTS("regionSelectCuboidPoints",								"&6<region>"),
		REGION_SELECT_CUBOID_POINTS_HOVER("regionSelectCuboidPointsHover",					"&6Min : (&c<min_x>&6, &c<min_y>&6, &c<min_z>&6)[RT]&6Max : (&c<max_x>&6, &c<max_y>&6, &c<max_z>&6)"),
		REGION_SELECT_POLYGONAL("regionSelectPolygonal",										"&7Vous venez de sélectionner la région &6<positions> &7de type &6<type>&7."),
		REGION_SELECT_POLYGONAL_POINTS("regionSelectPolygonalPoints",							"&6<region>"),
		REGION_SELECT_POLYGONAL_POINTS_HOVER("regionSelectPolygonalPointsHover",				"&6Les positions : [RT]<positions>"),
		REGION_SELECT_POLYGONAL_POINTS_HOVER_LINE("regionSelectPolygonalPointsHoverLine",		"&6#<num> : (&c<x>&6, &c<y>&6, &c<z>&6)"),
		REGION_SELECT_POLYGONAL_POINTS_HOVER_JOIN("regionSelectPolygonalPointsHoverJoin",		"[RT]"),
		REGION_SELECT_GLOBAL("regionSelectGlobal",											"&cErreur : Impossible de sélectionner une région de type &6<type>&c."),
		REGION_SELECT_TEMPLATE("regionSelectTemplate",										"&cErreur : Impossible de sélectionner une région de type &6<type>&c."),
		
		
		FLAG_BLOCK_PLACE_DESCRIPTION("flagBlockPlaceDescription",				"Autorise/Interdit de placer des blocs"),
		FLAG_BLOCK_PLACE_MESSAGE("flagBlockPlaceMessage",						EMessageFormat.builder()
																					.actionbarMessageString("&cPlacer des blocs de <block> est interdit (<x>, <y>, <z>)")
																					.actionbarStay(3 * 1000)
																					.actionbarPriority(Priorities.FLAG)),
		
		FLAG_BLOCK_BREAK_DESCRIPTION("flagBlockBreakDescription",				"Autorise/Interdit de détruire des blocs"),
		FLAG_BLOCK_BREAK_MESSAGE("flagBlockBreakMessage",						EMessageFormat.builder()
																					.actionbarMessageString("&cCasser des blocs de <block> est interdit (<x>, <y>, <z>)")
																					.actionbarStay(3 * 1000)
																					.actionbarPriority(Priorities.FLAG)),
		
		FLAG_BUILD_DESCRIPTION("flagBuildDescription",						"Autorise/Interdit les constructions"),
		FLAG_BUILD_MESSAGE("flagBuildMessage",								EMessageFormat.builder()
																					.actionbarMessageString("&cConstruction désactivée (<x>, <y>, <z>)")
																					.actionbarStay(3 * 1000)
																					.actionbarPriority(Priorities.FLAG)),
				
		FLAG_CHAT_DESCRIPTION("flagChatSendDescription",						"Active/Désactive le chat"),
		FLAG_CHAT_SEND_MESSAGE("flagChatSendSendMessage",						"&cL'envoi de message est désactivé dans cette région (<x>, <y>, <z>)"),
		
		FLAG_COMMAND_DESCRIPTION("flagCommandsDescription",					"Active/Désactive l'execution des commandes"),
		FLAG_COMMAND_MESSAGE("flagCommandsMessage",							"&cLa commande <command> est désactivé dans cette région (<x>, <y>, <z>)"),
		
		FLAG_DAMAGE_ENTITY_DESCRIPTION("flagDamageEntityDescription",			"Active/Désactive les dégats infligés aux entités"),
		FLAG_DAMAGE_ENTITY_MESSAGE("flagDamageEntityMessage",					EMessageFormat.builder()
																					.actionbarMessageString("&cLes dégats infligés aux entités sont désactivé (<x>, <y>, <z>)")
																					.actionbarStay(3 * 1000)
																					.actionbarPriority(Priorities.FLAG)),
		
		FLAG_ENDERDRAGON_GRIEF_DESCRIPTION("flagEnderDragonGriefDescription",	"Active/Désactive la destruction des blocs par l'EnderDragon"),
		
		FLAG_ENDERPEARL_DESCRIPTION("flagEnderpearlDescription",				"Active/Désactive la téléportation avec une enderperle"),
		FLAG_ENDERPEARL_MESSAGE("flagEnderpearlMessage",						EMessageFormat.builder()
																					.actionbarMessageString("&cL'utilisation des enderperles est désactivé (<x>, <y>, <z>)")
																					.actionbarStay(3 * 1000)
																					.actionbarPriority(Priorities.FLAG)),
		
		FLAG_ENTITY_DAMAGE_DESCRIPTION("flagEntityDamageDescription",			"Active/Désactive les dégats infligés par une entité"),
		
		FLAG_ENDERMAN_GRIEF_DESCRIPTION("flagEndermanGriefDescription",		"Active/Désactive la destruction des blocs de l'Enderman"),
			
		FLAG_INTERACT_BLOCK_DESCRIPTION("flagInteractBlockDescription",		"Active/Désactive l'interaction avec les blocs"),
		FLAG_INTERACT_BLOCK_MESSAGE("flagInteractBlockMessage",				EMessageFormat.builder()
																					.actionbarMessageString("&cL'Interaction avec les blocs est désactivé (<x>, <y>, <z>)")
																					.actionbarStay(3 * 1000)
																					.actionbarPriority(Priorities.FLAG)),
		
		FLAG_INTERACT_ENTITY_DESCRIPTION("flagInteractEntityDescription",		"Active/Désactive les interactions avec les entités"),
		FLAG_INTERACT_ENTITY_MESSAGE("flagInteractEntityMessage",				EMessageFormat.builder()
																					.actionbarMessageString("&cL'Interaction avec les entités est désactivé (<x>, <y>, <z>)")
																					.actionbarStay(3 * 1000)
																					.actionbarPriority(Priorities.FLAG)),
		
		FLAG_ENTRY_DESCRIPTION("flagEntryDescription",						"Autorise/Interdit le joueur d'entrer dans la région"),
		
		FLAG_ENTRY_MESSAGE_DESCRIPTION("flagEntryMessageDescription",			"Autorise/Interdit le joueur d'entrer dans la région"),
		
		FLAG_ENTRY_DENY_MESSAGE_DESCRIPTION("flagEntryDenyMessageDescription","Interdit le joueur d'entrer dans la région"),
		FLAG_ENTRY_DENY_MESSAGE_DEFAULT("flagEntryDenyMessageDefault",		EMessageFormat.builder()
																					.actionbarMessageString("&cImpossible d'entrer dans la région (<region>)")
																					.actionbarStay(3 * 1000)
																					.actionbarPriority(Priorities.FLAG)),
		
		FLAG_EXIT_DESCRIPTION("flagExitDescription",							"Autorise/Interdit le joueur de sortir dans la région"),
		
		FLAG_EXIT_MESSAGE_DESCRIPTION("flagExitMessageDescription",			"Autorise/Interdit le joueur de sortir dans la région"),
		
		FLAG_EXIT_DENY_MESSAGE_DESCRIPTION("flagExitDenyMessageDescription",	"Interdit le joueur de sortir dans la région"),
		FLAG_EXIT_DENY_MESSAGE_DEFAULT("flagExitDenyMessageDefault",			EMessageFormat.builder()
																					.actionbarMessageString("&cImpossible de sortir dans la région (<region>)")
																					.actionbarStay(3 * 1000)
																					.actionbarPriority(Priorities.FLAG)),
		
		FLAG_EXP_DROP_DESCRIPTION("flagExpDropDescription",					"Active/Désactive la perte d'expérience au sol"),
		
		FLAG_EXPLOSION_DESCRIPTION("flagExplosionDescription",				"Active/Désactive les explosions dans la région"),
		
		FLAG_EXPLOSION_BLOCK_DESCRIPTION("flagExplosionBlockDescription",		"Active/Désactive la destruction des blocs lors d'une explosion"),
		
		FLAG_EXPLOSION_DAMAGE_DESCRIPTION("flagExplosionDamageDescription",	"Active/Désactive les dégats lors d'une explosion"),
		
		FLAG_FIRE_DESCRIPTION("flagFireDescription",							"Active/Désactive le fait d'allumer un feu"),
		
		FLAG_FIRE_MESSAGE("flagFireMessage",									EMessageFormat.builder()
																					.actionbarMessageString("&cLe feu est désactivé (<x>, <y>, <z>)")
																					.actionbarStay(3 * 1000)
																					.actionbarPriority(Priorities.FLAG)),
		
		FLAG_ICE_DESCRIPTION("flagIceDescription",							"Active/Désactive la formation de glace"),
		
		FLAG_KEEP_INVENTORY_DESCRIPTION("flagKeekInventoryDescription",		"Active/Désactive la perte de l'inventaire"),
		
		FLAG_INVINCIBILITY_DESCRIPTION("flagInvincibityDescription",			"Active/Désactive l'invincibilité"),
		
		FLAG_ITEM_DROP_DESCRIPTION("flagItemDropDescription",					"Active/Désactive le fait de jeter des objets"),
		FLAG_ITEM_DROP_MESSAGE("flagItemDropMessage",							EMessageFormat.builder()
																					.actionbarMessageString("&cJeter les objets de <item> est interdit (<x>, <y>, <z>)")
																					.actionbarStay(3 * 1000)
																					.actionbarPriority(Priorities.FLAG)),
		
		FLAG_ITEM_PICKUP_DESCRIPTION("flagItemPickupDescription",				"Active/Désactive le fait de rammasser des objets"),
		FLAG_ITEM_PICKUP_MESSAGE("flagItemPickupMessage",						EMessageFormat.builder()
																					.actionbarMessageString("&cRamasser les objets de <item> est interdit (<x>, <y>, <z>)")
																					.actionbarStay(3 * 1000)
																					.actionbarPriority(Priorities.FLAG)),
		
		
		FLAG_LIGHTNING_DESCRIPTION("flagInvincibityDescription",				"Active/Désactive la foudre"),
		
		FLAG_POTION_SPLASH_DESCRIPTION("flagPotionSplashDescription",				"Active/Désactive les potions splash"),
		FLAG_POTION_SPLASH_MESSAGE("flagPotionSplashMessage",						EMessageFormat.builder()
																					.actionbarMessageString("&cL'utilisation des potions <potion> sont désactivé (<x>, <y>, <z>)")
																					.actionbarStay(3 * 1000)
																					.actionbarPriority(Priorities.FLAG)),
		
		FLAG_PROPAGATION_DESCRIPTION("flagPropagationDescription",			"Active/Désactive la propagation"),
		FLAG_PROPAGATION_MESSAGE("flagPropagationMessage",					EMessageFormat.builder()
																					.actionbarMessageString("&cPropagation désactivé (<x>, <y>, <z>)")
																					.actionbarStay(3 * 1000)
																					.actionbarPriority(Priorities.FLAG)),
		
		FLAG_PVP_DESCRIPTION("flagPvpDescription",							"Active/Désactive le PVP"),
		FLAG_PVP_MESSAGE("flagPvpMessage",									EMessageFormat.builder()
																					.actionbarMessageString("&cPVP désactivé (<x>, <y>, <z>)")
																					.actionbarStay(3 * 1000)
																					.actionbarPriority(Priorities.FLAG)),
		
		FLAG_SNOW_DESCRIPTION("flagSnowDescription",							"Active/Désactive la neige"),
		
		FLAG_TELEPORT_DESCRIPTION("flagTeleportDescription",					"Sauvegarde une position pour se téléporter à la région"),
		
		FLAG_SPAWN_DESCRIPTION("flagSpawnDescription",						"Défini un spawn pour la région"),
		
		FLAG_SPAWN_ENTITY_DESCRIPTION("flagSpawnEntityDescription",			"Active/Désactive l'apparition des entités"),
		FLAG_SPAWN_ENTITY_MESSAGE("flagSpawnEntityMessage",					EMessageFormat.builder()
																					.actionbarMessageString("&cL'apparition de l'entité <entity> est désactivé (<x>, <y>, <z>)")
																					.actionbarStay(3 * 1000)
																					.actionbarPriority(Priorities.FLAG));
		
		private final String path;
	    private final EMessageBuilder french;
	    private final EMessageBuilder english;
	    private EMessageFormat message;
	    private EMessageBuilder builder;
	    
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
	    	Preconditions.checkNotNull(french, "Le message '" + this.name() + "' n'est pas défini");
	    	
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
		
		public EMessageBuilder getBuilder() {
			return this.builder;
		}
		
		public void set(EMessageBuilder message) {
			this.message = message.build();
			this.builder = message;
		}
	}
}
