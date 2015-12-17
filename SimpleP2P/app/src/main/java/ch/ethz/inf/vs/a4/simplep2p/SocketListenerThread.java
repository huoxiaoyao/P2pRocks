package ch.ethz.inf.vs.a4.simplep2p;

import android.location.Location;

import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Felix on 17.12.2015.
 */

class SocketListenerThread extends Thread {

    public static final String TAG = "##SocketListener ##";

    private final ArrayBlockingQueue<SendTask> taskQueue;
    private final LinkedBlockingQueue<Socket> socketList;
    private final List<LocationUpdateListener> locationUpdateListenerList;

    private final boolean isServer;
    private final List<InetAddress> alarmInvoker;

    private volatile boolean stopVar = false;

    private byte[] buffer;

    public SocketListenerThread( ArrayBlockingQueue<SendTask> taskQueue, LinkedBlockingQueue<Socket> socketList, List<LocationUpdateListener> locationUpdateListenerList, boolean isServer ){

        this.taskQueue = taskQueue;
        this.socketList = socketList;
        this.locationUpdateListenerList = locationUpdateListenerList;
        this.isServer = isServer;

        buffer = new byte[ ConfigP2p.BUFFER_SIZE ];
        alarmInvoker = new java.util.ArrayList<>( );
    }

    public final void run() {
        while ( !this.stopVar ) {
            long start_time = System.currentTimeMillis();

            if ( !taskQueue.isEmpty() )
            {
                try
                {
                    SendTask sendTask = taskQueue.take();
                    sendAll( sendTask.message.getBytes() );
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }

            for ( Socket socket : socketList ){
                try
                {
                    if ( socket.getInputStream().available() != 0 )
                    {
                        socket.getInputStream().read( buffer );
                        String message = new String( buffer, 0, ConfigP2p.MESSAGE_SIZE_SHORT );
                        String prefix = message.substring( 0, ConfigP2p.PREFIX_LENGTH );
                        String json = message.substring( ConfigP2p.PREFIX_LENGTH, message.length() );

                        // TODO: make prefix tests

                        if ( isServer )
                        {
                            if ( prefix.equals( ConfigP2p.ALARM_INIT ) && !alarmInvoker.contains( socket.getInetAddress() ) )
                            {
                                alarmInvoker.add(socket.getInetAddress());

                                // TODO: parse from json
                                String testPayload = Double.toString( 47.37794d )  + "#" + Double.toString( 8.54020d );

                                Location mDestLocation = new Location("");
                                mDestLocation.setLatitude( 47.37794d );
                                mDestLocation.setLongitude( 8.54020d );
                                updateLocation(mDestLocation);

                                sendAll((ConfigP2p.ALARM_INIT + testPayload).getBytes());
                            }
                        }
                        else
                        {
                            if ( prefix.equals( ConfigP2p.ALARM_INIT )  )
                            {
                                // TODO: extract postition and give to listener
                                String[] args = json.split( "#" );

                                Location mDestLocation = new Location("");
                                mDestLocation.setLatitude(Double.parseDouble(args[0]));
                                mDestLocation.setLongitude( Double.parseDouble( args[1] ) );
                                updateLocation(mDestLocation);
                            }
                        }


                    }
                }
                catch ( IOException e )
                {

                }
            }

            long end_time = System.currentTimeMillis();
            long elapsed_ns = end_time - start_time;
            if ( elapsed_ns < ConfigP2p.MIN_TIME_THRESHOLD) {
                try {
                    Thread.sleep ( ConfigP2p.MIN_TIME_THRESHOLD  - elapsed_ns );
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendAll( byte[] message ){

        byte[] sendMessage = Arrays.copyOf( message, ConfigP2p.MESSAGE_SIZE_LONG );

        for( Socket socket : socketList ){
            try {
                socket.getOutputStream().write( message );
                socket.getOutputStream().flush();
            } catch (IOException e) {
                try
                {
                    socket.close();
                }
                catch (IOException e2)
                {
                    e.printStackTrace();
                }
                finally
                {
                    socketList.remove( socket );
                }
            }
        }
    }

    private void updateLocation( Location location ){
        for ( LocationUpdateListener listener : locationUpdateListenerList ){
            listener.onLocationUpdate( location );
        }
    }

}
