package servent.handler.snapshot;

import app.AppConfig;
import app.CausalBroadcastShared;
import app.ServentInfo;
import app.snapshot_bitcake.SnapshotCollector;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.snapshot.ABTellMessage;
import servent.message.snapshot.LYTellMessage;
import servent.message.util.MessageUtil;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ABTellHandler implements MessageHandler {

    private Message clientMessage;
    private boolean doRebroadcast = false;
    private static Set<Message> receivedBroadcasts = Collections.newSetFromMap(new ConcurrentHashMap<Message, Boolean>());
    private SnapshotCollector snapshotCollector;

    public ABTellHandler(Message clientMessage,boolean doRebroadcast, SnapshotCollector snapshotCollector) {
        this.clientMessage = clientMessage;
        this.doRebroadcast = doRebroadcast;
        this.snapshotCollector = snapshotCollector;
    }



    @Override
    public void run() {
        try {
            if (clientMessage.getMessageType() == MessageType.AB_TELL) {

                ServentInfo senderInfo = clientMessage.getOriginalSenderInfo();
                if (doRebroadcast) {
                    if (senderInfo.getId() == AppConfig.myServentInfo.getId()) {
                        //We are the sender :o someone bounced this back to us. /ignore
                        AppConfig.timestampedStandardPrint("Got own message back. No rebroadcast.");
                    } else {
                        //Try to put in the set. Thread safe add ftw.
                        boolean didPut = receivedBroadcasts.add(clientMessage);


                        if (didPut) {
                            if(((ABTellMessage)clientMessage).getInitiatorID()==AppConfig.myServentInfo.getId()){

                            }
                            //New message for us. Rebroadcast it.

                            CausalBroadcastShared.addPendingMessage(clientMessage);
                            CausalBroadcastShared.checkPendingMessages();
                            AppConfig.timestampedStandardPrint("Rebroadcasting... " + receivedBroadcasts.size());

                            for (Integer neighbor : AppConfig.myServentInfo.getNeighbors()) {
                                //Same message, different receiver, and add us to the route table.
                                MessageUtil.sendMessage(clientMessage.changeReceiver(neighbor).makeMeASender());
                            }


                        } else {
                            //We already got this from somewhere else. /ignore
                            AppConfig.timestampedStandardPrint("Already had this. No rebroadcast.");
                        }
                    }
                }
                else
                {
                    CausalBroadcastShared.addPendingMessage(clientMessage);
                    CausalBroadcastShared.checkPendingMessages();
                }

            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
