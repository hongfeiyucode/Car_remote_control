package com.hongfeiyu.car_remote_control;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.crossfadedrawerlayout.view.CrossfadeDrawerLayout;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.MiniDrawer;
import com.mikepenz.materialdrawer.interfaces.ICrossfader;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;
import com.mikepenz.materialdrawer.util.DrawerUIUtils;
import com.mikepenz.materialize.util.UIUtils;

/**
 * Created by 红绯鱼 on 2016/4/12 0012.
 */
public class MainActivity extends AppCompatActivity {
    //save our header or result
    private AccountHeader headerResult = null;
    private Drawer result = null;
    private MiniDrawer miniResult = null;
    private CrossfadeDrawerLayout crossfadeDrawerLayout = null;

    //bluetooth
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    public static final byte[] out = {'t'};
    public static char direction = 'w';

    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    //  private TextView mTitle;
    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;

    private Button up_button;
    private Button left_button;
    private Button down_button;
    private Button right_button;
    private Button stop_button;
    private Button fire_button;
    private Button random_button;

    private String mConnectedDeviceName = null;
    private ArrayAdapter<String> mConversationArrayAdapter;
    private StringBuffer mOutStringBuffer;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothChatService mChatService = null;


    //gravity
    // 感应器管理器
    private SensorManager sensorMgr;
    // 得到加速感应器
    Sensor sensor;
    // 定义各坐标轴上的重力加速度
    private float x, y, z;
    boolean graisopen = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_dark_toolbar);

        //Remove line to test RTL support
        //getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        // Handle Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Bluetooth
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null)
        {
            Toast.makeText(this, "��ǰ�ֻ���֧������.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        //gravity
        // 得到当前手机传感器管理对象
        sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);

        // 加速重力感应对象
        sensor = sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // 实例化一个监听器
        SensorEventListener lsn = new SensorEventListener() {
            // 实现接口的方法

            public void onSensorChanged(SensorEvent e) {
                // 得到各轴上的重力加速度
                x = e.values[SensorManager.DATA_X];
                y = e.values[SensorManager.DATA_Y];
                z = e.values[SensorManager.DATA_Z];
                if(graisopen&&mChatService!= null){
                    // 在标题处显示出来
                    setTitle("重力感应模式 X:" + (int)x + "," + "Y:" + (int)y + ","+ "Z:" + (int)z);
                    //Toast.makeText(MainActivity.this, "X轴上的重力加速度为:" + x + "," + "Y轴上的重力加速度为:" + y + "," + "Z轴上的重力加速度为:" + z, Toast.LENGTH_SHORT).show();

                    if(z>5){
                        //前
                        out[0] = 'w';
                        direction = 'w';
                        sendCommand(out);
                    }else if(z<-5){
                        //后
                        out[0] = 's';
                        direction = 's';
                        sendCommand(out);
                    }else {
                        //停
                        sendCommand(out);
                        out[0] = 't';
                        sendCommand(out);
                    }

                    if(x>6){
                        //左
                        out[0] = 'a';
                        sendCommand(out);
                    }else if(x<-6){
                        //右
                        out[0] = 'd';
                        sendCommand(out);
                    }
                }

            }

            public void onAccuracyChanged(Sensor s, int accuracy) {
            }
        };
        // 注册listener，第三个参数是检测的精确度
        sensorMgr.registerListener(lsn, sensor, SensorManager.SENSOR_DELAY_GAME);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                remote_ctrl();
                graisopen = !graisopen;
                setTitle("遥控触碰模式");
            }
        });


        // Create a few sample profile
        // NOTE you have to define the loader logic too. See the CustomApplication for more details
        final IProfile profile = new ProfileDrawerItem().withName("小车遥控器").withEmail("一言不合,开始飙车!").withIcon(R.mipmap.ic_launcher);
        final IProfile profile2 = new ProfileDrawerItem().withName("红绯鱼").withEmail("联系作者").withIcon(R.drawable.conan);

        // Create the AccountHeader
        headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(true)
                .withHeaderBackground(R.drawable.header)
                .addProfiles(
                        profile, profile2
                )
                .withSavedInstance(savedInstanceState)
                .build();


        //Create the drawer
        result = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withDrawerLayout(R.layout.crossfade_material_drawer)
                .withHasStableIds(true)
                .withDrawerWidthDp(72)
                .withGenerateMiniDrawer(true)
                .withAccountHeader(headerResult) //set the AccountHeader we created earlier for the header
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.drawer_item_first).withDescription("右下按钮开启重力感应").withIcon(MaterialDesignIconic.Icon.gmi_car).withIdentifier(1),
                        new PrimaryDrawerItem().withName(R.string.drawer_item_second).withIcon(MaterialDesignIconic.Icon.gmi_directions_run).withIdentifier(2),
                        new PrimaryDrawerItem().withName(R.string.drawer_item_third).withIcon(MaterialDesignIconic.Icon.gmi_truck).withIdentifier(3),
                        new PrimaryDrawerItem().withName(R.string.drawer_item_fourth).withIcon(MaterialDesignIconic.Icon.gmi_airplane).withIdentifier(4),
                        new PrimaryDrawerItem().withName(R.string.drawer_item_fifth).withIcon(MaterialDesignIconic.Icon.gmi_search).withIdentifier(5),
                        new PrimaryDrawerItem().withName(R.string.drawer_item_sixth).withIcon(FontAwesome.Icon.faw_eye).withIdentifier(6),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_seventh).withIcon(FontAwesome.Icon.faw_user_secret).withIdentifier(7).withSelectable(false)
                ) // add the items we want to use with our Drawer
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

                        up_button = (Button) findViewById(R.id.button_up);
                        up_button.setEnabled(false);
                        down_button = (Button) findViewById(R.id.button_down);
                        down_button.setEnabled(false);
                        right_button = (Button) findViewById(R.id.button_right);
                        right_button.setEnabled(false);
                        left_button = (Button) findViewById(R.id.button_left);
                        left_button.setEnabled(false);
                        stop_button = (Button) findViewById(R.id.button_stop);
                        stop_button.setEnabled(false);
                        fire_button = (Button) findViewById(R.id.button_fire);
                        fire_button.setEnabled(false);

                        if (drawerItem.getIdentifier() == 1) {
                            Toast.makeText(MainActivity.this, ((Nameable) drawerItem).getName().getText(MainActivity.this), Toast.LENGTH_SHORT).show();
                            remote_ctrl();
                            return true;
                        }else
                        if (drawerItem.getIdentifier() == 2) {
                            Toast.makeText(MainActivity.this, ((Nameable) drawerItem).getName().getText(MainActivity.this), Toast.LENGTH_SHORT).show();
                            auto_ctrl_1m();
                            return true;
                        }else
                        if (drawerItem.getIdentifier() == 3) {
                            Toast.makeText(MainActivity.this, ((Nameable) drawerItem).getName().getText(MainActivity.this), Toast.LENGTH_SHORT).show();
                            auto_ctrl_3m();
                            return true;
                        }else
                        if (drawerItem.getIdentifier() == 4) {
                            Toast.makeText(MainActivity.this, ((Nameable) drawerItem).getName().getText(MainActivity.this), Toast.LENGTH_SHORT).show();
                            auto_avoid_obstacle();
                            return true;
                        }else
                        if (drawerItem.getIdentifier() == 5) {
                            Toast.makeText(MainActivity.this, ((Nameable) drawerItem).getName().getText(MainActivity.this), Toast.LENGTH_SHORT).show();
                            Intent serverIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                            return true;
                        }else
                        if (drawerItem.getIdentifier() == 6) {
                            Toast.makeText(MainActivity.this, ((Nameable) drawerItem).getName().getText(MainActivity.this), Toast.LENGTH_SHORT).show();
                            ensureDiscoverable();
                            return true;
                        }else
                        if (drawerItem.getIdentifier() == 7) {
//                            new LibsBuilder()
//                                    .withFields(R.string.class.getFields())
//                                    .withActivityStyle(Libs.ActivityStyle.DARK)
//                                    .start(MainActivity.this);
                        } else {
                            if (drawerItem instanceof Nameable) {
                                Toast.makeText(MainActivity.this, ((Nameable) drawerItem).getName().getText(MainActivity.this), Toast.LENGTH_SHORT).show();
                            }
                        }

                        return false;
                    }
                })
                .withSavedInstance(savedInstanceState)
                .withShowDrawerOnFirstLaunch(true)
                .build();

        //get out our drawerLyout
        crossfadeDrawerLayout = (CrossfadeDrawerLayout) result.getDrawerLayout();

        //define maxDrawerWidth
        crossfadeDrawerLayout.setMaxWidthPx(DrawerUIUtils.getOptimalDrawerWidth(this));
        //add second view (which is the miniDrawer)
        MiniDrawer miniResult = result.getMiniDrawer();
        //build the view for the MiniDrawer
        View view = miniResult.build(this);
        //set the background of the MiniDrawer as this would be transparent
        view.setBackgroundColor(UIUtils.getThemeColorFromAttrOrRes(this, com.mikepenz.materialdrawer.R.attr.material_drawer_background, com.mikepenz.materialdrawer.R.color.material_drawer_background));
        //we do not have the MiniDrawer view during CrossfadeDrawerLayout creation so we will add it here
        crossfadeDrawerLayout.getSmallView().addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        //define the crossfader to be used with the miniDrawer. This is required to be able to automatically toggle open / close
        miniResult.withCrossFader(new ICrossfader() {
            @Override
            public void crossfade() {
                boolean isFaded = isCrossfaded();
                crossfadeDrawerLayout.crossfade(400);

                //only close the drawer if we were already faded and want to close it now
                if (isFaded) {
                    result.getDrawerLayout().closeDrawer(GravityCompat.START);
                }
            }

            @Override
            public boolean isCrossfaded() {
                return crossfadeDrawerLayout.isCrossfaded();
            }
        });

        //hook to the crossfade event
        crossfadeDrawerLayout.withCrossfadeListener(new CrossfadeDrawerLayout.CrossfadeListener() {
            @Override
            public void onCrossfade(View containerView, float currentSlidePercentage, int slideOffset) {
                //Log.e("CrossfadeDrawerLayout", "crossfade: " + currentSlidePercentage + " - " + slideOffset);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //add the values which need to be saved from the drawer to the bundle
        outState = result.saveInstanceState(outState);
        //add the values which need to be saved from the accountHeader to the bundle
        outState = headerResult.saveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        //handle the back press :D close the drawer first and if the drawer is closed close the activity
        if (result != null && result.isDrawerOpen()) {
            result.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }


    //bluetooth

    @Override
    public void onStart()
    {
        super.onStart();

        if (!mBluetoothAdapter.isEnabled())
        {
            //Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        else
        {
            if (mChatService == null)
                setupChat();
        }
    }

    public void onClick_Enable_Bluetooth(View view)
    {

        if (!mBluetoothAdapter.isEnabled())
        {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
    }

    public void onClick_Disable_Bluetooth(View view)
    {

        mBluetoothAdapter.disable();
    }


    @Override
    public synchronized void onResume()
    {
        super.onResume();

        if (mChatService != null)
        {
            if (mChatService.getState() == BluetoothChatService.STATE_NONE)
            {
                mChatService.start();
            }
        }
    }

    private void setupChat()
    {


        mConversationArrayAdapter = new ArrayAdapter<String>(this,R.layout.message);
        mConversationView = (ListView) findViewById(R.id.in);
        mConversationView.setAdapter(mConversationArrayAdapter);

        mOutEditText = (EditText) findViewById(R.id.edit_text_out);//�ı��༭��
        mOutEditText.setOnEditorActionListener(mWriteListener);

        mSendButton = (Button) findViewById(R.id.button_send);//���Ͱ�ť
        mSendButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                TextView view = (TextView) findViewById(R.id.edit_text_out);
                String message = view.getText().toString();
                sendMessage(message);
            }
        });

        up_button = (Button) findViewById(R.id.button_up);//���Ͱ�ť
        up_button.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                out[0] = 'w';
                direction = 'w';
                sendCommand(out);
            }
        });

        down_button = (Button) findViewById(R.id.button_down);//���Ͱ�ť
        down_button.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                out[0] = 's';
                direction = 's';
                sendCommand(out);
            }
        });

        left_button = (Button) findViewById(R.id.button_left);//���Ͱ�ť
        left_button.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        out[0] = 'a';
                        sendCommand(out);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (direction == 's')
                            out[0] = 's';
                        else
                            out[0] = 'w';
                        sendCommand(out);
                        break;
                    default:
                        break;
                }

                return true;
            }
        });


        right_button = (Button) findViewById(R.id.button_right);//���Ͱ�ť

        right_button.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        out[0] = 'd';
                        sendCommand(out);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (direction == 's')
                            out[0] = 's';
                        else
                            out[0] = 'w';
                        sendCommand(out);
                        break;
                    default:
                        break;
                }

                return true;
            }
        });

        stop_button = (Button) findViewById(R.id.button_stop);//���Ͱ�ť
        stop_button.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                sendCommand(out);
                out[0] = 't';
                sendCommand(out);
            }
        });

        fire_button = (Button) findViewById(R.id.button_fire);//���Ͱ�ť
        fire_button.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                out[0] = 'f';
                sendCommand(out);
            }
        });

        random_button = (Button) findViewById(R.id.button_random);//�����������ť
        random_button.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                out[0] = String.valueOf(Math.random()*7 + 1).getBytes()[0];
                sendCommand(out);
            }
        });

        mChatService = new BluetoothChatService(this, mHandler);

        mOutStringBuffer = new StringBuffer("");
    }

    @Override
    public synchronized void onPause()
    {
        super.onPause();
    }

    @Override
    public void onStop()
    {
        super.onStop();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (mChatService != null)
            mChatService.stop();
    }

    private void ensureDiscoverable()
    {
        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)
        {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    private void sendMessage(String message)
    {
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED)
        {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        if (message.length() > 0)
        {
            byte[] send = message.getBytes();
            mChatService.write(send);
            mOutStringBuffer.setLength(0);
            mOutEditText.setText(mOutStringBuffer);
        }
    }


    private void sendCommand(byte[] out)
    {
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED)
        {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        if (out != null)
        {
            mChatService.write(out);
            mOutStringBuffer.setLength(0);
            mOutEditText.setText(mOutStringBuffer);
        }
    }


    private TextView.OnEditorActionListener mWriteListener = new TextView.OnEditorActionListener()
    {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event)
        {
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP)
            {
                String message = view.getText().toString();
                sendMessage(message);
            }
            return true;
        }
    };

    private final Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MESSAGE_STATE_CHANGE:

                    break;
                case MESSAGE_WRITE://д��Ϣ
                    byte[] writeBuf = (byte[]) msg.obj;
                    String writeMessage = new String(writeBuf);
                    mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case MESSAGE_READ://����Ϣ
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    mConversationArrayAdapter.add(mConnectedDeviceName + ":  "+ readMessage);
                    break;
                case MESSAGE_DEVICE_NAME:
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(),"Connected to " + mConnectedDeviceName,
                            Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(),msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK)
                {
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    mChatService.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK)
                {
                    setupChat();
                }
                else
                {
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }


    private void remote_ctrl()
    {
        if (mBluetoothAdapter.isEnabled())
        {
            up_button = (Button) findViewById(R.id.button_up);//���Ͱ�ť
            up_button.setEnabled(true);
            down_button = (Button) findViewById(R.id.button_down);//���Ͱ�ť
            down_button.setEnabled(true);
            right_button = (Button) findViewById(R.id.button_right);//���Ͱ�ť
            right_button.setEnabled(true);
            left_button = (Button) findViewById(R.id.button_left);//���Ͱ�ť
            left_button.setEnabled(true);
            stop_button = (Button) findViewById(R.id.button_stop);//���Ͱ�ť
            stop_button.setEnabled(true);
            fire_button = (Button) findViewById(R.id.button_fire);//���Ͱ�ť
            fire_button.setEnabled(true);

            byte[] out = {0x01};
            sendCommand(out);
        }
    }


    private void auto_ctrl_1m()
    {
        if (mBluetoothAdapter.isEnabled())
        {
            byte[] out = {0x02};
            sendCommand(out);
        }
    }


    private void auto_ctrl_3m()
    {
        if (mBluetoothAdapter.isEnabled())
        {
            byte[] out = {0x03};
            sendCommand(out);
        }
    }


    private void auto_avoid_obstacle()
    {
        if (mBluetoothAdapter.isEnabled())
        {
            byte[] out = {0x04};
            sendCommand(out);
        }
    }



}
