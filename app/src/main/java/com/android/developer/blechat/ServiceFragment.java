package com.android.developer.blechat;

import android.app.Fragment;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.ParcelUuid;

public abstract class ServiceFragment extends Fragment{
    public abstract BluetoothGattService getBluetoothGattService();
    public abstract ParcelUuid getServiceUUID();
    /*
     This interface must be implemented by activities that contain a ServiceFragment to allow an
     interaction in the fragment to be communicated to the activity.
    */
    public interface ServiceFragmentDelegate {
        void sendNotificationToDevices(BluetoothGattCharacteristic characteristic);
    }

    public int writeCharacteristic(BluetoothGattCharacteristic characteristic, int offset, byte[] value) {
        throw new UnsupportedOperationException("Method writeCharacteristic not overriden");
    };
}