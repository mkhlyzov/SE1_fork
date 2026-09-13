package network;

import java.util.logging.Level;
import java.util.logging.Logger;

abstract class AbstractDelayedNtwork implements INetwork{
    protected Logger LOGGER;
    private int GAMESTATE_REQUEST_DELAY = 400;
    private long lastPollTime = 0;

    protected void delayForPolling() {
    long now = System.currentTimeMillis();

    if (lastPollTime == 0) {
      lastPollTime = now;
      return;
    }

    long elapsed = now - lastPollTime;
    long sleepTime = GAMESTATE_REQUEST_DELAY - elapsed;

    if (sleepTime > 0) {
      try {
        Thread.sleep(sleepTime);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        LOGGER.log(Level.WARNING, "Sleep unterbrochen.", e);
      }
    }

    lastPollTime = System.currentTimeMillis();
  }
    
}
