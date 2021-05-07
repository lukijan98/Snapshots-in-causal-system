package servent.message;

import app.AppConfig;
import app.ServentInfo;
import app.snapshot_bitcake.ABSnapshotResult;
import app.snapshot_bitcake.BitcakeManager;
import app.snapshot_bitcake.LaiYangBitcakeManager;
import servent.message.snapshot.ABTellMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a bitcake transaction. We are sending some bitcakes to another node.
 * 
 * @author bmilojkovic
 *
 */
public class TransactionMessage extends BasicMessage {

	private static final long serialVersionUID = -333251402058492901L;

	private transient BitcakeManager bitcakeManager;
	private Map<Integer, Integer> senderVectorClock;
	private int intendedReciver;
	
	public TransactionMessage(ServentInfo sender, ServentInfo receiver, int amount, BitcakeManager bitcakeManager,  Map<Integer, Integer> senderVectorClock, int intendedReciver) {
		super(MessageType.TRANSACTION, sender, receiver, String.valueOf(amount));
		this.bitcakeManager = bitcakeManager;
		this.senderVectorClock = senderVectorClock;
		this.intendedReciver = intendedReciver;
	}

	private TransactionMessage(ServentInfo originalSenderInfo, ServentInfo receiverInfo,
						  List<ServentInfo> routeList, String messageText, int messageId,  Map<Integer, Integer> senderVectorClock, int intendedReciver) {
		super(MessageType.AB_TELL,originalSenderInfo,receiverInfo,routeList, messageText,messageId);
		this.senderVectorClock = senderVectorClock;
		this.intendedReciver = intendedReciver;
	}

	@Override
	public Message changeReceiver(Integer newReceiverId) {
		if (AppConfig.myServentInfo.getNeighbors().contains(newReceiverId)) {
			ServentInfo newReceiverInfo = AppConfig.getInfoById(newReceiverId);

			Message toReturn = new TransactionMessage(getOriginalSenderInfo(),
					newReceiverInfo, getRoute(), getMessageText(), getMessageId(),senderVectorClock,intendedReciver);

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
		Message toReturn = new TransactionMessage(getOriginalSenderInfo(),
				getReceiverInfo(), newRouteList, getMessageText(), getMessageId(),senderVectorClock,intendedReciver);

		return toReturn;
	}
	
	/**
	 * We want to take away our amount exactly as we are sending, so our snapshots don't mess up.
	 * This method is invoked by the sender just before sending, and with a lock that guarantees
	 * that we are white when we are doing this in Chandy-Lamport.
	 */
	@Override
	public void sendEffect() {
		int amount = Integer.parseInt(getMessageText());
		
		bitcakeManager.takeSomeBitcakes(amount);

	}
}
