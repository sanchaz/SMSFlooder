package hiper.flood.sms;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class SMSSendThread extends Thread {

	private volatile SMSFlooderActivity mContext;
	private String phoneNumber;
	private String message;
	private int quantity = 0;
	private int mSentMessages = 0;
	private boolean next;
	private Boolean finished = new Boolean(true);
	private boolean end = false; 
	private BroadcastReceiver receiver = null;


	public void run() {
		//Log.d("Debug", "run 1");
		try {
			synchronized (this) {
				this.wait();
			}
		} catch (InterruptedException e1) {
			// shit just went wrong return
			return;
		}
		//Log.d("Debug", "run 2");
		while(!end) {
			//Log.d("Debug", "run 3");
			while(mSentMessages < quantity) {
				next = false;
				sendSMS(phoneNumber, message);
				while(!next);
			}
			//Log.d("Debug", "run 4");

			try {
				synchronized (finished) {
					finished = true;
				}
				synchronized (this) {
					this.wait();
				}
			} catch (InterruptedException e) {
				//try again
			}
			//Log.d("Debug", "run 5");
		}
		//Log.d("Debug", "run 3");
	}

	private void sendSMS(String phoneNumber, String message) {

		//Log.d("Debug", "sendSMS 1");

		String SENT = "SMS_SENT";

		PendingIntent sentPI = PendingIntent.getBroadcast(mContext, 0, new Intent(SENT), 0);

		//---when the SMS has been sent---
		receiver = new BroadcastReceiver(){
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				switch (getResultCode())
				{
				case Activity.RESULT_OK:
					Toast.makeText(mContext, "SMS " + updateSent(incrementSentMessages()) + " sent", Toast.LENGTH_SHORT).show();
					next = true;
					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					Toast.makeText(mContext, "Generic failure", Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_NO_SERVICE:
					Toast.makeText(mContext, "No service", Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_NULL_PDU:
					Toast.makeText(mContext, "Null PDU", Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_RADIO_OFF:
					Toast.makeText(mContext, "Radio off", Toast.LENGTH_SHORT).show();
					break;
				}
				arg0.unregisterReceiver(this);
			}
		};
		mContext.registerReceiver(receiver, new IntentFilter(SENT));      

		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(phoneNumber, null, message, sentPI, null);

		//Log.d("Debug", "sendSMS 2");
	}

	public synchronized int incrementSentMessages() {
		return ++mSentMessages;
	}

	private int updateSent(int sent) {
		mContext.updateSent(sent + "/" + quantity);
		return sent;
	}

	public void startExecution(String phoneNumber, String message, int quantity) {
		//Log.d("Debug", "startExecution 1");
		synchronized (finished) {
			//Log.d("Debug", "startExecution 2");
			if(!finished) {
				Toast.makeText(mContext, "Still sending last batch...", Toast.LENGTH_SHORT).show();
				return;
			}
			finished = false;
		}
		next = true;
		//Log.d("Debug", "startExecution 3");

		this.phoneNumber = phoneNumber;
		this.message = message;
		this.quantity = quantity;
		this.mSentMessages = 0;
		updateSent(mSentMessages);
		//Log.d("Debug", "startExecution 4");
		synchronized (this) {
			this.notify();
		}
		//Log.d("Debug", "startExecution 5");
	}

	public void end() {
		/*if(receiver != null) {
			mContext.unregisterReceiver(receiver);
		}*/
	}
	
	public void exit() {
		Log.d("Debug", "Thread destroyed!");
		this.interrupt();
		/*if(receiver != null) {
			mContext.unregisterReceiver(receiver);
		}*/
	}

	public boolean isFinished() {
		return finished;
	}

	/*public void reset(SMSFlooderActivity context) {
		this.mContext = context;
		updateSent(mSentMessages);
	}*/
	
	public SMSSendThread(SMSFlooderActivity context) {
		this.mContext = context;
	}

}
