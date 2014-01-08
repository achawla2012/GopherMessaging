package edu.umn.gophermagsensing;


import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class HomeScreen extends Activity {

  private static final String MY_API_KEY = "Gopher";
  private static boolean devicePairedFlag = false;
  private static boolean appStarted = false;
  private static BluetoothDevice remoteDevice = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home_screen);

    Button btnStartTest = (Button) findViewById(R.id.buttonStartTest);

    if (appStarted) { // this is to detect if user arrives at home screen after
                      // doing some action
      Intent calledIntent = getIntent();
      Bundle receivedExtras = calledIntent.getExtras();
      //if (calledIntent.hasExtra("devicePairedflag")) {
      if (receivedExtras != null) {
        devicePairedFlag = receivedExtras.getBoolean("devicePairedflag", false);
        remoteDevice = receivedExtras.getParcelable("remotedevice");
        
        if (devicePairedFlag) {
          btnStartTest.setEnabled(true);
        } else {
          btnStartTest.setEnabled(false);
        }
      } else {
        if (devicePairedFlag) {
          btnStartTest.setEnabled(true);
        } else {
          btnStartTest.setEnabled(false);
        }
        btnStartTest.setEnabled(devicePairedFlag);
      }
    } else {
      btnStartTest.setEnabled(false);
    }

    appStarted = true;
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.home_screen, menu);
    return true;
  }

  public void scanDevices(View view) { // goto scanDevices
    if (view.getId() == R.id.buttonScan) {
      Intent intent = new Intent(this, ScanDevices.class);
      startActivity(intent);
    }
  }

  public void startTest(View view) { // goto starttest page, pass remoteDevice
                                     // details
    if (view.getId() == R.id.buttonStartTest) {

      Bundle bundle = new Bundle();
      bundle.putParcelable("remotedevice", remoteDevice);

      Intent intent = new Intent(this, StartTest.class);
      // intent.putExtra("devicePairedflag", devicePaired);
      intent.putExtras(bundle);
      startActivity(intent);
    }
  }

  public void emailResults(View view) { // goto email page
    if (view.getId() == R.id.buttonEmail) {
      Intent intent = new Intent(this, SendEmail.class);
      startActivity(intent);

    }
  }

  public void gotoSettings(View view) { // goto settings page
    if (view.getId() == R.id.buttonSettings) {
      Intent intent = new Intent(this, Settings.class);
      startActivity(intent);
    }
  }

}
