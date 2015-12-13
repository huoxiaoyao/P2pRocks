package ch.ethz.inf.vs.allergictorockshittingmeintheface;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.util.Log;

import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Felix on 13.12.2015.
 */
public class P2PMaster implements WifiP2pManager.ConnectionInfoListener, PeerDiscoverListener{

    public static final String TAG = "## P2PMaster ##";

    protected final WifiP2pManager manager;
    protected final WifiP2pManager.Channel channel;

    private WiFiDirectBroadcastReceiver broadcastReceiver;

    public static final String TXTRECORD_PROP_AVAILABLE = "available";
    public static final String SERVICE_INSTANCE = "_safeMyAss";
    public static final String SERVICE_REG_TYPE = "_presence._tcp";

    private static final int SERVER_PORT = 4545;
    private static final int PORT = 5000;

    protected final PeerDiscovery peerDiscovery;

    private final WifiP2pDnsSdServiceInfo service;

    private final List<Socket> socketList;

    private boolean alarmActive = false;

    public static  P2PMaster createP2PMaster( final Activity activity ){

        P2PMaster master = new P2PMaster( activity );

        WiFiDirectBroadcastReceiver broadcastReceiver = new WiFiDirectBroadcastReceiver( master.manager, master.channel, master );

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        master.setBroadcastReceiver(broadcastReceiver);
        activity.registerReceiver( broadcastReceiver, intentFilter );

        return master;
    }

    private P2PMaster( final Activity activity ){

        manager = (WifiP2pManager) activity.getSystemService( Context.WIFI_P2P_SERVICE );
        channel = manager.initialize( activity, activity.getMainLooper(), null );

        peerDiscovery = new PeerDiscovery( manager, channel, SERVICE_INSTANCE );

        Map<String, String> record = new HashMap<String, String>();
        record.put(TXTRECORD_PROP_AVAILABLE, "visible");
        // TODO: maybe add more record entries

        service = WifiP2pDnsSdServiceInfo.newInstance( SERVICE_INSTANCE, SERVICE_REG_TYPE, record );

        socketList = new java.util.ArrayList<>();

    }

    private void setBroadcastReceiver( WiFiDirectBroadcastReceiver broadcastReceiver ){
        this.broadcastReceiver = broadcastReceiver;
    }

    public void invokeAlarm(){
        alarmActive = true;
        peerDiscovery.registerListener( this );
        peerDiscovery.startDiscovery();
    }

    public void revokeAlarm(){
        alarmActive = false;
    }

    public void acceptAlarms(){
        manager.clearLocalServices(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                manager.addLocalService(channel, service, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d( TAG, "add service success");
                    }

                    @Override
                    public void onFailure(int reason) {
                        // TODO: add some failure mechanism
                        Log.d( TAG, "add service failure");
                    }
                });
            }

            @Override
            public void onFailure(int reason) {
                // TODO: maybe add some failure mechanism
                Log.d( TAG, "add service failure");
            }
        });
    }

    public void rejectAlarms(){
        manager.clearLocalServices(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "remove service success");
            }

            @Override
            public void onFailure(int reason) {
                Log.d( TAG, "remove service failure");
            }
        });
    }

    public void addSocket( Socket socket ){
        socketList.add( socket );
    }

    @Override
    public void foundPeerDevice( final WifiP2pDevice device) {

        /*
        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                manager.createGroup(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {

                        Log.d(TAG, "create Group success");

                        */

                        WifiP2pConfig config = new WifiP2pConfig();
                        config.deviceAddress = device.deviceAddress;
                        config.wps.setup = WpsInfo.PBC;
                        config.groupOwnerIntent = 15;

                        manager.connect(channel, config, new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {
                                // TODO: specify if there is something here to do
                                Log.d(TAG, "try to connect success");
                            }

                            @Override
                            public void onFailure(int reason) {
                                // TODO: add some failure mechanism
                                Log.d(TAG, "try to connect failure");
                            }
                        });
                        /*
                    }

                    @Override
                    public void onFailure(int reason) {
                        Log.d(TAG, "create Group failure");
                    }
                });
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "remove Group failure");


            }
        });
        */
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        Log.d( TAG, "onConnectionInfoAvailable" );

        // TODO: sometimes this method is called even though no connection exists, which leads to errors in the current version

        if ( info.isGroupOwner )
        {
            new GroupOwnerSocketHandler( this, SERVER_PORT ).start();
        }
        else
        {
            new ClientSocketHandler( this, info.groupOwnerAddress, SERVER_PORT, PORT ).start();
        }
    }

}