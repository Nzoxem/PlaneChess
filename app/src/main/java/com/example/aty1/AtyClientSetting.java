package com.example.aty1;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.network.broadcast.DataBroaCastSerlied;
import com.example.network.broadcast.HelperIPAdress;
import com.example.network.model.MsgNet;
import com.example.network.model.client.Client;
import com.example.utils.UtilDeserializable;
import com.example.yifeihappy.planechess.R;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author XQF
 * @created 2017/6/6
 */

public class AtyClientSetting extends AppCompatActivity {

    public static final String TAG = "atyclientsetting";


    public static final String CREATE_ROOM = "create_room";//CREATEROOM
    public static final String CONNECT = "connect_to_sever";//CONNECT TO SERVER
    public static final String ENTER_ROOM = "enter_room";//ENTERROOM
    public static final String WELCOME = "welcome";//WELCOME
    public static final String REFUSE = "refuse";//REFUSE
    public static final String BEGIN = "begin";//BEGIN
    public static final String CBACK = "CBA";//CBA
    public static final String RBACK = "RBA";//RBA

    public static final int CONNECT_WHAT = 0X300;//CONNECT TO SERVER
    public static final int WELCOME_WHAT = 0x200;
    public static final int REFUSE_WHAT = 0x400;
    public static final int BEGIN_WHAT = 0x100;
    public static final String ROOM_DATA = "roomData";
    public static final int WELCOME_OTHERS_WHAT = 0x201;
    public static final int REFUSE_OTHERS_WHAT = 0x401;
    public static final int RBACK_WHAT = 0x500;
    public static final int SOCKET_PORT = 20000;
    String roomIP = null;
    String mPlayerIP = null;
    String playersNum = null;
    Client client = null;
    String mPlaneColor = "NULL";
    String mPlayerName = "NULL";
    int mIndex;
    ClientThread clientThread;
    String planeColor;
    Button btnEnter = null;
    private RadioGroup radioGroupColor = null;
    private RadioButton radioButtonRoomerSelected = null;

