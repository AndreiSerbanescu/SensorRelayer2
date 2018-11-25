package com.android.developer.blechat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.developer.blechat.ServiceFragment.ServiceFragmentDelegate;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements ServiceFragmentDelegate {

    private static final int REQUEST_ENABLE_BLUETOOTH = 1;
    private static final String TAG = MainActivity.class.getCanonicalName();
    private static final String CURRENT_FRAGMENT_TAG = "CURRENT_FRAGMENT";
    private static final UUID CHARACTERISTIC_USER_DESCRIPTION_UUID = UUID
            .fromString("00002901-0000-1000-8000-00805f9b34fb");
    private static final UUID CLIENT_CHARACTERISTIC_CONFIGURATION_UUID = UUID
            .fromString("00002902-0000-1000-8000-00805f9b34fb");

    private ServiceFragment mSendTextFragment;
    private BluetoothGattService mBluetoothGattService;
    private HashSet<BluetoothDevice> mBluetoothDevices;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private AdvertiseData mAdvertiseData;
    private AdvertiseData mAdvertiseScanResponse;
    private AdvertiseSettings mAdvertiseSettings;
    private BluetoothLeAdvertiser mAdvertiser;
    private BluetoothGattServer mBluetoothGattServer;

    private String mResponse = "response";

    private final AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        //reference: https://developer.android.com/reference/android/bluetooth/le/AdvertiseCallback.html
        //Bluetooth LE advertising callbacks, used to deliver advertising operation status.
        public void onStartFailure(int errorCode) {
            //Callback when advertising could not be started.
            super.onStartFailure(errorCode);
            Log.e(TAG, "Not broadcasting: " + errorCode);
        }

        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
        /*
        Callback triggered in response to startAdvertising(AdvertiseSettings, AdvertiseData,
        AdvertiseCallback) indicating that the advertising has been started successfully.
        */
            super.onStartSuccess(settingsInEffect);
            Log.v(TAG, "Broadcasting");
        }
    };
    /*
    Public API for the Bluetooth GATT Profile server role.
    This class provides Bluetooth GATT server role functionality, allowing applications to create
    Bluetooth Smart services and characteristics.
    BluetoothGattServer is a proxy object for controlling the Bluetooth Service via IPC.
    Use openGattServer(Context, BluetoothGattServerCallback) to get an instance of this class.
     */
    private final BluetoothGattServerCallback mGattServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset,
                                                BluetoothGattCharacteristic characteristic) {
            //A remote client has requested to read a local characteristic.
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            Log.d(TAG, "Device tried to read characteristic: " + characteristic.getUuid());
            Log.d(TAG, "Value: " + Arrays.toString(characteristic.getValue()));
            if (offset != 0) {
                //mResponse is the response sent whenever a response is requested from the Dragonboard
                //When null, the Dragonboard ceases to communicate with the Android App after some seconds
                mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_INVALID_OFFSET, offset,
                        mResponse.getBytes());
                return;
            }
            mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS,
                    offset, characteristic.getValue());
        }

        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            super.onNotificationSent(device, status);
            Log.v(TAG, "Notification sent. Status: " + status);
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
                                                 BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded,
                                                 int offset, byte[] value) {
            //A remote client has requested to write to a local characteristic.
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite,
                    responseNeeded, offset, value);
            Log.v(TAG, "Characteristic Write request: " + Arrays.toString(value));
            int status = mSendTextFragment.writeCharacteristic(characteristic, offset, value);
            characteristic.setValue(value);
            //mResponse is the response sent whenever a response is requested from the Dragonboard
            //When null, the Dragonboard ceases to communicate with the Android App after some seconds
            if (responseNeeded) {
                mBluetoothGattServer.sendResponse(device, requestId, status, 0,
                        mResponse.getBytes());
            }
        }

        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId,
                                            int offset, BluetoothGattDescriptor descriptor) {
            //A remote client has requested to read a local descriptor.
            super.onDescriptorReadRequest(device, requestId, offset, descriptor);
            Log.d(TAG, "Device tried to read descriptor: " + descriptor.getUuid());
            Log.d(TAG, "Value: " + Arrays.toString(descriptor.getValue()));
            if (offset != 0) {
                mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_INVALID_OFFSET, offset,
                        mResponse.getBytes());
                return;
            }
            //mResponse is the response sent whenever a response is requested from the Dragonboard
            //When null, the Dragonboard ceases to communicate with the Android App after some seconds
            mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
                    descriptor.getValue());
        }

        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId,
                                             BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded,
                                             int offset,
                                             byte[] value) {
            //A remote client has requested to write to a local descriptor.
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded,
                    offset, value);
            descriptor.setValue(value);
            //mResponse is the response sent whenever a response is requested from the Dragonboard
            //When null, the Dragonboard ceases to communicate with the Android App after some seconds
            if (responseNeeded) {
                mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0,
                        mResponse.getBytes());
            }
            Log.v(TAG, "Descriptor Write Request " + descriptor.getUuid() + " " + Arrays.toString(value));
        }

        @Override
        //This abstract class is used to implement BluetoothGattServer callbacks.
        public void onConnectionStateChange(BluetoothDevice device, final int status, int state) {
            //Callback indicating when a remote device has been connected or disconnected.
            super.onConnectionStateChange(device, status, state);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (state == BluetoothGatt.STATE_DISCONNECTED) {
                    mBluetoothDevices.remove(device);
                } else if (state == BluetoothGatt.STATE_CONNECTED) {
                    mBluetoothDevices.add(device);
                }
            } else {
                mBluetoothDevices.remove(device);
                // There are too many gatt errors (some of them not even in the documentation) so we just
                // show the error to the user.
                final String error = getString(R.string.errorWhenConnecting) + ": " + status;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                });
                Log.e(TAG, "Error: " + status);
            }
        }
    };

                // Lifecycle Callbacks
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
            if (this.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
            if (this.checkSelfPermission(android.Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(new String[]{android.Manifest.permission.BLUETOOTH_ADMIN}, 1);
            }
        }

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE not supported", Toast.LENGTH_SHORT).show();
            finish();
        }
        mBluetoothDevices = new HashSet<>();
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        mSendTextFragment = new SendTextFragment();
        getFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, mSendTextFragment, CURRENT_FRAGMENT_TAG)
                .commit();
        mBluetoothGattService = mSendTextFragment.getBluetoothGattService();
        mAdvertiseSettings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                .setConnectable(true)
                .build();
        mAdvertiseScanResponse = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .build();
        mAdvertiseData = new AdvertiseData.Builder()
                .setIncludeTxPowerLevel(true)
                .addServiceUuid(mSendTextFragment.getServiceUUID())
                .build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            if (resultCode == RESULT_OK) {
                if (!mBluetoothAdapter.isMultipleAdvertisementSupported()) {
                    //Toast.makeText(this, R.string.bluetoothAdvertisingNotSupported, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Advertising not supported");
                }
                onStart();
            } else {
                Toast.makeText(this, R.string.bluetoothNotEnabled, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Bluetooth not enabled");
                finish();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
//        updateConnectedDevicesStatus();
        // If the user disabled Bluetooth when the app was in the background,
        // openGattServer() will return null.
        mBluetoothGattServer = mBluetoothManager.openGattServer(this, mGattServerCallback);
        if (mBluetoothGattServer == null) {
            if (mBluetoothAdapter == null) {
                Toast.makeText(this, R.string.bluetoothNotSupported, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Bluetooth not supported");
                finish();
            } else if (!mBluetoothAdapter.isEnabled()) {
                // Make sure bluetooth is enabled.
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
            }
            return;
        }
        // Add a service for a total of three services (Generic Attribute and Generic Access
        // are present by default).
        mBluetoothGattServer.addService(mBluetoothGattService);
        mAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
        mAdvertiser.startAdvertising(mAdvertiseSettings, mAdvertiseData, mAdvertiseScanResponse, mAdvertiseCallback);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAdvertiser != null && mBluetoothAdapter.isEnabled()) {
            mAdvertiser.stopAdvertising(mAdvertiseCallback);
        }
        if (mBluetoothGattServer != null) {
            mBluetoothGattServer.close();
        }
    }

    @Override
    public void sendNotificationToDevices(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothDevices.isEmpty()) {
            Toast.makeText(this, R.string.bleDeviceNotConnected, Toast.LENGTH_SHORT).show();
        } else {
            boolean indicate = (characteristic.getProperties()
                    & BluetoothGattCharacteristic.PROPERTY_INDICATE)
                    == BluetoothGattCharacteristic.PROPERTY_INDICATE;
            for (BluetoothDevice device : mBluetoothDevices) {
                mBluetoothGattServer.notifyCharacteristicChanged(device, characteristic, indicate);
            }
        }
    }
                // Bluetooth
    public static BluetoothGattDescriptor getCharacteristicUserDescriptionDescriptor(String defaultValue) {
        BluetoothGattDescriptor bluetoothGattDescriptor = new BluetoothGattDescriptor(
                CHARACTERISTIC_USER_DESCRIPTION_UUID,
                (BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE));
        try {
            bluetoothGattDescriptor.setValue(defaultValue.getBytes("UTF-8"));
        } finally {
            return bluetoothGattDescriptor;
        }
    }

    public static BluetoothGattDescriptor getClientCharacteristicConfigurationDescriptor() {
      /*Represents a Bluetooth GATT Descriptor
        GATT Descriptors contain additional information and attributes of a GATT characteristic,
        BluetoothGattCharacteristic.
        They can be used to describe the characteristic's features or to control
        certain behaviours of the characteristic.
       */
        BluetoothGattDescriptor descriptor = new BluetoothGattDescriptor(
                CLIENT_CHARACTERISTIC_CONFIGURATION_UUID,
                (BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE));
        descriptor.setValue(new byte[]{0, 0});
        return descriptor;
    }
}