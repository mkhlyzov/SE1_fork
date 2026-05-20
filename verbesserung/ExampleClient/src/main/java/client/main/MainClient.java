package client.main;

import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import clientcore.ClientMain;
import engine.GameSimulator;
import util.RandomManager;

public class MainClient {

	// ADDITIONAL TIPS ON THIS MATTER ARE GIVEN THROUGHOUT THE TUTORIAL SESSION!

	/*
	 * Below, you can find an example of how to use both required HTTP operations,
	 * i.e., POST and GET to communicate with the server.
	 * 
	 * Note, this is only an example. Hence, your own implementation should NOT
	 * place all the logic in a single main method!
	 * 
	 * Further, I would recommend that you check out: a) The JavaDoc of the network
	 * message library, which describes all messages, and their CTORs/methods. You
	 * can find it here http://swe1.wst.univie.ac.at/ b) The informal network
	 * documentation is given in Moodle, which describes which messages must be used
	 * when and how.
	 */
	public static void main(String[] args) {
		// RandomManager.setSeed(1773484627773L);
		RandomManager.randomizeSeed();
		Logger root = Logger.getLogger("");
		root.setLevel(Level.FINE);

		for (var h : root.getHandlers()) {
			h.setLevel(Level.FINE);

			h.setFormatter(new Formatter() {
				@Override
				public String format(LogRecord record) {
					String className = record.getSourceClassName();
					String methodName = record.getSourceMethodName();

					return String.format(
							"%1$tH:%1$tM:%1$tS.%1$tL %2$s [%3$s.%4$s] %5$s%n",
							record.getMillis(),
							record.getLevel().getName(),
							className,
							methodName,
							formatMessage(record));
				}
			});
		}
		if (args.length < 3) {
			GameSimulator.main(args);
		} else {
			ClientMain.main(args);
		}

	}

}