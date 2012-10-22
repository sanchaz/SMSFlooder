package hiper.flood.sms;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SMSFlooderActivity extends Activity {

	//Number of menu item
	private static final int MENU_ABOUT = 1;

	private EditText mPhoneNumber;
	private EditText mMessage;
	private EditText mQuantity;
	private TextView mSent;
	private SMSSendThread mSendSMS = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mPhoneNumber = (EditText) findViewById(R.id.phoneNumber);
		mMessage = (EditText) findViewById(R.id.message);
		mQuantity = (EditText) findViewById(R.id.quantityValue);
		mSent = (TextView) findViewById(R.id.sent);
		//Log.d("Debug", "Thread created and stared...");
		if(this == null) {
			Log.d("Debug", "This is null");
		}else{
			Log.d("Debug", "Starting Thread!");
			mSendSMS = new SMSSendThread(this);
			mSendSMS.start();
		}
	}

	@Override
	public void onStart() {
		super.onStart();  
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mSendSMS.exit();
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		final SMSSendThread runningThread = mSendSMS;
		return runningThread;
	}

	@Override
	public void onBackPressed() {
		if(!mSendSMS.isFinished()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("The last SMS batch is still sending.\nExiting will cause the remaing SMS's to not be sent.\nAre you sure you want to exit?")
			.setCancelable(false)
			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					//mSendSMS.exit();
					setResult(RESULT_OK);
					finish();
					return;
				}
			})
			.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					return;
				}
			}).show();
		}else{
			//mSendSMS.exit();
			setResult(RESULT_OK);
			finish();
			return;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		boolean result = super.onCreateOptionsMenu(menu);
		//create menu
		//Configuration option
		MenuItem configuration = menu.add(Menu.NONE, MENU_ABOUT, 1, "About");
		//Set configuration icon
		configuration.setIcon(android.R.drawable.ic_menu_info_details);

		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		//select menu
		int id = item.getItemId();
		switch(id){
		case MENU_ABOUT:
			//If user wants to change bootstrap server call configuration
			about();
			return true;
		}

		return super.onOptionsItemSelected(item);

	}

	public void onSend(View v) {

		String phoneNumber = mPhoneNumber.getText().toString();
		String message = mMessage.getText().toString();
		//Log.d("Debug", "Weird shit " + mQuantity.getText().toString());
		int quantity = Integer.parseInt(mQuantity.getText().toString());


		if(!((phoneNumber.length() > 0) && (message.length() > 0))) {
			Toast.makeText(getBaseContext(), "Please enter both phone number and message!", Toast.LENGTH_SHORT).show();
			return;
		}
		if(quantity < 1) {
			Toast.makeText(getBaseContext(), "Sending 0 messages won't do anything!", Toast.LENGTH_SHORT).show();
			return;
		}

		mSendSMS.startExecution(phoneNumber, message, quantity);
	}

	private void about() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("SMS Flooder v1.0-RC.\nDev: sanchaz\nSite: SMSFlooder.sanchaz.net")
		.setCancelable(false)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				return;
			}
		}).show();
	}

	public void updateSent(String sent) {
		mSent.setText(sent);		
	}
}