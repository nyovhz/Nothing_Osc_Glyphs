package com.vhznyo.nothing_osc_glyphs;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.nothing.ketchum.Common;
import com.nothing.ketchum.GlyphException;
import com.nothing.ketchum.GlyphFrame;
import com.nothing.ketchum.GlyphManager;
import com.relivethefuture.osc.data.BasicOscListener;
import com.relivethefuture.osc.data.OscMessage;
import com.relivethefuture.osc.transport.OscServer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    ///////////////// NOTHING //////////////////
    private GlyphManager mGM = null;
    private GlyphManager.Callback mCallback = null;
    private final String TAG = "ALGO ANDA MAL";

    ////////////////////////////////////////////

    ///////////////// UI ///////////////////////
    private TextView Logger;
    private ScrollView ScrollLogger;
    int[] colors;
    Handler handler = new Handler();

    ///////////////////////////////////////////
    OSCMessageHandler messageHandler = new OSCMessageHandler();
    //////////////// SERVER ///////////////////
    public static final int DEFAULT_OSC_PORT = 3333;
    protected OscServer server;
    protected boolean isListening = false;
    protected  int oscPort = DEFAULT_OSC_PORT;
    ///////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        mGM = GlyphManager.getInstance(getApplicationContext());
        mGM.init(mCallback);
        setContentView(R.layout.activity_main);
        Logger = findViewById(R.id.LogText);
        ScrollLogger = findViewById(R.id.LogView);
    }

    @Override
    public void onDestroy() {
        try {
            mGM.closeSession();
        } catch (GlyphException e) {
            Log.e(TAG, e.getMessage());
        }
        mGM.unInit();
        try {
            stopListening();
            clearGlyphs();
        } catch (GlyphException e) {
            throw new RuntimeException(e);
        }
        super.onDestroy();
    }

    private void init() {
        mCallback = new GlyphManager.Callback() {
            @Override
            public void onServiceConnected(ComponentName componentName) {
                if (Common.is20111()) mGM.register(Common.DEVICE_20111);
                if (Common.is22111()) mGM.register(Common.DEVICE_22111);
                try {
                    mGM.openSession();
                } catch (GlyphException e) {
                    Log.e(TAG, e.getMessage());
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                try {
                    mGM.closeSession();
                } catch (GlyphException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    public void clearConsole(View view) throws GlyphException {Logger.setText("");}

    public void clearGlyphs() throws GlyphException {
        GlyphFrame.Builder builder = mGM.getGlyphFrameBuilder();
        GlyphFrame frameX = builder.build();
        colors = frameX.getChannel();
        mGM.setFrameColors(turnOFF(colors));
    }

    public void AnimateFrames(int frame, int cycle, float period, int interval) throws GlyphException {
        int periodX = (int) (period * 1000);
        GlyphFrame.Builder builder = mGM.getGlyphFrameBuilder();
        GlyphFrame frameX = builder.buildChannel(frame,4000).buildCycles(cycle).buildPeriod(periodX).buildInterval(1).build();
        mGM.animate(frameX);
    }

    public void AnimateFramesSP(int frame, float period) throws GlyphException {
        int periodX = (int) (period * 1000);
        GlyphFrame.Builder builder = mGM.getGlyphFrameBuilder();
        GlyphFrame frameX = builder.buildChannel(frame,4000).buildCycles(1).buildPeriod(periodX).buildInterval(1).build();
        mGM.animate(frameX);
    }

    public int[] turnOFF(int[] colors){
        Arrays.fill(colors, 0);
        return colors;
    }
    public void DeployServer(View view) throws GlyphException {startListening();}

    public void CloseServer(View view) throws GlyphException {
        stopListening();
    }


        //////////////////////////////////////////////////////////////////////////////

    protected class LooperListener extends BasicOscListener {
        @Override
        public void handleMessage(OscMessage msg){
            String messageAddress = msg.getAddress();
            ArrayList<Object> args = msg.getArguments();
            //getOSCMessage(messageAddress, args);
            messageHandler.processOSCMessage(args);
        }

        public void getOSCMessage(String messageAddress, ArrayList<Object> args){
            try {
                AnimateFrames((Integer) args.get(1),1, (Float) args.get(5),1);
            } catch (GlyphException e) {
                throw new RuntimeException(e);
            }
            //Logger.append(messageAddress + " -> " + args.toString() + "\n");
            //ScrollLogger.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }

    public void startListening() throws GlyphException {
        stopListening();
        try {
            server = new OscServer(oscPort);
            server.setUDP(true);
            server.start();
        } catch (Exception e) {
            Toast
                    .makeText(this, "Failed to start OSC server: " + e.getMessage(), Toast.LENGTH_LONG)
                    .show();
            return;
        }
        server.addOscListener(new MainActivity.LooperListener());
        printOSCData("\nListening on port: " + oscPort + "\n");
        Logger.append("\n");
    }

    public void stopListening() throws GlyphException {
        if (server != null) {
            server.stop();
            server = null;
            messageHandler.threadPool.shutdown();
            printOSCData("Stopped listening");
            ScrollLogger.fullScroll(ScrollView.FOCUS_DOWN);
            clearGlyphs();
        }
    }
    private void printOSCData(String msg){
        Logger.append(msg);;
    }

    public class OSCMessageHandler {
        private ExecutorService threadPool = Executors.newFixedThreadPool(3);
        private void printOSCMessage(ArrayList<Object> args) throws GlyphException {

            long threadID = Thread.currentThread().getId();
            AnimateFrames((Integer) args.get(1),1, (Float) args.get(5),1);
            System.out.println("Thread ID: " + threadID + " - " + (String) args.get(0));
        }
        public void processOSCMessage(ArrayList<Object> args) {
            String flag = (String) args.get(0);
            if (flag.startsWith("/xNoise")) {
                threadPool.execute(() -> {
                    try {
                        printOSCMessage(args);
                    } catch (GlyphException e) {
                        throw new RuntimeException(e);
                    }
                });
            } else if (flag.startsWith("/xFoley")) {
                threadPool.execute(() -> {
                    try {
                        printOSCMessage(args);
                    } catch (GlyphException e) {
                        throw new RuntimeException(e);
                    }
                });
            } else {
                threadPool.execute(() -> {
                    try {
                        printOSCMessage(args);
                    } catch (GlyphException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
    }

}
