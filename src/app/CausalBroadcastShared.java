package app;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.function.BiFunction;

import app.snapshot_bitcake.SnapshotCollector;
import servent.handler.TransactionHandler;
import servent.handler.snapshot.ABTellHandler;
import servent.handler.snapshot.TokenHandler;
import servent.message.Message;
import servent.message.TransactionMessage;
import servent.message.snapshot.ABTellMessage;

/**
 * This class contains shared data for the Causal Broadcast implementation:
 * <ul>
 * <li> Vector clock for current instance
 * <li> Commited message list
 * <li> Pending queue
 * </ul>
 * As well as operations for working with all of the above.
 *
 * @author bmilojkovic
 *
 */
public class CausalBroadcastShared {

    private static Map<Integer, Integer> vectorClock = new ConcurrentHashMap<>();
    private static List<Message> commitedCausalMessageList = new CopyOnWriteArrayList<>();
    private static Queue<Message> pendingMessages = new ConcurrentLinkedQueue<>();
    public static Object pendingMessagesLock = new Object();
    private static Object sendingMessageLock = new Object();
    private static ExecutorService commitedMessagesPoll = Executors.newCachedThreadPool();

    public static void initializeVectorClock(int serventCount) {
        for(int i = 0; i < serventCount; i++) {
            vectorClock.put(i, 0);
        }
    }

    public static void incrementClock(int serventId) {
        vectorClock.computeIfPresent(serventId, new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer key, Integer oldValue) {
                return oldValue+1;
            }
        });
    }

    public static Map<Integer, Integer> getVectorClock() {
        return vectorClock;
    }

    public static List<Message> getCommitedCausalMessages() {
        List<Message> toReturn = new CopyOnWriteArrayList<>(commitedCausalMessageList);

        return toReturn;
    }

    public static void addPendingMessage(Message msg) {
        pendingMessages.add(msg);
    }


    private static boolean otherClockGreater(Map<Integer, Integer> clock1, Map<Integer, Integer> clock2) {
        if (clock1.size() != clock2.size()) {
            throw new IllegalArgumentException("Clocks are not same size how why");
        }

        for(int i = 0; i < clock1.size(); i++) {
            if (clock2.get(i) > clock1.get(i)) {
                return true;
            }
        }

        return false;
    }

    public static boolean removeCommitedMessage(Message message){
        if(commitedCausalMessageList.remove(message))
            return true;
        return false;
    }

    public static void checkPendingMessages(SnapshotCollector snapshotCollector) {
        boolean gotWork = true;

        while (gotWork) {
            gotWork = false;

            synchronized (pendingMessagesLock) {
                Iterator<Message> iterator = pendingMessages.iterator();

                Map<Integer, Integer> myVectorClock = getVectorClock();
                while (iterator.hasNext()) {
                    Message pendingMessage = iterator.next();
                    if (!otherClockGreater(myVectorClock, pendingMessage.getSenderVectorClock())) {
                        gotWork = true;
                        switch (pendingMessage.getMessageType()) {
                            case TOKEN:
                                incrementClock(pendingMessage.getOriginalSenderInfo().getId());
                                commitedMessagesPoll.submit(new TokenHandler(pendingMessage,snapshotCollector.getBitcakeManager()));
                                break;
                            case AB_TELL:
                                if(((ABTellMessage)pendingMessage).getInitiatorID()==AppConfig.myServentInfo.getId())
                                    commitedMessagesPoll.submit(new ABTellHandler(pendingMessage,snapshotCollector));
                                incrementClock(pendingMessage.getOriginalSenderInfo().getId());
                            case TRANSACTION:
                                if(((TransactionMessage)pendingMessage).getIntendedReciver()==AppConfig.myServentInfo.getId())
                                    commitedMessagesPoll.submit(new TransactionHandler(pendingMessage,snapshotCollector.getBitcakeManager()));
                                incrementClock(pendingMessage.getOriginalSenderInfo().getId());
                        }


                        //AppConfig.timestampedStandardPrint("Committing " + pendingMessage);
                        commitedCausalMessageList.add(pendingMessage);

                        iterator.remove();

                        break;
                    }
                }
            }
        }

    }
    public static void shutdown(){commitedMessagesPoll.shutdown();}
}
