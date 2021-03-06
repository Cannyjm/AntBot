package insectsrobotics.antbotcombiner.Receiver_and_Broadcast;

import android.content.Context;

import insectrobotics.broadcastlibrary.BroadcastValues;
import insectrobotics.broadcastlibrary.Broadcaster;

public class Broadcast extends Broadcaster implements BroadcastValues{

    private static final String TAG = "OCVSample::Activity";


    public Broadcast(Context context) {
        super(context);
    }

    public void requestNewVisualData(int requestCode){
        initiateBroadcast(REQUEST_VN_DATA_BROADCAST);
        onNewDataToBroadcast(requestCode);
        executeBroadcast();
    }

    public void requestNewIntegratorData(){
        initiateBroadcast(REQUEST_PI_DATA_BROADCAST);
        onNewDataToBroadcast(true);
        executeBroadcast();
    }

    public void broadcastDecision(String decision){
        initiateBroadcast(DECISION_BROADCAST);
        onNewDataToBroadcast(decision);
        executeBroadcast();
    }

    public void broadcastTurn(double angle){
        initiateBroadcast(TURN_ANGLE_BROADCAST);
        onNewDataToBroadcast(angle);
        executeBroadcast();
    }



}