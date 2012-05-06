package org.paradroid.adk;

public interface NavigationListenner {
	public void handleNewRangeFinderDistance(int distance);
	public void changeStatus(String textMsg);
	public void handleLight(boolean on);
	public void onUsbConnected(boolean enable);
}
