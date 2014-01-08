package edu.umn.gophermagsensing;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class StartTest extends Activity {

  EditText pName, pGender, pEmail, pAge;
  String ptName, ptGender, ptEmail, ptAge;
  BluetoothDevice btremoteDevice;
  BluetoothAdapter btBluetoothAdapter;
  BluetoothSocket btSocket;
  BluetoothDevice btDevice;
  OutputStream btOutputStream;
  InputStream btInputStream;
  BufferedReader btReader;
  byte[] buffer = new byte[1024];
  int bytes;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.starttest);
    
    btremoteDevice = getIntent().getExtras().getParcelable("remotedevice");
    Log.d("Starttest", btremoteDevice.getName());

    pName = (EditText) findViewById(R.id.editName);
    pGender = (EditText) findViewById(R.id.editGender);
    pEmail = (EditText) findViewById(R.id.editEmail);
    pAge = (EditText) findViewById(R.id.editAge);


  }

  public void saveUserInfo(View view) {
    if (view.getId() == R.id.buttonSaveUserInfo) {

      ptName = pName.getText().toString();
      ptGender = pGender.getText().toString();
      ptEmail = pEmail.getText().toString();
      ptAge = pAge.getText().toString();

      String alertMsg =
          "User Information \n" + ptName + "\n" + ptGender + "\n" + ptEmail
              + "\n" + ptAge + "\nSaved";
      String alertTitle = "Info";
      new AlertDialog.Builder(this).setTitle(alertTitle).setMessage(alertMsg)
          .setNeutralButton("OK", new OkOnClickListener()).show();

    }

  }

  private final class OkOnClickListener implements
      DialogInterface.OnClickListener {
    public void onClick(DialogInterface dialog, int which) {

      Bundle bundle = new Bundle();
      bundle.putParcelable("remotedevice", btremoteDevice);

      Intent intent = new Intent(StartTest.this, StartTestMsg.class);
      // intent.putExtra("devicePairedflag", devicePaired);
      intent.putExtras(bundle);
      startActivity(intent);

    }
  }


}
