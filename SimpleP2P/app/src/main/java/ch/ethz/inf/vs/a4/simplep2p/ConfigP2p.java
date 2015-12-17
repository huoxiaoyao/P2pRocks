package ch.ethz.inf.vs.a4.simplep2p;

/**
 * Created by Felix on 17.12.2015.
 */
public final class ConfigP2p {

    public static final long MIN_TIME_THRESHOLD = 500;
    public static final int MESSAGE_SIZE_LONG = 2048;
    public static final int MESSAGE_SIZE_SHORT = 256;
    public static final int BUFFER_SIZE = MESSAGE_SIZE_LONG;

    public static final int PREFIX_LENGTH = 5;
    public static final String ALARM_INIT = "AINIT";
    public static final String NEW_ALARM = "ANEW_";
    public static final String REVOKE_ALARM = "AREVO";
    public static final String INVOKE_POSITION_UPDATE = "APOUP";
    public static final String RECEIVE_POSITION_UPDATE = "RPOUP";

    public static final String JSON_LENGTH_DELIMITER = "£";

    public static final int SEND_TASK_QUEUE_SIZE = 5;
    public static final int SOCKET_QUEUE_SIZE = 5;

}
