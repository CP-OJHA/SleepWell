package com.example.lenovo.sleepwell;
//package com.example.lenovo.sleepwell;


import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

//import com.example.lenovo.sleepwell.R;

public class MainActivity extends Activity {
	public static final int DETECT_NONE = 0;
	public static final int DETECT_SNORE = 1;
	public static int selectedDetection = DETECT_NONE;

	private com.example.lenovo.sleepwell.DetectorThread detectorThread;
	private com.example.lenovo.sleepwell.RecorderThread recorderThread;
	private com.example.lenovo.sleepwell.DrawThread drawThread;

	public static int snoreValue = 0;

	private View mainView;
	private Button mSleepRecordBtn, mAlarmBtn, mRecordBtn, mTestBtn;
	private TextView txtAbs;

	private Toast mToast;

	private Handler rhandler = new Handler();
	private Handler showhandler = null;
	private Handler alarmhandler = null;

	private Intent intent;
	private PendingIntent pendingIntent;
	private AlarmManager am;

	private SurfaceView sfv;
	private Paint mPaint;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setTitle("UIC SleepTracker Demo");

		mSleepRecordBtn = (Button) this.findViewById(R.id.btnSleepRecord);
		mAlarmBtn = (Button) findViewById(R.id.btnSelectAlarm);
		mRecordBtn = (Button) findViewById(R.id.btnRecordAlarm);
		mTestBtn = (Button) findViewById(R.id.btnAlarmTest);
		txtAbs = (TextView) findViewById(R.id.txtaverageAbsValue);
		sfv = (SurfaceView) this.findViewById(R.id.SurfaceView);

		intent = new Intent(MainActivity.this, com.example.lenovo.sleepwell.AlarmReceiverActivity.class);
		pendingIntent = PendingIntent.getActivity(MainActivity.this, 2, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		am = (AlarmManager) getSystemService(ALARM_SERVICE);

		mPaint = new Paint();
		mPaint.setColor(Color.GREEN);

		/**
		 * show variable handler
		 */
		showhandler = new Handler() {
			public void handleMessage(Message msg) {
				txtAbs.setText(msg.obj.toString());
			}
		};

		/**
		 * Output alarm handler
		 */
		alarmhandler = new Handler() {
			public void handleMessage(Message msg) {
				int interval = 1;
				int i = msg.arg1;
				setLevel(i);
				com.example.lenovo.sleepwell.AlarmStaticVariables.level = com.example.lenovo.sleepwell.AlarmStaticVariables.level1;
				am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
						+ (interval * 1000), pendingIntent);
			}
		};

		/**
		 * Sleep Record Button
		 */
		mSleepRecordBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				selectedDetection = DETECT_SNORE;
				// alarmThread = new AlarmThread(pendingIntent, am);
				recorderThread = new com.example.lenovo.sleepwell.RecorderThread(showhandler);
				recorderThread.start();
				detectorThread = new com.example.lenovo.sleepwell.DetectorThread(recorderThread,
						alarmhandler);
				detectorThread.start();
				drawThread = new com.example.lenovo.sleepwell.DrawThread(sfv.getHeight() / 2, sfv, mPaint);
				drawThread.start();
				// clsOscilloscope.baseLine = sfv.getHeight() / 2;
				// clsOscilloscope.Start(audioRecord, recBufSize, sfv, mPaint);

				mToast = Toast.makeText(getApplicationContext(),
						"Recording & Detecting start", Toast.LENGTH_LONG);
				mToast.show();
				// goListeningView();
			}
		});

		/**
		 * Select alarm Button
		 */
		mAlarmBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				Intent intent = new Intent(MainActivity.this,
						com.example.lenovo.sleepwell.AlarmSelectActivity.class);
				startActivity(intent);
			}
		});

		/**
		 * Record name Button
		 */
		mRecordBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				rhandler.removeCallbacks(recordActivity);
				rhandler.postDelayed(recordActivity, 1000);
			}
		});

		/**
		 * Test
		 */
		mTestBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				int level = 1;
				setLevel(level);
				startOneShoot();
			}
		});

	}

	private Runnable recordActivity = new Runnable() {
		public void run() {
			Intent intent = new Intent(MainActivity.this,
					com.example.lenovo.sleepwell.AlarmRecordActivity.class);
			startActivity(intent);
		}
	};

	public void startOneShoot() {
		int i = 5;
		am.set(AlarmManager.RTC_WAKEUP,
				System.currentTimeMillis() + (i * 1000), pendingIntent);
	}

	public void setLevel(int l) {
		switch (l) {
		case 0:
			com.example.lenovo.sleepwell.AlarmStaticVariables.level = com.example.lenovo.sleepwell.AlarmStaticVariables.level0;
			break;
		case 1:
			com.example.lenovo.sleepwell.AlarmStaticVariables.level = com.example.lenovo.sleepwell.AlarmStaticVariables.level1;
			break;
		case 2:
			com.example.lenovo.sleepwell.AlarmStaticVariables.level = com.example.lenovo.sleepwell.AlarmStaticVariables.level2;
			break;
		case 3:
			com.example.lenovo.sleepwell.AlarmStaticVariables.level = com.example.lenovo.sleepwell.AlarmStaticVariables.level3;
			break;
		default:
			com.example.lenovo.sleepwell.AlarmStaticVariables.level = com.example.lenovo.sleepwell.AlarmStaticVariables.level1;
			break;
		}
	}

	private void goHomeView() {
		setContentView(mainView);
		if (recorderThread != null) {
			recorderThread.stopRecording();
			recorderThread = null;
		}
		if (detectorThread != null) {
			detectorThread.stopDetection();
			detectorThread = null;
		}
		selectedDetection = DETECT_NONE;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, "Quit demo");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			am.cancel(pendingIntent);
			finish();
			break;
		default:
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			goHomeView();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	protected void onDestroy() {
		super.onDestroy();
		android.os.Process.killProcess(android.os.Process.myPid());
	}
}
