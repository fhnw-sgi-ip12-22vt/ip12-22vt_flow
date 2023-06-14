package ch.fhnw.flow.pi;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class EventHandler {
    public static boolean isHandlingEvents = false;
    public static boolean isStopHandling = false;
    private static int TIMEOUT = 400;
    private static int TICK_TIME = 40;

    public static void start(TCA9548A tca) {
        if(isHandlingEvents) return;
        isHandlingEvents = true;
        isStopHandling = false;
        new Thread(() -> handleEvents(tca)).start();
    }

    public static void handleEvents(TCA9548A tca) {
        Collection<Integer> listenerChannels = tca.getListenerChannels();
        Iterator<Integer> boardIterator = listenerChannels.iterator();
        while (!isStopHandling && listenerChannels.size() > 0) {
            tca.selectChannel(boardIterator.next());
            if(!boardIterator.hasNext()) {
                boardIterator = listenerChannels.iterator();
            }
            boolean isInterrupted = tca.getCurrentGravityBoard().pollInterrupts();
            int sleep = isInterrupted ? TIMEOUT : TICK_TIME;
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                // TODO: log exception
                System.out.println(e.getMessage());
                stop();
            }
        }
        isHandlingEvents = false;
    }

    public static void stop() {
        isStopHandling = true;
    }
}
