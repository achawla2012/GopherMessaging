package edu.umn.gophermagsensing;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class ScanDevices extends Activity {

  private static final int REQUEST_ENABLE_BT = 1;
  private int displayDevice = 0;
  private boolean devicePaired = false;
  // private static Object lock = new Object;

  ListView listDevicesFound;
  Button btnScanDevice;
  TextView stateBluetooth;
  BluetoothAdapter bluetoothAdapter;
  BluetoothDevice pairedDevice;

  ArrayAdapter<String> btArrayAdapter;
  ArrayList<BluetoothDevice> arrayListDiscoveredDevices;
  Set<BluetoothDevice> arrayListPairedDevices;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.scandevices);

    btnScanDevice = (Button) findViewById(R.id.btnscandevice);

    stateBluetooth = (TextView) findViewById(R.id.bluetoothstate);
    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    listDevicesFound = (ListView) findViewById(R.id.devicesfound);
    btArrayAdapter =
        new ArrayAdapter<String>(ScanDevices.this,
            android.R.layout.simple_list_item_1);
    listDevicesFound.setAdapter(btArrayAdapter);

    CheckBlueToothState();

    btnScanDevice.setOnClickListener(btnScanDeviceOnClickListener);
    listDevicesFound.setOnItemClickListener(listDevicesOnClickListener);

    arrayListDiscoveredDevices = null;
    arrayListDiscoveredDevices = new ArrayList<BluetoothDevice>();
    arrayListPairedDevices = null;
    arrayListPairedDevices = bluetoothAdapter.getBondedDevices();

    registerReceiver(ActionFoundReceiver, new IntentFilter(
        BluetoothDevice.ACTION_FOUND));
  }

  @Override
  protected void onDestroy() {
    // TODO Auto-generated method stub
    super.onDestroy();
    unregisterReceiver(ActionFoundReceiver);
  }

  private void CheckBlueToothState() {
    if (bluetoothAdapter == null) {
      stateBluetooth.setText("Bluetooth NOT support");
    } else {
      if (bluetoothAdapter.isEnabled()) {
        if (bluetoothAdapter.isDiscovering()) {
          stateBluetooth
              .setText("Bluetooth is currently in device discovery process.");
        } else {
          stateBluetooth.setText("Bluetooth is Enabled.");
          btnScanDevice.setEnabled(true);
        }
      } else {
        stateBluetooth.setText("Bluetooth is NOT Enabled!");
        Intent enableBtIntent =
            new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
      }
    }
    }

  private Button.OnClickListener btnScanDeviceOnClickListener =
      new Button.OnClickListener() {

  @Override
        public void onClick(View arg0) {
          // TODO Auto-generated method stub
          bluetoothAdapter.cancelDiscovery();
          btArrayAdapter.clear();
          arrayListDiscoveredDevices.clear();
          // synchronized (ScanDevices.this) {
          displayDevice = 0;
          // }
          btArrayAdapter.add("No RN42 found \n Wait for few seconds...");
          btArrayAdapter.notifyDataSetChanged();
          bluetoothAdapter.startDiscovery();
          // stateBluetooth.setText("Scanning for Devices...");
        }
      };

  private AdapterView.OnItemClickListener listDevicesOnClickListener =
      new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View view, int i, long l) {
          boolean pairedFlag = false;
          if (arrayListDiscoveredDevices.isEmpty()) {
            String alertMsg =
                "Wait for few seconds and press Scan Devices again...";
            String alertTitle = "Alert";
            new AlertDialog.Builder(ScanDevices.this).setTitle(alertTitle)
                .setMessage(alertMsg)
                .setNeutralButton("OK", new OkOnClickListener()).show();
          } else {
            BluetoothDevice device = arrayListDiscoveredDevices.get(i);
            for (BluetoothDevice bt : arrayListPairedDevices) {
              if (bt.getAddress().toString()
                  .equals(device.getAddress().toString())) {
                Log.d("Pairing", "Device already paired");
                pairedFlag = true;
                break;
              }
            }
            if (pairedFlag) {
              pairedDevice = device;
              String alertMsg =
                  "Device : " + device.getName() + " is already paired";
              String alertTitle = "Info";
              new AlertDialog.Builder(ScanDevices.this).setTitle(alertTitle)
                  .setMessage(alertMsg)
                  .setNeutralButton("OK", new OkOnClickListener()).show();
            } else {
            pairDevice(device);
            }
          }
        }
      };

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    // TODO Auto-generated method stub
    if (requestCode == REQUEST_ENABLE_BT) {
      CheckBlueToothState();
    }
  }

  private final BroadcastReceiver ActionFoundReceiver =
      new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
          // TODO Auto-generated method stub
          String action = intent.getAction();
          boolean pairedFlag = false;
          if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            BluetoothDevice device =
                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            // Log.d("BR", "Receiving : " + device.getName());
            if (device.getName().startsWith("RN42")) { // Listing only RN42 BT
                                                       // devices
              // synchronized (ScanDevices.this) {
              if (displayDevice == 0) {
                  displayDevice = 1;
                  btArrayAdapter.clear();
                  arrayListDiscoveredDevices.clear();
                }

              arrayListDiscoveredDevices.add(device);

              btArrayAdapter.add(device.getName() + "\n" + device.getAddress());
              btArrayAdapter.notifyDataSetChanged();
            }
          }
        }
      };

  private void pairDevice(BluetoothDevice device) {
    try {
      Log.d("pairDevice()", "Start Pairing...");
      Method m = device.getClass().getMethod("createBond", (Class[]) null);
      m.invoke(device, (Object[]) null);
      Log.d("pairDevice()", "Pairing finished.");

      pairedDevice = device;

      String alertMsg = "Device : " + device.getName() + " is paired";
      String alertTitle = "Info";
      new AlertDialog.Builder(ScanDevices.this).setTitle(alertTitle)
          .setMessage(alertMsg).setNeutralButton("OK", new OkOnClickListener())
          .show();

    } catch (Exception e) {
      Log.e("pairDevice()", e.getMessage());
    }
  }

  private final class OkOnClickListener implements
      DialogInterface.OnClickListener {
    public void onClick(DialogInterface dialog, int which) {
      devicePaired = true;

      Bundle bundle = new Bundle();
      bundle.putBoolean("devicePairedflag", devicePaired);
      bundle.putParcelable("remotedevice", pairedDevice);

      Intent intent = new Intent(ScanDevices.this, HomeScreen.class);
      // intent.putExtra("devicePairedflag", devicePaired);
      intent.putExtras(bundle);
      startActivity(intent);
    }
  }
}