package com.rs.tzfe;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.Leaderboards.LoadPlayerScoreResult;
import com.google.example.games.basegameutils.BaseGameActivity;
import com.rs.tzfe.views.CustomGameView;
import com.rs.tzfe.views.OnStateListener;
import com.rs.tzfe.views.OnTimerListener;
import com.rs.tzfe.views.OnToolsChangeListener;

public class MainActivity extends BaseGameActivity implements OnClickListener,
		OnTimerListener, OnStateListener, OnToolsChangeListener {

	private ImageButton btnPlay;
	private ImageButton btnHelp;
	private ImageButton btnRate;
	private ImageButton btnLeaderboard;
	private ImageButton btnSignin;
	private ImageButton btnSignout;

	private ImageButton btnRefresh;
	private ImageButton btnTip;
	private ImageButton btnMenu;
	private ImageButton btnSound;
	private ImageButton btnPause;

	private ImageView imgTitle;
	private CustomGameView gameView;
	private ProgressBar progress;
	private ImageView clock;
	private TextView textRefreshNum;
	private TextView textTipNum;
	private TextView gameState;
	private TextView continue_to;
	private TextView explaination;
	private TextView timeUsed;
	private ImageView example;

	private RelativeLayout timerLayout;
	private RelativeLayout menuLayout;
	private RelativeLayout pauseLayout;
	private RelativeLayout refreshLayout;
	private RelativeLayout tipLayout;
	private RelativeLayout stateLayout;
	private RelativeLayout soundLayout;

	private MediaPlayer player;
	private boolean hasSound;
	private SharedPreferences sp;
	private SharedPreferences.Editor spEditor;
	private int record;
	private long myBest;
	
	private int currentView;
	private AudioManager soundManager;
	private int musicVolumn;

	private int currentState;

	private Animation scaleOut;
	private Animation transIn;
	private Animation scale;
	private Animation bounce_in;
	private Animation slideUp;

	private AdView adView;
	private String interstitialID;
	private InterstitialAd mInterstitial;
	private AdRequest adRequest;

	// private Handler handler = new Handler() {
	// @Override
	// public void handleMessage(Message msg) {
	// switch (msg.what) {
	// case 0:
	// dialog = new MyDialog(WelActivity.this, gameView,
	// "Congratulations！", gameView.getTotalTime()
	// - progress.getProgress());
	// dialog.show();
	// break;
	// case 1:
	// dialog = new MyDialog(WelActivity.this, gameView, "Game Over！",
	// gameView.getTotalTime() - progress.getProgress());
	// dialog.show();
	// break;
	//
	// case 2:
	// dialog = new MyDialog(WelActivity.this, gameView, "Paused！",
	// gameView.getTotalTime() - progress.getProgress());
	// dialog.show();
	// break;
	// }
	// }
	// };

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set up tracker
		((MyApplication) getApplication())
				.getTracker(MyApplication.TrackerName.APP_TRACKER);

		setContentView(R.layout.welcome);
		// Show ad
		adView = (AdView) findViewById(R.id.ad);
		if (adView != null) {
			AdRequest adRequest = new AdRequest.Builder()
					.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
					.addTestDevice("5E4CA696BEB736E734DD974DD296F11A").build();
			adView.loadAd(adRequest);
		}

		interstitialID = getResources().getString(R.string.interstitialID);
		mInterstitial = new InterstitialAd(this);
		mInterstitial.setAdUnitId(interstitialID);
		adRequest = new AdRequest.Builder()
				.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
				.addTestDevice("5E4CA696BEB736E734DD974DD296F11A").build();
		mInterstitial.loadAd(adRequest);

		btnPlay = (ImageButton) findViewById(R.id.play_btn);
		btnHelp = (ImageButton) findViewById(R.id.help_btn);
		btnRate = (ImageButton) findViewById(R.id.rate_btn);
		btnLeaderboard = (ImageButton) findViewById(R.id.leaderboard_btn);
		btnSignin = (ImageButton) findViewById(R.id.sign_in_button);
		btnSignout = (ImageButton) findViewById(R.id.sign_out_button);

		btnRefresh = (ImageButton) findViewById(R.id.refresh_btn);
		btnTip = (ImageButton) findViewById(R.id.tip_btn);
		btnMenu = (ImageButton) findViewById(R.id.menu_btn);
		btnPause = (ImageButton) findViewById(R.id.pause_btn);
		btnSound = (ImageButton) findViewById(R.id.sound_btn);

		imgTitle = (ImageView) findViewById(R.id.title_img);
		gameView = (CustomGameView) findViewById(R.id.game_view);
		clock = (ImageView) findViewById(R.id.clock);
		progress = (ProgressBar) findViewById(R.id.timer);
		textRefreshNum = (TextView) findViewById(R.id.text_refresh_num);
		textTipNum = (TextView) findViewById(R.id.text_tip_num);
		gameState = (TextView) findViewById(R.id.game_state);
		continue_to = (TextView) findViewById(R.id.continue_to);
		explaination = (TextView) findViewById(R.id.explaination);
		timeUsed = (TextView) findViewById(R.id.time_used);
		example = (ImageView) findViewById(R.id.example);

		timerLayout = (RelativeLayout) findViewById(R.id.timer_layout);
		menuLayout = (RelativeLayout) findViewById(R.id.menu_layout);
		pauseLayout = (RelativeLayout) findViewById(R.id.pause_layout);
		refreshLayout = (RelativeLayout) findViewById(R.id.refresh_layout);
		tipLayout = (RelativeLayout) findViewById(R.id.tip_layout);
		soundLayout = (RelativeLayout) findViewById(R.id.sound_layout);
		stateLayout = (RelativeLayout) findViewById(R.id.state_layout);
		// XXX
		progress.setMax(gameView.getTotalTime());

		btnPlay.setOnClickListener(this);
		btnHelp.setOnClickListener(this);
		btnRate.setOnClickListener(this);

		btnRefresh.setOnClickListener(this);
		btnTip.setOnClickListener(this);
		btnMenu.setOnClickListener(this);
		btnPause.setOnClickListener(this);
		btnSound.setOnClickListener(this);

		menuLayout.setOnClickListener(this);
		pauseLayout.setOnClickListener(this);
		refreshLayout.setOnClickListener(this);
		tipLayout.setOnClickListener(this);
		soundLayout.setOnClickListener(this);
		stateLayout.setOnClickListener(this);

		gameView.setOnTimerListener(this);
		gameView.setOnStateListener(this);
		gameView.setOnToolsChangedListener(this);
		CustomGameView.initSound(this);

		// 获得声音设备和设备音量
		soundManager = (AudioManager) this
				.getSystemService(Context.AUDIO_SERVICE);
		musicVolumn = soundManager.getStreamVolume(AudioManager.STREAM_MUSIC);

		// Animation Effects
		scaleOut = AnimationUtils.loadAnimation(this, R.anim.scale_anim_out);
		transIn = AnimationUtils.loadAnimation(this, R.anim.trans_in);
		scale = AnimationUtils.loadAnimation(this, R.anim.scale_anim);
		bounce_in = AnimationUtils.loadAnimation(this, R.anim.bounce_in);
		slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);

		btnLeaderboard.setOnClickListener(this);
		btnSignin.setOnClickListener(this);
		btnSignout.setOnClickListener(this);

		imgTitle.startAnimation(scale);
		btnPlay.startAnimation(scale);
		btnHelp.startAnimation(scale);
		btnRate.startAnimation(scale);
		btnLeaderboard.startAnimation(scale);
		btnSound.startAnimation(bounce_in);
		if (isSignedIn()) {
			btnSignout.startAnimation(scale);
		} else {
			btnSignin.startAnimation(scale);
		}

		sp = this.getSharedPreferences("settings", 0);
		spEditor = sp.edit();
		hasSound = true;

		player = MediaPlayer.create(this, R.raw.bg);
		player.setLooping(true);// 设置循环播放
		player.start();
		// if (hasSound)
		// player.start();
		// else
		// btnSound.setBackgroundResource(R.drawable.no_sound_fixed47);

		currentView = 0;
		currentState = -1;
		// GameView.soundPlay.play(GameView.ID_SOUND_BACK2BG, -1);
		mHelper.setMaxAutoSignInAttempts(1);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.play_btn:
			playClicked();
			break;

		case R.id.help_btn:
			// Show instruction
			showExample();
			break;

		case R.id.rate_btn:
			openRateIntent();
			break;

		case R.id.refresh_btn:
			refreshClicked();
			break;

		case R.id.refresh_layout:
			refreshClicked();
			break;

		case R.id.tip_btn:
			tipClicked();
			break;

		case R.id.tip_layout:
			tipClicked();
			break;

		case R.id.menu_btn:
			showHomeView();
			break;

		case R.id.menu_layout:
			showHomeView();
			break;

		case R.id.sound_btn:
			toggleSound();
			break;

		case R.id.sound_layout:
			toggleSound();
			break;

		case R.id.pause_btn:
			// Pause, show dialog
			pauseClicked();
			break;

		case R.id.pause_layout:
			// Pause, show dialog
			pauseClicked();
			break;

		case R.id.state_layout:
			doStateLayout();
			break;

		case R.id.sign_in_button:
			beginUserInitiatedSignIn();
			break;

		case R.id.sign_out_button:
			signOut();
			findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
			findViewById(R.id.sign_out_button).setVisibility(View.GONE);
			break;

		case R.id.leaderboard_btn:
			if (isSignedIn()) {
				startActivityForResult(Games.Leaderboards.getLeaderboardIntent(
						getApiClient(),
						getString(R.string.fatest_time_leaderboard)), 2);
			} else {
				// User not signed in yet, let them sign in
				beginUserInitiatedSignIn();
			}
			break;

		}

	}

	private void showExample() {

		gameState.setText(MainActivity.this.getResources().getString(
				R.string.instruction));
		continue_to.setText(MainActivity.this.getResources().getString(
				R.string.back));

		toggleHomeBtns();
		timeUsed.setVisibility(View.GONE);
		stateLayout.setVisibility(View.VISIBLE);
		example.setVisibility(View.VISIBLE);
		explaination.setVisibility(View.VISIBLE);
		stateLayout.startAnimation(bounce_in);
		soundLayout.startAnimation(slideUp);
		soundLayout.setVisibility(View.GONE);
		currentState = CustomGameView.HOME;

	}

	private void openRateIntent() {
		String packageName = "com.rs.tzfe";
		Intent rateIntent = new Intent(Intent.ACTION_VIEW);
		// Try Google play
		rateIntent.setData(Uri.parse("market://details?id=" + packageName));
		if (tryStartActivity(rateIntent) == false) {
			// Market (Google play) app seems not installed, let's try to open a
			// webbrowser
			rateIntent.setData(Uri
					.parse("https://play.google.com/store/apps/details?id="
							+ packageName));
			if (tryStartActivity(rateIntent) == false) {
				// Well if this also fails, we have run out of options,inform
				// the user.
				Toast.makeText(
						this,
						"Could not open Google Play, please install Google Play.",
						Toast.LENGTH_SHORT).show();
			}
		}

	}

	private boolean tryStartActivity(Intent aIntent) {
		try {
			startActivity(aIntent);
			return true;
		} catch (ActivityNotFoundException e) {
			return false;
		}
	}

	public void doStateLayout() {
		record = getRecord();
		if (mInterstitial != null)
			mInterstitial.loadAd(adRequest);
		stateLayout.setVisibility(View.GONE);
		if (currentState == CustomGameView.WIN) {
			progress.setMax(gameView.getTotalTime() - 10);
			gameView.startNextPlay();
			timeUsed.setVisibility(View.GONE);
			
		} else if (currentState == CustomGameView.LOSE) {
			gameView.startPlay();
			timeUsed.setVisibility(View.GONE);
		} else if (currentState == CustomGameView.PAUSE) {
			resumeGame();
		} else if (currentState == CustomGameView.HOME) {
			stateLayout.startAnimation(slideUp);
			stateLayout.setVisibility(View.GONE);
			example.setVisibility(View.GONE);
			explaination.setVisibility(View.GONE);
			toggleHomeBtns();
		}

	}

	private void toggleHomeBtns() {
		if (btnPlay.getVisibility() == View.VISIBLE) {
			btnPlay.startAnimation(scaleOut);
			btnHelp.startAnimation(scaleOut);
			btnRate.startAnimation(scaleOut);
			btnLeaderboard.startAnimation(scaleOut);
			btnSignin.startAnimation(scaleOut);
			btnSignout.startAnimation(scaleOut);
			imgTitle.startAnimation(scaleOut);

			btnPlay.setVisibility(View.GONE);
			btnHelp.setVisibility(View.GONE);
			btnRate.setVisibility(View.GONE);
			btnLeaderboard.setVisibility(View.GONE);
			btnSignin.setVisibility(View.GONE);
			btnSignout.setVisibility(View.GONE);
			imgTitle.setVisibility(View.GONE);

		} else {
			btnPlay.setVisibility(View.VISIBLE);
			btnHelp.setVisibility(View.VISIBLE);
			btnRate.setVisibility(View.VISIBLE);
			btnLeaderboard.setVisibility(View.VISIBLE);
			if (!isSignedIn()) {
				btnSignin.setVisibility(View.VISIBLE);
				btnSignin.startAnimation(scale);
				btnSignout.setVisibility(View.GONE);
			} else {
				btnSignin.setVisibility(View.GONE);
				btnSignout.setVisibility(View.VISIBLE);
				btnSignout.startAnimation(scale);
			}
			soundLayout.setVisibility(View.VISIBLE);
			imgTitle.setVisibility(View.VISIBLE);

			btnPlay.startAnimation(scale);
			btnHelp.startAnimation(scale);
			btnRate.startAnimation(scale);
			btnLeaderboard.startAnimation(scale);

			soundLayout.startAnimation(bounce_in);
			imgTitle.startAnimation(scale);
		}
	}

	public void playClicked() {

		toggleHomeBtns();

		imgTitle.setVisibility(View.GONE);
		gameView.setVisibility(View.VISIBLE);

		btnRefresh.setVisibility(View.VISIBLE);
		btnTip.setVisibility(View.VISIBLE);
		progress.setVisibility(View.VISIBLE);
		clock.setVisibility(View.VISIBLE);
		textRefreshNum.setVisibility(View.VISIBLE);
		textTipNum.setVisibility(View.VISIBLE);

		timerLayout.setVisibility(View.VISIBLE);
		menuLayout.setVisibility(View.VISIBLE);
		pauseLayout.setVisibility(View.VISIBLE);
		refreshLayout.setVisibility(View.VISIBLE);
		tipLayout.setVisibility(View.VISIBLE);

		timerLayout.startAnimation(bounce_in);
		menuLayout.startAnimation(bounce_in);
		pauseLayout.startAnimation(bounce_in);
		refreshLayout.startAnimation(bounce_in);
		tipLayout.startAnimation(bounce_in);

		btnRefresh.startAnimation(bounce_in);
		btnTip.startAnimation(bounce_in);
		gameView.startAnimation(transIn);
		player.pause();
		gameView.startPlay();
		record = getRecord();
		record = getRecord();
		record = getRecord();

		currentView = 1;
	}

	public void refreshClicked() {
		Animation rotate = AnimationUtils.loadAnimation(this, R.anim.rotate);
		btnRefresh.startAnimation(rotate);
		gameView.refreshChange();
	}

	public void tipClicked() {
		Animation shake02 = AnimationUtils.loadAnimation(this, R.anim.shake);
		btnTip.startAnimation(shake02);
		gameView.autoClear();
	}

	// Change Views
	public void showHomeView() {
		// Show Home View

		toggleHomeBtns();

		imgTitle.setVisibility(View.VISIBLE);
		imgTitle.startAnimation(scale);

		timerLayout.startAnimation(slideUp);
		menuLayout.startAnimation(slideUp);
		pauseLayout.startAnimation(slideUp);
		refreshLayout.startAnimation(slideUp);
		tipLayout.startAnimation(slideUp);

		gameView.setVisibility(View.GONE);
		btnRefresh.setVisibility(View.GONE);
		btnTip.setVisibility(View.GONE);
		progress.setVisibility(View.GONE);
		clock.setVisibility(View.GONE);
		textRefreshNum.setVisibility(View.GONE);
		textTipNum.setVisibility(View.GONE);

		timerLayout.setVisibility(View.GONE);
		menuLayout.setVisibility(View.GONE);
		pauseLayout.setVisibility(View.GONE);
		refreshLayout.setVisibility(View.GONE);
		tipLayout.setVisibility(View.GONE);

		gameView.pausePlayer();
		gameView.stopTimer();

		currentView = 0;
		if (hasSound)
			player.start();
	}

	public void toggleSound() {
		// Home View
		if (hasSound) {
			// Disable sound
			hasSound = false;
			// spEditor.putBoolean("sound", false);
			if (currentView == 0)
				player.pause();
			else if (currentView == 1)
				gameView.pausePlayer();
			musicVolumn = soundManager
					.getStreamVolume(AudioManager.STREAM_MUSIC);
			soundManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
			btnSound.setBackgroundResource(R.drawable.no_sound_fixed47);

		} else {
			// Enable sound
			hasSound = true;
			// spEditor.putBoolean("sound", true);
			if (currentView == 0)
				player.start();
			else if (currentView == 1)
				gameView.startPlayer();
			if (musicVolumn == 0) {
				musicVolumn = 3;
			}
			soundManager.setStreamVolume(AudioManager.STREAM_MUSIC,
					musicVolumn, 0);
			btnSound.setBackgroundResource(R.drawable.sound_fixed47);
		}

		// spEditor.commit();
	}

	@Override
	public void onTimer(int leftTime) {
		// Log.i("onTimer", leftTime + "");
		progress.setProgress(leftTime);
	}

	public void resumeGame() {
		// stateLayout.startAnimation(slideUp);
		stateLayout.setVisibility(View.GONE);
		if (currentView == 0) {
			player.start();
		} else if (currentView == 1) {
			gameView.startPlayer();
			gameView.resumeTimer();
		}
	}

	@Override
	public void onRefreshChanged(int count) {
		textRefreshNum.setText("" + gameView.getRefreshNum());
	}

	@Override
	public void onTipChanged(int count) {
		textTipNum.setText("" + gameView.getTipNum());
	}

	public void quit() {
		this.finish();
	}


	@Override
	public void onBackPressed() {

		// if (stateLayout.getVisibility() == View.GONE && numOfBackPressed ==
		// 0) {
		// // show ad
		// if (mInterstitial != null)
		// if (mInterstitial.isLoaded()) {
		// mInterstitial.show();
		// }
		// numOfBackPressed++;
		// return;
		// }

		Dialog dialog = new AlertDialog.Builder(this)
				.setIcon(R.drawable.icon)
				.setTitle(R.string.quit)
				.setMessage(R.string.sure_quit)
				.setPositiveButton(R.string.alert_dialog_ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								quit();
							}
						})
				.setNegativeButton(R.string.alert_dialog_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						}).create();
		dialog.show();
	}

	@Override
	public void onStart() {
		super.onStart();
		// Get an Analytics tracker to report app starts & uncaught exceptions
		// etc.
		GoogleAnalytics.getInstance(this).reportActivityStart(this);

	}

	@Override
	public void onStop() {
		super.onStop();
		GoogleAnalytics.getInstance(this).reportActivityStop(this);
	}

	@Override
	protected void onPause() {
		if (adView != null) {
			adView.pause();
		}
		// if (!isAdShowed) {
		// gameView.setMode(GameView.PAUSE);
		// } else {
		// isAdShowed = false;
		// }
		if (stateLayout.getVisibility() == View.GONE) {
			gameView.setMode(CustomGameView.PAUSE);
		} else {
			// disable sound
			gameView.player.pause();
			gameView.stopTimer();
		}
		super.onPause();

	}

	@Override
	protected void onResume() {
		super.onResume();

		if (adView != null) {
			adView.resume();
		}
		if (currentView == 0) {
			player.start();
		} else {
			if (currentState == CustomGameView.WIN || currentState == CustomGameView.LOSE) {
				// Current state is win or lose and activity is coming back from
				// outside
				// Resume sound
				gameView.startPlayer();
			}
		}

	}

	@Override
	protected void onDestroy() {
		// Destroy ads when the view is destroyed
		if (adView != null) {
			adView.destroy();
		}

		soundManager.setStreamVolume(AudioManager.STREAM_MUSIC, musicVolumn, 0);
		gameView.setMode(CustomGameView.QUIT);
		super.onDestroy();

	}

	private void showInterstitial() {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (mInterstitial != null) {
					if (mInterstitial.isLoaded()) {
						mInterstitial.show();
					}
				}

			}
		});
	}

	
	private int getRecord() {
		if (isSignedIn()) {
			// get score from leaderboard
			if (getApiClient().isConnected()) {
				Games.Leaderboards.loadCurrentPlayerLeaderboardScore(
						getApiClient(),
						getResources().getString(
								R.string.fatest_time_leaderboard),
						LeaderboardVariant.TIME_SPAN_ALL_TIME,
						LeaderboardVariant.COLLECTION_PUBLIC).setResultCallback(new ResultCallback<LoadPlayerScoreResult>() {

			                @Override
			                public void onResult(LoadPlayerScoreResult arg0) {
			                    LeaderboardScore c = arg0.getScore();
			                    if(c != null){
				                    long score = c.getRawScore();
				                    myBest = score/1000;
			                    }else{
			                    	myBest = 9999;
			                    }
			                }
			             });
				
				return (int)myBest;
			}
		} else {
			// get score from local
			myBest = sp.getInt("best", 9999);
			return (int)myBest;
		}
		return 9999;
	}

	public void processScore(){
		int seconds = gameView.getTotalTime()
				- progress.getProgress();
		if (isSignedIn()) {
			
			if (getApiClient().isConnected()) {
//				int betterScore = seconds < record ? seconds : record;
				Games.Leaderboards
						.submitScore(
								getApiClient(),
								getString(R.string.fatest_time_leaderboard),
								seconds * 1000);
			}
		}else{
			// Store the score to local if it is the best
			if(seconds <= record){
				spEditor.putInt("best", seconds);
				spEditor.commit();
			}
		}
	}
	
	@Override
	public void OnStateChanged(int StateMode) {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				timeUsed.setVisibility(View.GONE);

//				record = getRecord();
				int curScore = gameView.getTotalTime() - progress.getProgress();
				int mrecord =  (int) (curScore < myBest ? curScore : myBest);
				// Log.i("debug", "time used: "
				// + (gameView.getTotalTime() - progress.getProgress()));
				// Show time spent in the game
				timeUsed.setText("Time: "
						+ curScore
						+ " seconds\nBest: " + mrecord + " seconds");
			}

		});

		switch (StateMode) {
		case CustomGameView.WIN:
			// handler.sendEmptyMessage(0);
			this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					showInterstitial();
					// stuff that updates ui
					gameState.setText(MainActivity.this.getResources()
							.getString(R.string.win));
					continue_to.setText(MainActivity.this.getResources()
							.getString(R.string.go));
					timeUsed.setVisibility(View.VISIBLE);
					stateLayout.setVisibility(View.VISIBLE);

					processScore();


				}
			});

			break;
		case CustomGameView.LOSE:
			// handler.sendEmptyMessage(1);
			this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					showInterstitial();
					// stuff that updates ui
					gameState.setText(MainActivity.this.getResources()
							.getString(R.string.lose));
					continue_to.setText(MainActivity.this.getResources()
							.getString(R.string.retry));
					timeUsed.setVisibility(View.VISIBLE);
					stateLayout.setVisibility(View.VISIBLE);
				}
			});

			break;
		case CustomGameView.PAUSE:
			if (currentView == 0) {
				player.pause();
			} else if (currentView == 1) {
				gameView.player.pause();
				gameView.stopTimer();
				gameState.setText(MainActivity.this.getResources().getString(
						R.string.pause));
				continue_to.setText(MainActivity.this.getResources().getString(
						R.string.go));
				stateLayout.setVisibility(View.VISIBLE);
				// stateLayout.startAnimation(bounce_in);
			}
			break;
		case CustomGameView.QUIT:
			player.release();
			gameView.player.release();
			gameView.stopTimer();
			break;
		}
		currentState = StateMode;

		// if (currentState != GameView.PAUSE) {
		// if(currentView == 1)
		// showInterstitial();

		// }
	}

	public void pauseClicked() {
		// gameView.setMode(GameView.PAUSE);
		showInterstitial();
		if (currentView == 1) {
			gameView.player.pause();
			gameView.stopTimer();
			gameState.setText(MainActivity.this.getResources().getString(
					R.string.pause));
			continue_to.setText(MainActivity.this.getResources().getString(
					R.string.go));
			stateLayout.setVisibility(View.VISIBLE);
			currentState = CustomGameView.PAUSE;
			// stateLayout.startAnimation(bounce_in);
		}

	}

	@Override
	public void onSignInFailed() {
		if (currentView == 0) {
			btnSignin.setVisibility(View.VISIBLE);
			btnSignout.setVisibility(View.GONE);
		}
	}

	@Override
	public void onSignInSucceeded() {
		if (currentView == 0) {
//			Log.i("debug","signed in");
			btnSignin.setVisibility(View.GONE);
			btnSignout.setVisibility(View.VISIBLE);
		}
	}

}