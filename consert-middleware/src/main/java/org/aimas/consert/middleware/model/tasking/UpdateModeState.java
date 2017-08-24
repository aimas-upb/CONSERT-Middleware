package org.aimas.consert.middleware.model.tasking;

import org.aimas.consert.middleware.model.AssertionUpdateMode;

/**
 * Allows to know if the updates are enabled or not
 */
public class UpdateModeState {

	private AssertionUpdateMode updateMode;  // the chosen mode for the updates
	private boolean enabled;  // equals true if the updates are enabled
	
	
	public UpdateModeState(AssertionUpdateMode updateMode, boolean enabled) {
		this.updateMode = updateMode;
		this.enabled = enabled;
	}


	public AssertionUpdateMode getUpdateMode() {
		return updateMode;
	}

	public void setUpdateMode(AssertionUpdateMode updateMode) {
		this.updateMode = updateMode;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
