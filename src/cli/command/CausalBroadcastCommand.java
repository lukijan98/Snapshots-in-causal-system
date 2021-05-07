package cli.command;

import java.util.Map;

import app.AppConfig;
import app.CausalBroadcastShared;
import app.ServentInfo;
import servent.message.CausalBroadcastMessage;
import servent.message.Message;
import servent.message.util.MessageUtil;

public class CausalBroadcastCommand implements CLICommand {

    @Override
    public String commandName() {
        return "causal_broadcast";
    }

    @Override
    public void execute(String args) {
        String msgToSend = "";

        msgToSend = args;

        if (args == null) {
            AppConfig.timestampedErrorPrint("No message to causally broadcast");
            return;
        }

        ServentInfo myInfo = AppConfig.myServentInfo;
        Map<Integer, Integer> myClock = CausalBroadcastShared.getVectorClock();
        Message broadcastMessage = new CausalBroadcastMessage(
                myInfo, null, msgToSend, myClock);
        for (Integer neighbor : AppConfig.myServentInfo.getNeighbors()) {
            //ServentInfo neighborInfo = AppConfig.getInfoById(neighbor);

            /*
             * Causal Broadcast is implemented for clique only, so
             * we don't care about rebroadcast issues the way we do
             * in our regular Broadcast implementation.
             */
            broadcastMessage = broadcastMessage.changeReceiver(neighbor);

            MessageUtil.sendMessage(broadcastMessage);
        }

        /*
         * After broadcasting to others, we can safely commit locally.
         */
        //broadcastMessage.changeReceiver(myInfo.getId());
        Message commitMessage = new CausalBroadcastMessage(
                myInfo, myInfo, msgToSend, myClock);
        CausalBroadcastShared.commitCausalMessage(commitMessage);
    }

}
