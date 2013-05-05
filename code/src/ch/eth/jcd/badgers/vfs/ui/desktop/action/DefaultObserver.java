package ch.eth.jcd.badgers.vfs.ui.desktop.action;

import java.awt.Component;

import ch.eth.jcd.badgers.vfs.ui.desktop.controller.DesktopController;
import ch.eth.jcd.badgers.vfs.util.SwingUtil;

public abstract class DefaultObserver implements ActionObserver {

	private final DesktopController desktopController;

	public DefaultObserver(final DesktopController desktopController) {
		this.desktopController = desktopController;
	}

	@Override
	public void onActionFailed(final AbstractBadgerAction action, final Exception e) {
		SwingUtil.handleException((Component) desktopController.getView(), e);
		desktopController.updateGUI();
	}

}
