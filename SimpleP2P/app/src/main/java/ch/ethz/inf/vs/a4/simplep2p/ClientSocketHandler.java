package ch.ethz.inf.vs.a4.simplep2p;


import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

/**
 * Created by Felix on 13.12.2015.
 */
public class ClientSocketHandler extends Thread {

    public static final String TAG = "## ClientSocket ##";

    private final InetAddress groupOwnerAddress;
    private final int serverPort;
    private final int port;

    private final P2PMaster master;
    private final SocketListenerThread socketListenerThread;

    public ClientSocketHandler( P2PMaster master, InetAddress groupOwnerAddress, int serverPort,int  port ) {
        this.master = master;
        this.groupOwnerAddress = groupOwnerAddress;
        this.serverPort = serverPort;
        this.port = port;

        socketListenerThread = new SocketListenerThread( master.taskQueue, master.socketList, Arrays.<LocationUpdateListener>asList( master ), false );

    }

    @Override
    public void run() {
        Socket socket = new Socket();
        try
        {
            Log.d( TAG, "connecting on socket" );
            socket.bind(null);
            socket.connect( new InetSocketAddress(groupOwnerAddress.getHostAddress(), serverPort), port );

            master.addSocket( socket );
            socketListenerThread.run();

            // TODO: specify what has to be done next
        }
        catch (IOException e)
        {
            e.printStackTrace();
            try
            {
                socket.close();
            } catch (IOException e1)
            {
                e1.printStackTrace();
            }
            return;
        }
    }

}
