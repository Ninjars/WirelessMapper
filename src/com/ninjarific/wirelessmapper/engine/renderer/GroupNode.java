package com.ninjarific.wirelessmapper.engine.renderer;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.PointF;

public class GroupNode extends GraphNode {
	private ArrayList<GraphNode> mChildNodes;
	private PointF mTranslation = new PointF(0,0);
	
	public GroupNode() {
		mChildNodes = new ArrayList<GraphNode>();
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
		c.translate(mTranslation.x, mTranslation.y);
		
		for (GraphNode node : mChildNodes) {
			node.draw(c);
		}
	}
}
