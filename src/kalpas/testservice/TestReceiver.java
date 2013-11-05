package kalpas.testservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class TestReceiver extends BroadcastReceiver {
    

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            String sender = Preferences.getSender(context);
            Bundle bundle = intent.getExtras();
            SmsMessage[] msgs = null;
            String msg_from;
            if (bundle != null) {
                try {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    msgs = new SmsMessage[pdus.length];
                    for (int i = 0; i < msgs.length; i++) {
                        msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        msg_from = msgs[i].getOriginatingAddress();

                        if (sender.equals(msg_from)) {
                            String msgBody = msgs[i].getMessageBody();
                            Intent startService = new Intent(context, BackgroundService.class);
                            startService.putExtra(BackgroundService.EXTRA_MESSAGE, msgBody);
                            context.startService(startService);
                        }
                    }
                } catch (Exception e) {
                }
            }
        }
    }

}
