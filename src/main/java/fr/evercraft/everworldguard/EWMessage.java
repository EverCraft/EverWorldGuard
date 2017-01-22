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
		
		SELECT_DESCRIPTION("select.description",					"Permet de sélectionner une région"),
		
		SELECT_INFO_POS("select.info.pos",												"&7(&6<x>&7, &6<y>&7, &6<z>&7)"),
		SELECT_INFO_POS_HOVER("select.info.posHover",									"&7X : &6<x>[RT]&7Y : &6<y>[RT]&7Z : &6<z>"),
		SELECT_INFO_CUBOID_POS1_AND_POS2("select.info.cuboidPos1AndPos2",				"&7Position 1 : <pos1> &7et Position 2 : <pos2> &7(&6<area>&7)"),
		SELECT_INFO_CUBOID_POS1("select.info.cuboidPos1",								"&7Position 1 : <pos>"),
		SELECT_INFO_CUBOID_POS2("select.info.cuboidPos2",								"&7Position 2 : <pos>"),
		SELECT_INFO_CUBOID_EMPTY("select.info.cuboidEmpty",								"&7Aucune position sélectionné"),
		SELECT_INFO_POLY_LINE("select.info.polyLine",									"    &7- <pos>"),
		SELECT_INFO_POLY_TITLE("select.info.polyTitle",									"&7Liste des positions &7(&6<area>&7)"),
		SELECT_INFO_POLY_EMPTY("select.info.polyEmpty",									"&7Aucune position sélectionné"),
		SELECT_INFO_CYLINDER_CENTER_AND_RADIUS("select.info.cylinderCenterAndRadius",	"&7Centre : <pos1> &7et Radius : <pos2> &7(&6<area>&7)"),
		SELECT_INFO_CYLINDER_CENTER("select.info.cylinderCenter",						"&7Centre : <pos>"),
		SELECT_INFO_CYLINDER_RADIUS("select.info.cylinderRadius",						"&7Radius : <pos>"),
		SELECT_INFO_CYLINDER_EMPTY("select.info.cylinderEmpty",							"&7Aucune position sélectionné"),
		
		SELECT_POS1_DESCRIPTION("select.pos1.description",			"Definie la première position"),
		SELECT_POS1_CUBOID_ONE("select.pos1.cuboidOne",				"&7Selection de la première position : <pos>"),
		SELECT_POS1_CUBOID_TWO("select.pos1.cuboidTwo",				"&7Selection de la première position : <pos> &7(&6<area>&7)"),
		SELECT_POS1_POLY("select.pos1.poly",						"&7Selection de la première position : <pos>"),
		SELECT_POS1_CYLINDER_CENTER("select.pos1.cylinderCentor",	"&7Selection du centre : <pos>"),
		SELECT_POS1_EQUALS("select.pos1.equals",					"&cErreur : Vous avez déjà selectionné cette position."),
		SELECT_POS1_CANCEL("select.pos1.cancel",					"&cErreur : Impossible de sélection la position pour le moment."),
		
		SELECT_POS2_DESCRIPTION("select.pos2.description",			"Definie la deuxième position"),
		SELECT_POS2_CUBOID_ONE("select.pos2.cuboidOne",				"&7Selection de la deuxième position : <pos>"),
		SELECT_POS2_CUBOID_TWO("select.pos2.cuboidTwo",				"&7Selection de la deuxième position : <pos> &7(&6<area>&7)"),
		SELECT_POS2_POLY_ONE("select.pos2.polyOne",					"&7Ajoute de la position &6#<num> &7: <pos>"),
		SELECT_POS2_POLY_ALL("select.pos2.polyAll",					"&7Ajoute de la position &6#<num> &7: <pos> &7(&6<area>&7)"),
		SELECT_POS2_RADIUS("select.pos2.radius",					"&7Selection d'un rayon de <radius> &7block(s) : <pos>"),
		SELECT_POS2_NO_CENTER("select.pos2.noCenter",				"&cErreur : Aucune position centrale selectionné."),
		SELECT_POS2_EQUALS("select.pos2.equals",					"&cErreur : Vous avez déjà selectionné cette position."),
		SELECT_POS2_CANCEL("select.pos2.cancel",					"&cErreur : Impossible de sélection la position pour le moment."),
		
		SELECT_CLEAR_DESCRIPTION("select.clear.description",		"Supprime toutes les positions selectionné"),
		SELECT_CLEAR_PLAYER("select.clear.player",					"&7Vous n'avez plus aucune position sélectionné."),
		
		SELECT_REMOVE_DESCRIPTION("select.remove.description",		"Supprime la denière position sélectionné d'un polygone"),
		SELECT_REMOVE_PLAYER("select.remove.player",				"&7Vous avez supprimer la position : <pos>"),
		SELECT_REMOVE_EMPTY("select.remove.empty",					"&4Erreur : Vous n'avez aucune position sélectionné."),
		SELECT_REMOVE_ERROR("select.remove.error",					"&4Erreur : Uniquement pour le type &62D Polygonal&c."),
		
		
		SELECT_EXPAND_DESCRIPTION("select.expand.description",	""),
		
		SELECT_TYPE_DESCRIPTION("select.type.description",		"&7Changé le type de selection"),
		SELECT_TYPE_CUBOID("select.type.cuboid",				"&7Cuboid : clique gauche pour définir le point 1 et clique droit pour définir le point 2.",
																"&7Cuboid: left click for point 1, right for point 2."),
		SELECT_TYPE_POLYGONAL("select.type.poly",				"&72D Polygonal : clique gauche pour définir le premier point et clique droit pour définir les points suivants.",
																"&72D polygon selector: Left/right click to add a point."),
		SELECT_TYPE_CYLINDER("select.type.cylinder",			"&7Cylindrique : clique gauche pour définir le centre, clique droit pour définir le rayon",
																"&7Cylindrical select: Left click=center, right click to extend."),
		SELECT_TYPE_EQUALS("select.type.equals",				"&cErreur : Sélection &6<type> &cdéjà activée"),
		SELECT_TYPE_CANCEL("select.type.cancel",				"&cErreur : Impossible de changé de type de selection pour le moment"),
		
		REGION_DESCRIPTION("region.description",				"Permet de gérer les régions protéger"),
		
		REGION_INFO_DESCRIPTION("region.info.description",		"Permet de voir la liste des régions sur votre position"),
		
		REGION_LIST_DESCRIPTION("region.list.description",		"Permet de voir la liste des régions sur tout le monde");
		
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
