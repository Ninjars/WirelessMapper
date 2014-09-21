package com.ninjarific.wirelessmapper.graphics.renderers;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.PointF;

public class GroupNode extends GraphNode {
	private ArrayList<GraphNode> mChildNodes;
	protected PointF mTranslation;
	
	public GroupNode() {
		mChildNodes = new ArrayList<GraphNode>();
		mTranslation = new PointF(0,0);
	}
	
	public void setTranslation(PointF translation) {
		mTranslation = translation;
	}
	
	public PointF getTranslation() {
		return mTranslation;
	}
	
	public void addChild(GraphNode child) {
		mChildNodes.add(child);
	}
	
	/*
	 * seeks for the passed GraphNode
	 * in this group's hierarchy and removes it.
	 */
	public boolean removeChild(GraphNode child) {
		if (mChildNodes.contains(child)) {
			mChildNodes.remove(child);
			return true;
		
		} else {
			boolean removed = false;
			for (GraphNode n : mChildNodes) {
				if (n instanceof GroupNode) {
					removed = ((GroupNode) n).removeChild(child);
					if (removed) {
						return true;
					}
				}
			}
			
		}
		
		return false;
	}
	
	public void draw(Canvas c) {
		update();
		c.save();
		c.translate(mTranslation.x, mTranslation.y);
		
		for (GraphNode node : mChildNodes) {
			node.draw(c);
		}
		c.restore();
	}
	
	protected void update() {
		// any on-frame update logic can go in overrides of this
		// method, eg getting the translation from an actor in 
		// an actor rendering group.
	}
}
