package com.android.developer.blechat;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;


import java.util.StringTokenizer;
import java.util.UUID;

public class SendTextFragment extends ServiceFragment {
    private static final UUID CHAT_SERVICE_UUID = UUID
            .fromString("0000180F-0000-1000-8000-00805f9b34fb");
    private static final UUID SENT_DATA_UUID = UUID
            .fromString("00000000-0000-1000-8000-00805f9b34fb");
    private static final UUID RECEIVED_DATA_UUID = UUID
            .fromString("00000003-0000-1000-8000-00805f9b34fb");
    private static final UUID BLE_CHAT_INFO_UUID = UUID
            .fromString("00000004-0000-1000-8000-00805f9b34fb");
    private static final String SENT_DATA_DESCRIPTION = "Data sent from the Android device";

    private ServiceFragmentDelegate mDelegate;

    private ImageButton mSendButton;
    private ImageButton mClearButton;
    private EditText mEditText;
    private ListView mChatListView;
    private ArrayAdapter<String> mConversationArrayAdapter;

    private BluetoothGattService mChatService;
    private BluetoothGattCharacteristic mSentDataCharacteristic;
    private BluetoothGattCharacteristic mReceivedDataCharacteristic;
    private BluetoothGattCharacteristic mBLEChatInfoCharacteristic;

    public SendTextFragment() {
        mChatService = new BluetoothGattService(CHAT_SERVICE_UUID,
                BluetoothGattService.SERVICE_TYPE_PRIMARY);

        //Characteristic that sends data to the Dragonboard
        mSentDataCharacteristic =
                new BluetoothGattCharacteristic(SENT_DATA_UUID,
                        BluetoothGattCharacteristic.PROPERTY_READ |
                        BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                        BluetoothGattCharacteristic.PERMISSION_READ);
        mSentDataCharacteristic.addDescriptor(
                MainActivity.getClientCharacteristicConfigurationDescriptor());
        mSentDataCharacteristic.addDescriptor(
                MainActivity.getCharacteristicUserDescriptionDescriptor(SENT_DATA_DESCRIPTION));

        //Characteristic that receives data from the Dragonboard
        mReceivedDataCharacteristic =
                new BluetoothGattCharacteristic(RECEIVED_DATA_UUID,
                        BluetoothGattCharacteristic.PROPERTY_READ |
                        BluetoothGattCharacteristic.PROPERTY_NOTIFY |
                        BluetoothGattCharacteristic.PROPERTY_WRITE |
                        BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                        BluetoothGattCharacteristic.PERMISSION_READ |
                        BluetoothGattCharacteristic.PERMISSION_WRITE);
        mReceivedDataCharacteristic.addDescriptor(
                MainActivity.getClientCharacteristicConfigurationDescriptor());
        mReceivedDataCharacteristic.addDescriptor(
                MainActivity.getCharacteristicUserDescriptionDescriptor(SENT_DATA_DESCRIPTION));

        //Characteristic that send info to help the Dragonboard indentify the application
        //The Dragonboard will check if the value of this characteristic is BLECHAT. If true, the connection is made.
        mBLEChatInfoCharacteristic =
                new BluetoothGattCharacteristic(BLE_CHAT_INFO_UUID,
                        BluetoothGattCharacteristic.PROPERTY_READ |
                                BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                        BluetoothGattCharacteristic.PERMISSION_READ);
        mBLEChatInfoCharacteristic.addDescriptor(
                MainActivity.getClientCharacteristicConfigurationDescriptor());
        mBLEChatInfoCharacteristic.addDescriptor(
                MainActivity.getCharacteristicUserDescriptionDescriptor(SENT_DATA_DESCRIPTION));

        mChatService.addCharacteristic(mSentDataCharacteristic);
        mChatService.addCharacteristic(mReceivedDataCharacteristic);
        mChatService.addCharacteristic(mBLEChatInfoCharacteristic);
    }

