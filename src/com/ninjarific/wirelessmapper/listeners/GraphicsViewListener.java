package com.ninjarific.wirelessmapper.listeners;

public interface GraphicsViewListener {
	public void onSurfaceCreated();
	public void onSurfaceDestroyed();
	public void attemptThreadReconnect() throws InterruptedException;
}