    @BindView(R.id.btn_aty_usersetting_back)
    protected Button mButtonBack;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg.what == CONNECT_WHAT) {
                Toast.makeText(AtyClientSetting.this, "你已经进入了房间", Toast.LENGTH_LONG).show();
            }

            if (msg.what == WELCOME_WHAT) {
                Log.d(TAG, "客户端接受到了欢迎消息消息");
                Toast.makeText(AtyClientSetting.this, "Waiting to begin111.", Toast.LENGTH_LONG).show();
                for (int i = 0; i < radioGroupColor.getChildCount(); i++) {
                    RadioButton radioButton = (RadioButton) radioGroupColor.getChildAt(i);
                    radioButton.setEnabled(false);
                }
            }

            if (msg.what == WELCOME_OTHERS_WHAT || msg.what == REFUSE_OTHERS_WHAT) {
                int radioIndex = msg.arg1;
                RadioButton radioButtonSelected = (RadioButton) radioGroupColor.getChildAt(radioIndex);
                radioButtonSelected.setEnabled(false);
                if (radioButtonSelected.isChecked()) radioGroupColor.clearCheck();
            }

            if (msg.what == REFUSE_WHAT) {
                int radioIndex = msg.arg1;
                RadioButton radioButtonSelected = (RadioButton) radioGroupColor.getChildAt(radioIndex);
                radioButtonSelected.setEnabled(false);
                radioGroupColor.clearCheck();
                btnEnter.setEnabled(true);
            }

            if (msg.what == BEGIN_WHAT) {
                Toast.makeText(AtyClientSetting.this, "Receive begin.", Toast.LENGTH_LONG).show();


                //客户端进入游戏房间----------------------------------------------------------------------------------------

                clientThread.stopGetData();
                Log.d(TAG, "进入client之前的mIndex" + mIndex);
                AtyGameClient.startAtyGameClient(AtyClientSetting.this, AtyGameClient.class, mIndex + "", roomIP);
//                finish();

            }

            if (msg.what == RBACK_WHAT) {
                Toast.makeText(AtyClientSetting.this, "Room close", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_setting);
        ButterKnife.bind(this);

        Intent intent = getIntent();


        final Bundle bundle = intent.getExtras();
        final DataBroaCastSerlied roomData = (DataBroaCastSerlied) bundle.getSerializable(ROOM_DATA);


        planeColor = roomData.getPlaneColor();//The color the creater of room has selected.
        roomIP = roomData.getRoomIP();//　This room IP.


        mPlayerIP = HelperIPAdress.getIPByWifi(this);//my IP
        playersNum = roomData.getPlayersNum();

        //set the planeColor which the creater of the room has selected enable = false
        radioGroupColor = (RadioGroup) findViewById(R.id.radiogroupColor);
        radioButtonRoomerSelected = (RadioButton) radioGroupColor.getChildAt(Integer.parseInt(planeColor));
        radioButtonRoomerSelected.setEnabled(false);
        radioGroupColor.clearCheck();


        btnEnter = (Button) findViewById(R.id.btnEnter);
        clientThread = new ClientThread();
        clientThread.start();

        btnEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String playerName;
                String planeColor = null;
                EditText edtName = (EditText) findViewById(R.id.edtName);
                playerName = edtName.getText().toString();
                if (playerName.equals("")) {
                    Toast.makeText(AtyClientSetting.this, "请输入玩家姓名", Toast.LENGTH_SHORT).show();
                    return;
                }

                int radi;
                for (radi = 0; radi < radioGroupColor.getChildCount(); radi++) {
                    RadioButton r = (RadioButton) radioGroupColor.getChildAt(radi);
                    if (r != radioButtonRoomerSelected) {
                        if (r.isChecked()) {
                            planeColor = String.valueOf(radi);
                            mIndex = radi;
                            break;
                        }
                    }

                }
                if (radi == radioGroupColor.getChildCount()) {
                    Toast.makeText(AtyClientSetting.this, "请输选择一种颜色", Toast.LENGTH_SHORT).show();
                    return;
                }


                DataBroaCastSerlied enterRoomData = new DataBroaCastSerlied(ENTER_ROOM, roomIP, playersNum, mPlayerIP, planeColor, playerName);
                MsgNet msg = new MsgNet(enterRoomData.toString(), (byte) 0x00);

                try {
                    client.sendToServer(msg);
                } catch (SocketException e) {
                    e.printStackTrace();
                    Toast.makeText(AtyClientSetting.this, "Send to server failed", Toast.LENGTH_LONG).show();
                }
                btnEnter.setEnabled(false);//wait for check

            }
        });

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //playerNum == mIndex;
        DataBroaCastSerlied enterRoomData = new DataBroaCastSerlied(CBACK, roomIP, String.valueOf(mIndex), mPlayerIP, mPlaneColor, mPlayerName);
        MsgNet msg = new MsgNet(enterRoomData.toString(), (byte) 0x06);

        try {
            client.sendToServer(msg);

        } catch (SocketException e) {
            e.printStackTrace();
            Toast.makeText(AtyClientSetting.this, "Back message Send to server failed", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onDestroy() {
        clientThread.stopGetData();
        client.close();
        super.onDestroy();


    }

    @OnClick(R.id.btn_aty_usersetting_back)
    public void onBtnBackClick() {
        finish();
    }


    /**
     * 在用户设置界面管理消息的线程-------------------------------------------------------------------------------------
     */

    class ClientThread extends Thread {
        public Object myLock = new Object();
        private volatile boolean stopThread = false;

        public void stopGetData() {
            stopThread = true;
//            client.close();
            this.interrupt();

        }

        @Override
        public void run() {
            super.run();
            try {

                client = Client.newInstance(InetAddress.getByName(roomIP), SOCKET_PORT);
            } catch (IOException e) {
                Log.d(TAG, e.getMessage());
                e.printStackTrace();
            }
            try {
                while (!stopThread) {
                    client.getPort();
                    MsgNet msg = client.getData();//If there is not data,this thread will be blocked.
                    Log.d(TAG, "客户端接受到了消息在setting里面" + msg.toString());
                    Message message = handler.obtainMessage();
                    UtilDeserializable deserializable = new UtilDeserializable();
                    DataBroaCastSerlied enterMessage = deserializable.deSerliBroacastData(msg.getData());
                    if (enterMessage.getRoomIP().startsWith(roomIP)) {
                        if (enterMessage.getTag().startsWith(CONNECT)) {
                            Log.d(TAG, "客户端接受到了服务器的连接成功消息" + msg.toString());
//                            mIndex = Integer.parseInt(enterMessage.getPlayersNum());//get index at server
                            message.what = CONNECT_WHAT;
                            handler.sendMessage(message);
                        }

                        if (enterMessage.getTag().startsWith(WELCOME)) {
                            if (enterMessage.getPlayerIP().startsWith(mPlayerIP)) {
                                message.what = WELCOME_WHAT;
                                mPlaneColor = enterMessage.getPlaneColor();//If tag==welcome,the message getPlaneColor==mPlanecolor
                                mPlayerName = enterMessage.getPlayerName();
                            } else {
                                message.arg1 = Integer.parseInt(enterMessage.getPlaneColor());//message.arg1 == index of color has selected.
                                message.what = WELCOME_OTHERS_WHAT;
                            }
                            handler.sendMessage(message);
                        }

                        if (enterMessage.getTag().startsWith(REFUSE)) {
                            if (enterMessage.getPlayerIP().startsWith(mPlayerIP)) {
                                message.what = REFUSE_WHAT;
                            } else {
                                message.arg1 = Integer.parseInt(enterMessage.getPlaneColor());//message.arg1 == index of color has selected.
                                message.what = REFUSE_OTHERS_WHAT;
                            }
                            handler.sendMessage(message);
                        }

                        if (enterMessage.getTag().startsWith(BEGIN)) {

                            Log.d(TAG, "转向开始游戏界面");
                            message.what = BEGIN_WHAT;
                            playersNum = enterMessage.getPlayersNum();
                            handler.sendMessage(message);
                        }

                        if (enterMessage.getTag().startsWith(RBACK)) {
                            message.what = RBACK_WHAT;
                            handler.sendMessage(message);

                        }
                    }

                }
            } catch (InterruptedException e) {
                this.interrupt();
            }


        }
    }
}
