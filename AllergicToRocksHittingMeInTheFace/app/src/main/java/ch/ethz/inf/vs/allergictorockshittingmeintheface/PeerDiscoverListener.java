package ch.ethz.inf.vs.allergictorockshittingmeintheface;

import android.net.wifi.p2p.WifiP2pDevice;

/**
 * Created by Felix on 13.12.2015.
 */
interface PeerDiscoverListener {
    public void foundPeerDevice( WifiP2pDevice device );
}
