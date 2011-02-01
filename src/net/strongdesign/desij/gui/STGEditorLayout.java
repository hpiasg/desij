/**
 * Copyright 2004,2005,2006,2007,2008,2009,2010,2011 Mark Schaefer, Dominic Wist
 *
 * This file is part of DesiJ.
 * 
 * DesiJ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DesiJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DesiJ.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.strongdesign.desij.gui;

import java.awt.*;

import net.strongdesign.stg.*;

import java.util.*;

public class STGEditorLayout {

	private STGEditorLayout() {}
	
	/**
	 * Applies random layout
	 */
	public static STGEditorCoordinates applyRandomLayout(Collection<Node> nodes) {
		STGEditorCoordinates result = new STGEditorCoordinates();
		for (Node node : nodes)
			result.put(node, new Point((int) (Math.random() * 500 + 250),
					(int) (Math.random() * 500 + 250)));
		return result;		
	}
	
	/**
	 * Applies spring layout
	 * @param count # of iterations
	 */
	public static STGEditorCoordinates applySpringLayout(STGEditorCoordinates coordinates, int count) {
		//Attraction of neighboured nodes
		double naturalLength = 100;
		double springConstantAttraction = 0.05;
		double springConstantRepulsion = 0.5;
		
		
		//Repulsion of non-neighboured nodes
		double minimumDistance = 200;
		double repulsionForce = 2;
		double maximumForce = 15;
		
		//Node centering
		double centeringForce = 3;
		
		
		Point minPoint = new Point(10000, 10000);
		
		for (int i=1; i<= count; ++i) {
			
			for (Node node : coordinates.keySet()) { 

				Point position = coordinates.get(node);
				Set<Node> neighbours = node.getNeighbours();
				
				Set<Node> nonNeighbours = new HashSet<Node>();
				for (Node n : coordinates.keySet())
					if (n != node && !neighbours.contains(n))
						nonNeighbours.add(n);
				
				//centering
				Point absDir = new Point();
				
				//Attraction force				
				for (Node child : neighbours) {
					
					Point cPos = coordinates.get(child);
					double dx=cPos.x-position.x;
					double dy=cPos.y-position.y;
					double length = Math.sqrt(dx*dx+dy*dy);
					double force=0;
					if (length > naturalLength)
						force = (length-naturalLength)*springConstantAttraction;
					else if (length < naturalLength)
						force = (length-naturalLength)*springConstantRepulsion;
					
					dx = dx / length;
					dy = dy / length;
					
					position.x = (int) (position.x + dx * force);
					position.y = (int) (position.y + dy * force);
					
					//Centering
					absDir.translate((int) dx, (int) dy);
					
				}
		
				//Repulsion
				for (Node child : nonNeighbours) {
					Point cPos = coordinates.get(child);
					double dx=cPos.x-position.x;
					double dy=cPos.y-position.y;
					double length = Math.sqrt(dx*dx+dy*dy);
					
					
					double force;
					if (length<=minimumDistance)
						force= Math.pow((minimumDistance/length),2)*repulsionForce;
					else
						force= Math.pow((minimumDistance/length),2)*repulsionForce;
					if (force > maximumForce) force = maximumForce;
					force = - force;
					dx /= length;
					dy /= length;
					position.x += (int) (dx * force);
					position.y += (int) (dy * force);
					
				}
				
       		//Centering
				double absLength = Math.sqrt(absDir.x*absDir.x+absDir.y*absDir.y);
				absDir.x /= absLength;
				absDir.y /= absLength;
				double force = absLength*centeringForce;
				position.x += absDir.x*force;
				position.y += absDir.y*force;
				

				
				
				
			if (position.x < minPoint.x) minPoint.x = position.x;
			if (position.y < minPoint.y) minPoint.y = position.y;			
				
			}
			
			for (Node node : coordinates.keySet()) {
				Point pos = coordinates.get(node);
				pos.translate(-minPoint.x+30, -minPoint.y+30);
			}
		}
		
		return coordinates;
	}
		
	
}
