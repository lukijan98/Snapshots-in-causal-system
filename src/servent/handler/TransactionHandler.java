package servent.handler;

import app.AppConfig;
import app.CausalBroadcastShared;
import app.ServentInfo;
import app.snapshot_bitcake.AcharyaBadrinathBitcakeManager;
import app.snapshot_bitcake.BitcakeManager;
import app.snapshot_bitcake.LaiYangBitcakeManager;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.snapshot.ABTellMessage;
import servent.message.util.MessageUtil;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TransactionHandler implements MessageHandler {

	private Message clientMessage;
	private BitcakeManager bitcakeManager;

	public TransactionHandler(Message clientMessage, BitcakeManager bitcakeManager) {
		this.clientMessage = clientMessage;
		this.bitcakeManager = bitcakeManager;
	}

	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.TRANSACTION) {
			String amountString = clientMessage.getMessageText();

			int amountNumber = 0;
			try {
				amountNumber = Integer.parseInt(amountString);
			} catch (NumberFormatException e) {
				AppConfig.timestampedErrorPrint("Couldn't parse amount: " + amountString);
				return;
			}
			bitcakeManager.addSomeBitcakes(amountNumber);
			if (bitcakeManager instanceof AcharyaBadrinathBitcakeManager) {
				AcharyaBadrinathBitcakeManager abBitcakeManager = (AcharyaBadrinathBitcakeManager)bitcakeManager;

				abBitcakeManager.recordGetTransaction(clientMessage.getOriginalSenderInfo().getId(), amountNumber);
			}


		} else {
			AppConfig.timestampedErrorPrint("Transaction handler got: " + clientMessage);
		}
	}


}
