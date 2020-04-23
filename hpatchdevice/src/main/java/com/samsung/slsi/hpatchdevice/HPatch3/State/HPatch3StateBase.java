package com.samsung.slsi.hpatchdevice.HPatch3.State;

import android.os.Handler;
import android.os.Looper;

import com.samsung.slsi.hpatchdevice.HPatch3.HPatch3Device;
import com.samsung.slsi.hpatchdevice.State;
import com.samsung.slsi.hpatchdevice.StateContext;
import com.samsung.slsi.hpatchdevice.HPatch3.HPatch3State;

public abstract class HPatch3StateBase implements HPatch3State {
    private final String name;
	private final StateContext stateContext;
	protected final HPatch3Device device;
    private final State state;

	public HPatch3StateBase(String name, StateContext stateContext, HPatch3Device device) {
        this.state = this;
        this.name = name;
		this.stateContext = stateContext;
		this.device = device;
	}

	protected State getCurrentState() {
        return stateContext.getCurrentState();
    }
	
	protected void chageState(final String name) {
		stateContext.changeState(name);
	}

	protected void finalState() {
        stateContext.finalState();
    }

    protected void log(String message) {
        device.logOTA(message);
    }

	private final Handler handler = new Handler(Looper.getMainLooper());
	private Runnable timeoutCheckRunnable = new Runnable() {
        @Override
        public void run() {
            log("Timeout for " + name);

            if (getCurrentState() == state) {
                synchronized (handler) {
                    if (timeoutRunnable != null) {
                        log("Handle Timeout for " + name);
                        try {
                            timeoutRunnable.run();
                        } catch (Exception e) {
                            e.printStackTrace();
                            log(e.getLocalizedMessage());
                        }
                    } else {
                        log("Invalid Handler of Timeout for " + name);
                    }
                }
            } else {
                log("Invalid State for Timeout of " + name);
            }
        }
    };
	private Runnable timeoutRunnable = null;

	protected void setTimeout(Runnable runnable, int timeout) {
        synchronized (handler) {
            timeoutRunnable = runnable;
            handler.postDelayed(timeoutCheckRunnable, timeout);
        }
	}

    protected void cancelTimeout() {
        synchronized (handler) {
            timeoutRunnable = null;
            handler.removeCallbacks(timeoutCheckRunnable);
        }
    }

	protected abstract void onEnter();
	protected abstract void onExit();

    @Override
    public String getName() {
        return this.name;
    }

	@Override
	public void enter() {
        log("Enter State: " + name);
        try {
            onEnter();
        } catch (Exception e) {
            e.printStackTrace();
            log("Exception during onEnter of " + name + " State: " + e.getLocalizedMessage());
        }
	}

	@Override
	public void exit() {
        log("Exit State: " + name);

        cancelTimeout();

        try {
            onExit();
        } catch (Exception e) {
            e.printStackTrace();
            log("Exception during onExit of " + name + " State: " + e.getLocalizedMessage());
        }
	}

    @Override
    public void onDisconnected() {
        finalState();
    }
}
