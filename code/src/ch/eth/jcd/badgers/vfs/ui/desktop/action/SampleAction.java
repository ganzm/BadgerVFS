package ch.eth.jcd.badgers.vfs.ui.desktop.action;

import ch.eth.jcd.badgers.vfs.core.interfaces.VFSDiskManager;

public class SampleAction extends BadgerAction implements Runnable {

	@Override
	public void runDiskAction(VFSDiskManager diskManager) {

		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