                // Lifecycle callbacks
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_send_text, container, false);

        mChatListView = view.findViewById(R.id.chat_list);
        mConversationArrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.message);
        mChatListView.setAdapter(mConversationArrayAdapter);
        mChatListView.setOnItemClickListener(new ChatItemOnClickListener(getContext()));

        //Initial values for the characteristics
        mReceivedDataCharacteristic.setValue(0, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
        mBLEChatInfoCharacteristic.setValue(getResources().getString(R.string.app_name));


        // added mock values - TODO remove
        mConversationArrayAdapter.add("Sensor value registered: 10");
        mConversationArrayAdapter.add("Sensor value registered: 100");
        mConversationArrayAdapter.add("Sensor value registered: 65");
        mChatListView.setAdapter(mConversationArrayAdapter);
        mConversationArrayAdapter.notifyDataSetChanged();

        return view;
    }

    //Method that store the value entered by the user in the mSentDataCharacteristic and notify it
    private final View.OnClickListener mSendButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String s = mEditText.getText().toString();
            mSentDataCharacteristic.setValue(s);
            mConversationArrayAdapter.add("Me: " + s);
            mConversationArrayAdapter.notifyDataSetChanged();
            mEditText.setText("");
            mDelegate.sendNotificationToDevices(mSentDataCharacteristic);
        }
    };

    //Method that clears the listView content and the mSentDataCharacteristic characteristic value
    private final View.OnClickListener mClearButtonListener = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            mSentDataCharacteristic.setValue("");
            mReceivedDataCharacteristic.setValue("");
            mConversationArrayAdapter.clear();
            mEditText.setText("");
        }
    };

    public BluetoothGattService getBluetoothGattService() {
        return mChatService;
    }

    @Override
    public ParcelUuid getServiceUUID() {
        return new ParcelUuid(CHAT_SERVICE_UUID);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mDelegate = (ServiceFragmentDelegate) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()+" must implement ServiceFragmentDelegate");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mDelegate = null;
    }


    //Method called whenever the dragoșboard tries to write some value to a characteristic
    @Override
    public int writeCharacteristic(BluetoothGattCharacteristic characteristic, int offset, byte[] value) {
        final String s = new String(value);
        //Runnable to refresh the ListView whenever data is received


        //parse sensor data
        StringTokenizer st = new StringTokenizer(s, ":");
        final String actualValue = st.nextToken().trim();

        Log.d("SENSOR VALUE", actualValue);


        getActivity().runOnUiThread(() -> {

            try {

                int sensorData = Integer.parseInt(actualValue);

                if (sensorData >= 50) {
                    popUpDialogWindow(sensorData);
                }

            } catch (NumberFormatException e) {

            }

            mConversationArrayAdapter.add("Sensor value registered: " + s);
            mChatListView.setAdapter(mConversationArrayAdapter);
            mConversationArrayAdapter.notifyDataSetChanged();
        });
        return BluetoothGatt.GATT_SUCCESS;
    }

    private void popUpDialogWindow(int sensorValue) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("SENSOR VALUE " + sensorValue)
                .setPositiveButton("KAY", (dialog, id) -> {})
                .setNegativeButton("CHEERS", (dialog, id) -> {});
        // Create the AlertDialog object and return it
        builder.show();
    }

    private static class ChatItemOnClickListener implements AdapterView.OnItemClickListener {

        private final Context context;
        private ChatItemOnClickListener(Context context) {
            this.context = context;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            String measurementInfo = extractMeasurementInfo((String) parent.getAdapter().getItem(position));

            AlertDialog.Builder builder = new AlertDialog.Builder(context);


            String measurementAlert = "Measurement: " + measurementInfo;
            String dateAlert = "Date received: 26/11/18";
            String timeAlert = "Time received: 23:22";
            String sensorIdAlert = "Sensor id: 1122";

            builder.setMessage(measurementAlert + "\n" + dateAlert + "\n"
                                + timeAlert + "\n" + sensorIdAlert)
                    .setPositiveButton("GOT IT", (dialog, idd) -> {});
            // Create the AlertDialog object and return it
            builder.show();
        }

        private String extractMeasurementInfo(String fullMeasurementInfo) {

            StringTokenizer st = new StringTokenizer(fullMeasurementInfo);

            String lastToken = "";

            while (st.hasMoreTokens()) {
                lastToken = st.nextToken();
            }

            return lastToken;
        }
    }
}