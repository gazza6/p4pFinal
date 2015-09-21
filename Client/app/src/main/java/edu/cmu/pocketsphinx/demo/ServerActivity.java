package edu.cmu.pocketsphinx.demo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ServerActivity extends Activity {
    private Button record;
    private TextView text;
    private TextView recognizing;
    boolean recording = false;

    RecordAudio recordTask;

    File recordingFile;

    //recording format
    int frequency = 16000, channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        text = (TextView) this.findViewById(R.id.textview_speech);
        recognizing = (TextView) this.findViewById(R.id.textview_recognizing);
        record = (Button) this.findViewById(R.id.button_record);
        record.setBackgroundColor(Color.GRAY);
        record.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {    //the Record button is pressed
                        record.setBackgroundColor(Color.LTGRAY);
                        record.setText("Release to end");
                        text.setText(" ");
                        Thread recordThread = new Thread(new Runnable() {

                            @Override
                            public void run() {
                                recording = true;
                                startRecord();
                            }

                        });
                        recordThread.start();
                        return true;
                    }
                    case MotionEvent.ACTION_UP: {   //the Record button is released
                        record.setBackgroundColor(Color.GRAY);
                        record.setText("Hold to record");
                        recording = false;
                        if (isNetworkConnected() == true) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    recognizing.setText("Recognizing");
                                }
                            });
                            final ServerAction serverTask = new ServerAction();
                            serverTask.execute();
                            //timeout
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (serverTask.getStatus() == AsyncTask.Status.RUNNING) {
                                        serverTask.cancel(true);
                                        recognizing.setText(" ");
                                        Context context = getApplicationContext();
                                        CharSequence text = "Recognition failed! Please try again later.";
                                        int duration = Toast.LENGTH_SHORT;
                                        Toast toast = Toast.makeText(context, text, duration);
                                        toast.show();
                                    }
                                }
                            }, 30000); // 30s timeout for online recognition task
                        } else {    //network not available
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Context context = getApplicationContext();
                                    CharSequence text = "Network not available!";
                                    int duration = Toast.LENGTH_SHORT;

                                    Toast toast = Toast.makeText(context, text, duration);
                                    toast.show();
                                }
                            });
                        }
                        return false;
                    }
                    default:
                        return false;
                }
            }
        });

        //save recordings on the phone
        File path = new File(
                Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/Sphinx/");
        path.mkdirs();
        try {
            recordingFile = File.createTempFile("recording", ".pcm", path);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't create file on SD card", e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_server, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startRecord() {
        recordTask = new RecordAudio();
        recordTask.execute();

    }

    //record user's speech from the microphone
    private class RecordAudio extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            recording = true;
            try {
                DataOutputStream dos = new DataOutputStream(
                        new BufferedOutputStream(new FileOutputStream(
                                recordingFile)));
                int bufferSize = AudioRecord.getMinBufferSize(frequency,
                        channelConfiguration, audioEncoding);
                AudioRecord audioRecord = new AudioRecord(
                        MediaRecorder.AudioSource.MIC, frequency,
                        channelConfiguration, audioEncoding, bufferSize);

                byte[] buffer = new byte[bufferSize];
                audioRecord.startRecording();
                int r = 0;
                while (recording) {
                    int bufferReadResult = audioRecord.read(buffer, 0,
                            bufferSize);
                    for (int i = 0; i < bufferReadResult; i++) {
                        dos.writeByte(buffer[i]);
                    }
                    publishProgress(new Integer(r));
                    r++;
                }
                audioRecord.stop();
                dos.close();
            } catch (Throwable t) {
                Log.e("AudioRecord", "Recording Failed");
            }
            return null;
        }
    }

    //
    private class ServerAction extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... params) {

            while (!isCancelled()) {
                byte[] audiodata = new byte[(int) (recordingFile.length())];
                try {
                    DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(recordingFile)));
                    while (dis.available() > 0) {
                        int i = 0;
                        while (dis.available() > 0 && i < audiodata.length) {
                            audiodata[i] = dis.readByte();
                            i++;
                        }
                    }
                    dis.close();

                    DataInputStream fromServer;

                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);

                    if (MainActivity.testMode == true) {
                        Socket socket = new Socket("172.23.58.138", 7000);   //need to be updated!!!
                        DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());

                        dOut.writeInt(audiodata.length); // write length of the message
                        dOut.write(audiodata);           // write the message
                        fromServer = new DataInputStream(socket.getInputStream());

                    } else {
                        //send the pcm file to the server
                        Socket socket = new Socket("65.52.39.87", 7000);    //azure server IP address
                        DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());

                        dOut.writeInt(audiodata.length); // write length of the message
                        dOut.write(audiodata);           // write the message
                        fromServer = new DataInputStream(socket.getInputStream());
                    }
                    //result text sent from server
                    final String s = fromServer.readUTF();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            text.setText(s);
                            recognizing.setText(" ");
                        }
                    });

                } catch (Throwable t) {
                    Log.e("AudioTrack", "Playback Failed");
                    t.printStackTrace();
                }



                return null;
            }
            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            runOnUiThread(new Runnable() {
                public void run() {
                    text.setText(" ");
                }
            });
        }
    }
    //Check network
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            // There are no active networks.
            return false;
        } else
            return true;
    }
}
