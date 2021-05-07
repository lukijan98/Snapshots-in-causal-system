package servent.message.snapshot;

import app.AppConfig;
import app.ServentInfo;

import app.snapshot_bitcake.ABSnapshotResult;
import servent.message.BasicMessage;

import servent.message.CausalBroadcastMessage;
import servent.message.Message;
import servent.message.MessageType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ABTellMessage extends BasicMessage {

    private ABSnapshotResult abSnapshotResult;
    private Map<Integer, Integer> senderVectorClock;
    private int initiatorID;

    public ABTellMessage(ServentInfo sender, ServentInfo receiver, ABSnapshotResult abSnapshotResult, Map<Integer, Integer> senderVectorClock, int initiatorID) {
        super(MessageType.AB_TELL, sender, receiver);
        this.abSnapshotResult = abSnapshotResult;
        this.senderVectorClock = senderVectorClock;
        this.initiatorID = initiatorID;
    }

    private ABTellMessage(ServentInfo originalSenderInfo, ServentInfo receiverInfo,
                                   List<ServentInfo> routeList,String messageText, int messageId, ABSnapshotResult abSnapshotResult,Map<Integer, Integer> senderVectorClock, int initiatorID) {
        super(MessageType.AB_TELL,originalSenderInfo,receiverInfo,routeList, messageText,messageId);
        this.senderVectorClock = senderVectorClock;
        this.abSnapshotResult = abSnapshotResult;
        this.initiatorID = initiatorID;
    }


    @Override
    public Message changeReceiver(Integer newReceiverId) {
        if (AppConfig.myServentInfo.getNeighbors().contains(newReceiverId)) {
            ServentInfo newReceiverInfo = AppConfig.getInfoById(newReceiverId);

            Message toReturn = new ABTellMessage(getOriginalSenderInfo(),
                    newReceiverInfo, getRoute(), getMessageText(), getMessageId(),abSnapshotResult,senderVectorClock,initiatorID);

            return toReturn;
        } else {
            AppConfig.timestampedErrorPrint("Trying to make a message for " + newReceiverId + " who is not a neighbor.");

            return null;
        }

    }

    @Override
    public Message makeMeASender() {
        ServentInfo newRouteItem = AppConfig.myServentInfo;

        List<ServentInfo> newRouteList = new ArrayList<>(getRoute());
        newRouteList.add(newRouteItem);
        Message toReturn = new ABTellMessage(getOriginalSenderInfo(),
                getReceiverInfo(), newRouteList, getMessageText(), getMessageId(),abSnapshotResult,senderVectorClock,initiatorID);

        return toReturn;
    }




    public ABSnapshotResult getABSnapshotResult() {
        return abSnapshotResult;
    }

    public Map<Integer, Integer> getSenderVectorClock() {
        return senderVectorClock;
    }

    public int getInitiatorID() {
        return initiatorID;
    }
}
