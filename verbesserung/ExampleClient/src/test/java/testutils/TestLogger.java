package testutils;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class TestLogger {

  private static final Logger ROOT_LOGGER = Logger.getLogger("");
  private static boolean configured = false;

  static {
    configure();
  }

  private TestLogger() {}

  private static void configure() {
    if (configured) {
      return;
    }

    ROOT_LOGGER.setLevel(Level.FINE);

    for (var handler : ROOT_LOGGER.getHandlers()) {
      handler.setLevel(Level.FINE);
    }

    configured = true;
  }

  public static Logger getLogger() {
    return ROOT_LOGGER;
  }
}
