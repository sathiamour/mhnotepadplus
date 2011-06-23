package com.android.notepadplus;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PlayAlarmService extends Service {

	 private Vibrator PhoneVibrator;
	 private MediaPlayer RingPlayer;
	 private TelephonyManager InComingTelManager;
	 //private int InitialCallState;
	 //private long StartTime;
	 private int NoteRowId;
	 private String RingMusic;
	 private int NotifyMethodIdx;

	 /** Play alarm up to 20 seconds before silencing */
	 private static final int ALARM_TIMEOUT_SECONDS = 10;
     /** Vibrate pattern for note alarm */
	 private static final long[] VibratePattern = new long[] { 500, 500 };
	 /** Volume suggested by media team for in-call alarms */
	 private static final float IN_CALL_VOLUME = 0.125f;
	 /** Internal messages */
	 private static final int KILLER = 1000;
	 private Handler mHandler = new Handler() {
	        public void handleMessage(Message msg) {
	            switch (msg.what) {
	                case KILLER:
	                    //if (Log.LOGV) {
	                    //    Log.v("*********** Alarm killer triggered ***********");
	                    //}
	                    SendKillBroadcast(msg.arg1);
	                    stopSelf();
	                    break;
	            }
	        }
	 };
	    
	 private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
	        @Override
	        public void onCallStateChanged(int state, String ignored) {
	            // The user might already be in a call when the alarm fires. When
	            // we register onCallStateChanged, we get the initial in-call state
	            // which kills the alarm. Check against the initial call state so
	            // we don't kill the alarm during a call.
	            if (state != TelephonyManager.CALL_STATE_IDLE) {
	                //sendKillBroadcast(mCurrentAlarm);
	                stopSelf();
	            }
	        }
	 };
	    
	 @Override
	 public void onCreate() {
		    PhoneVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);  
	        // Listen for incoming calls to kill the alarm.
	        InComingTelManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
	        InComingTelManager.listen(
	                mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
	        // Acquire wake lock
	        NotifyAlarmWakeLock.acquireCpuWakeLock(this);
	        
	        NoteRowId = ProjectConst.NegativeOne;
	        RingMusic = ProjectConst.EmptyStr;
	   	    NotifyMethodIdx = ProjectConst.NegativeOne;
	 }

	 @Override
	 public void onDestroy() {
	        Stop();
	        // Stop listening for incoming calls.
		    InComingTelManager.listen(mPhoneStateListener, 0);
		    // Release wake lock
		    NotifyAlarmWakeLock.releaseCpuLock();
	 }

	 @Override
	 public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	 }
	 
	 @Override
	 public int onStartCommand(Intent intent, int flags, int startId) {
	        // No intent, tell the system not to restart us.
	        if (intent == null) {
	            stopSelf();
	            return START_NOT_STICKY;
	        }
            // Get parameters
	        Bundle Parameters = intent.getExtras();
	        NoteRowId = Parameters.getInt(OneNote.KEY_ROWID);
	        RingMusic = Parameters.getString(OneNote.KEY_RINGMUSIC);
	        NotifyMethodIdx = Parameters.getInt(OneNote.KEY_NOTIFYMETHOD);
             
	        // Invalidate note row id
	        if( NoteRowId == ProjectConst.NegativeOne )  
	        	return START_STICKY;

	        Play(NoteRowId);
	        // Record the initial call state here so that the new alarm has the
	        // newest state.
	        // InitialCallState = InComingTelManager.getCallState();

	        return START_STICKY;
	 }
	 
	 private void SendKillBroadcast(int RowId) {
	        //long millis = System.currentTimeMillis() - StartTime;
	        //int minutes = (int) Math.round(millis / 60000.0);
	        Intent AlarmKilled = new Intent(Alarms.ALARM_KILL_ACTION);
	        AlarmKilled.putExtra(OneNote.KEY_ROWID, RowId);
	        sendBroadcast(AlarmKilled);
	 }
	 
	 private void Play(int RowId) {
	        // stop() checks to see if we are already playing.
	        Stop();

	        //if (Log.LOGV) {
	        //    Log.v("AlarmKlaxon.play() " + alarm.id + " alert " + alarm.alert);
	        //}
	        
	        // Check wether it is silent or user does not need ring
	        // If not, play ring music
	        if( !RingMusic.equals(OneNote.SilentMusicTitle) &&
	        	OneNote.IsRing(NotifyMethodIdx) ) {
	            Uri AlertMusic = Uri.parse(RingMusic);
	            // Fall back on the default alarm if the database does not have an
	            // alarm stored.
	            if (AlertMusic == null) {
	            	AlertMusic = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
	            	Log.d("log", "PlayAlarmService: Using default alarm: " + AlertMusic.toString());
	                //if (Log.LOGV) {
	                //    Log.v("Using default alarm: " + alert.toString());
	                //}
	            }

	            // TODO: Reuse mMediaPlayer instead of creating a new one and/or use
	            // RingtoneManager.
	            RingPlayer = new MediaPlayer();
	            RingPlayer.setOnErrorListener(new OnErrorListener() {
	                public boolean onError(MediaPlayer mp, int what, int extra) {
	                    Log.e("Err","PlayAlarmService: Error occurred while playing audio");
	                    mp.stop();
	                    mp.release();
	                    RingPlayer = null;
	                    return true;
	                }
	            });

	            try {
	                // Check if we are in a call. If we are, play alarm at a low volume 
	            	// to not disrupt the call.
	                if (InComingTelManager.getCallState() != TelephonyManager.CALL_STATE_IDLE) {
	                    //Log.v("Using the in-call alarm");
	                    RingPlayer.setVolume(IN_CALL_VOLUME, IN_CALL_VOLUME);

	                }
	                Log.d("log", "PlayAlarmService: playing alarm: " + AlertMusic.toString());
                	RingPlayer.setDataSource(this, AlertMusic);
	                StartAlarm(RingPlayer);
	            } catch (Exception ex) {
	                //Log.v("Using the fallback ringtone");
	                // The alert may be on the sd card which could be busy right
	                // now. Use the fallback ringtone.
	                try {
	                    // Must reset the media player to clear the error state.
	                	RingPlayer.reset();
	                	// Use default one
	                	Uri DefaultAlert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
	                	RingPlayer.setDataSource(this, DefaultAlert);
	                    StartAlarm(RingPlayer);
	                } catch (Exception ex2) {
	                    // At this point we just don't play anything.
	                    Log.d("Err", "PlayAlarmService: Failed to play fallback ringtone");
	                }
	            }
	        }

	        /** Start the vibrator after everything is ok with the media player */
	        if( OneNote.IsVibrate(NotifyMethodIdx) )
	        	PhoneVibrator.vibrate(VibratePattern, 0);
	        else
	        	PhoneVibrator.cancel();
	        
	        // After 10 seconds kill the player
	        EnableKiller(RowId,1000 * ALARM_TIMEOUT_SECONDS);
	    }

	    // Do the common stuff when starting the alarm.
	    private void StartAlarm(MediaPlayer Player)
	            throws java.io.IOException, IllegalArgumentException,
	                   IllegalStateException {
	        final AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
	        // do not play alarms if stream volume is 0
	        // (typically because ringer mode is silent).
	        if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
	        	Player.setAudioStreamType(AudioManager.STREAM_ALARM);
	        	Player.setLooping(true);
	        	Player.prepare();
	        	Player.start();
	        }
	    }
	    
	    /***
	     * Stops alarm audio and disables alarm if it not snoozed and not
	     * repeating
	     */
	    public void Stop() {
	        //if (Log.LOGV) Log.v("AlarmKlaxon.stop()");
	        //if (mPlaying) {
	        //    mPlaying = false;

	            // Stop audio playing
	            if (RingPlayer != null) {
	            	RingPlayer.stop();
	            	RingPlayer.release();
	            	RingPlayer = null;
	            }

	            // Stop vibrator
	            PhoneVibrator.cancel();
	        //}
	        DisableKiller();
	    }

	    /***
	     * Kills alarm audio after ALARM_TIMEOUT_SECONDS, so the alarm
	     * won't run all day.
	     *
	     * This just cancels the audio, but leaves the notification
	     * popped, so the user will know that the alarm tripped.
	     */
	    private void EnableKiller(int RowId, long Millis) {
	        mHandler.sendMessageDelayed(mHandler.obtainMessage(KILLER, RowId, 0), Millis);
	    }

	    private void DisableKiller() {
	        mHandler.removeMessages(KILLER);
	    }
}
