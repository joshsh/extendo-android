package net.fortytwo.extendo.brainstem.bluetooth;

import android.content.Context;
import android.util.Log;
import at.abraxas.amarino.Amarino;
import com.illposed.osc.OSCMessage;
import net.fortytwo.extendo.brainstem.Brainstem;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public abstract class BluetoothDeviceControl {
    private final String address;
    private Context context;

    public BluetoothDeviceControl(final String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void connect(final Context context) {
        // connect to the specific Bluetooth device
        Amarino.connect(context, address);

        // also keep this context for sending of messages (note: we assume the same context is used throughout)
        this.context = context;

        onConnect();
    }

    public void disconnect(final Context context) {
        // if you connect in onStart() you must not forget to disconnect when your app is closed
        Amarino.disconnect(context, address);
    }

    // override this method
    protected void onConnect() {
    }

    public void sendOSCMessage(OSCMessage message) {
        if (null == context) {
            Log.w(Brainstem.TAG, "can't send OSC message; context is null");
            return;
        }

        String serialMessage = new String(message.getByteArray());
        //Log.i(Brainstem.TAG, "sending OSC message of length " + serialMessage.length());
        Amarino.sendDataToArduino(context, address, 'o', serialMessage);
    }

    public class DeviceInitializationException extends Exception {
        public DeviceInitializationException(final Throwable cause) {
            super(cause);
        }
    }
}
