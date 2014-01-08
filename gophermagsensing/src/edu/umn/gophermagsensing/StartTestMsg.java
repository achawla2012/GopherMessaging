package edu.umn.gophermagsensing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
import android.widget.TextView;

public class StartTestMsg extends Activity {

  public enum State {
    Idle, ConfigSent, ConfigRepRecvd, StartSent, StartRepRecvd, RecvReport, StopSent, StopRepRecvd
  };

  EditText pName, pGender, pEmail, pAge;
  String ptName, ptGender, ptEmail, ptAge;

  State stateType = State.Idle;

  TextView btState, msgState;

  BluetoothAdapter btBluetoothAdapter;
  BluetoothSocket btSocket;
  BluetoothDevice btremoteDevice;
  OutputStream btOutputStream;
  InputStream btInputStream;
  BufferedReader btReader;

  private Thread _workerThread;
  private byte[] _readBuffer;
  private int _readBufferPosition;
  private volatile boolean _stopWorker;

  byte[] sendbuffer = null;

  boolean socketFlag = false;
  byte[] buffer = new byte[1024];
  int bytes;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.starttest_msg);

    btState = (TextView) findViewById(R.id.textBTstate);
    btState.setText("Bluetooth Not Opened");

    msgState = (TextView) findViewById(R.id.textMsgStatus);
    msgState.setText("Data not sent yet");

    btremoteDevice = getIntent().getExtras().getParcelable("remotedevice");
    Log.d("Starttestmsg", btremoteDevice.getName());

    try {
      connectBT();
      socketFlag = true;

      beginListenForData();

      sendConfigPacket();
      while (stateType != State.ConfigRepRecvd) {
        try {
          Thread.sleep(1000);
          Log.d("KK", "Waiting for Config Reply to send start packet. State="
              + stateType.toString());
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          Log.d("KK", "Exception in Sleep");
          e.printStackTrace();
        }
      }

      sendStartPacket();

    } catch (IOException e) {
      // TODO Auto-generated catch block
      Log.d("ConenctBT", "Exception ");
      socketFlag = false;
      btState.setText("Bluetooth NOT Opened. Try again !");
    }


  }

  public void connectBT() throws IOException {
    if (btremoteDevice != null) {
      UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");// Standard
                                                                          // SerialPortService
                                                                          // ID
      // UUID uuid = UUID.fromString("1101"); // Standard SerialPortService ID
      btSocket = btremoteDevice.createRfcommSocketToServiceRecord(uuid);
      btSocket.connect();
      btOutputStream = btSocket.getOutputStream();
      btInputStream = btSocket.getInputStream();
      btReader = new BufferedReader(new InputStreamReader(btInputStream));
      btState.setText("Bluetooth Opened");
    }
  }

  public void closeBT(View view) throws IOException {
    if (view.getId() == R.id.buttonCloseBT) {
      try {

        Packet stopPacket = new Packet(Packet.Type.Stop, "");
        sendPacket(stopPacket);
        sendBTStopMsg();
        while (stateType != State.StopRepRecvd) {
          try {
            Thread.sleep(1000);
            Log.d("KK",
                "Waiting for Stop Reply to close the BT connection. State="
                + stateType.toString());
          } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            Log.d("KK", "Exception in Sleep");
            e.printStackTrace();
          }
        }

        if (btOutputStream != null) {
          btOutputStream.close();
        }
        if (btInputStream != null) {
        btInputStream.close();
        }
        if (btSocket != null) {
          btSocket.close();
        }
      } catch (Exception e) {
        Log.d("closeBT", "Exception");
        socketFlag = false;
      }
      btState.setText("Bluetooth Closed");

      String alertMsg = "Bluetooth connection closed successfully";
      String alertTitle = "Info";
      new AlertDialog.Builder(StartTestMsg.this).setTitle(alertTitle)
        .setMessage(alertMsg).setNeutralButton("OK", new OkOnClickListener())
        .show();
    }
  }
  
  public void sendConfigPacket() {
    Packet configPacket = new Packet(Packet.Type.Config, "");
    sendPacket(configPacket);
    sendBTConfigMsg();
  }

  public void sendStartPacket() {
    Packet startPacket = new Packet(Packet.Type.Start, "");
    sendPacket(startPacket);
    sendBTStartMsg();
  }


  public void sendPacket(Packet packetToWrite) {
    byte[] buf = new byte[packetToWrite.getBuffer().length];
    
    buf = packetToWrite.getBuffer();
    /*Log.d("Packet","Prior to adding esc byte : " );

    String beforeStuffing = "";
    for (int i = 0; i < buf.length; i++)
    {
      beforeStuffing += String.format("%02X ", buf[i]);
    }
    
    Log.d("Packet", beforeStuffing);
    Log.d("Packet","\n" );*/
    
    List<Byte> payLoad = new ArrayList<Byte>();
    payLoad.add(buf[0]); //Adding SOF
    for (int i = 1; i < buf.length - 1; i++)  // Last index will have EOF
    {
        if (buf[i] == 0x7D || buf[i] == 0x12 || buf[i] == 0x13)  // Check if ESC byte is present
        {
            payLoad.add((byte)0x7D);
            byte xor = 0x20;
            payLoad.add((byte)(buf[i] ^ xor));
        }
        else
        {
            payLoad.add(buf[i]);
        }
    }

    payLoad.add(buf[buf.length - 1]); // Adding EOF
    sendbuffer = new byte[payLoad.size()];
    for ( int i =0; i<payLoad.size(); i++) {
      sendbuffer[i] = payLoad.get(i);
    }

    String afterStuffing = "";
    for (int i = 0; i < sendbuffer.length; i++)
    {
        if (i == 0)
        Log.d("Packet", " SOF:" + String.format("%02X ", sendbuffer[i]));
      else if (i == (sendbuffer.length - 1)) {
        Log.d("Packet", afterStuffing);
        Log.d("Packet", " EOF:" + String.format("%02X ", sendbuffer[i]));
      }
        else
        afterStuffing += String.format("%02X ", sendbuffer[i]);
    }
}

  public void sendBTConfigMsg() {

    int _readBufferPosition = 0;
    byte[] readBuffer = new byte[1024];

      try {
        if (btOutputStream != null || btInputStream != null) {
          Log.d("Packet", "In Sending Msg routine");
          btOutputStream.write(sendbuffer);
          btOutputStream.flush();
          msgState.setText("Data Sent ");
          Log.d("Packet",
              "Data sent" + toHexString(sendbuffer, 0, sendbuffer.length));
          stateType = State.ConfigSent;

          Thread.sleep(3);
        }
      } catch (IOException e) {
        e.printStackTrace();
        Log.d("Packet", "Exception");
      } catch (InterruptedException e) {
        Log.d("Packet", "Exception2");
        e.printStackTrace();
      }
  }

  public void sendBTStartMsg() {
    int _readBufferPosition = 0;
    byte[] readBuffer = new byte[1024];
      try {
        if (btOutputStream != null || btInputStream != null) {
          Log.d("Packet", "In Sending Msg routine");
          btOutputStream.write(sendbuffer);
          btOutputStream.flush();
          msgState.setText("Data Sent ");
          Log.d("Packet",
              "Data sent" + toHexString(sendbuffer, 0, sendbuffer.length));
        stateType = State.StartSent;

          Thread.sleep(3);
      }
      } catch (Exception e) {
        Log.d("Packet", "Exception in sending start msg");
      }
  }

  public void sendBTStopMsg() {
    int _readBufferPosition = 0;
    byte[] readBuffer = new byte[1024];
    try {
      if (btOutputStream != null || btInputStream != null) {
        Log.d("Packet", "In Sending Msg routine");
        btOutputStream.write(sendbuffer);
        btOutputStream.flush();
        msgState.setText("Data Sent ");
        Log.d("Packet",
            "Data sent" + toHexString(sendbuffer, 0, sendbuffer.length));
        stateType = State.StopSent;

        Thread.sleep(3);

      }
    } catch (Exception e) {
      Log.d("Packet", "Exception in sending stop msg");
    }
  }

  void beginListenForData() {
    _stopWorker = false;
    _readBufferPosition = 0;
    _readBuffer = new byte[1024];
    final String TAG = "ReadThread";

    _workerThread = new Thread(new Runnable() {
      public void run() {
        long time = System.currentTimeMillis();
        long millisPassed = 2000;
        boolean escFlag = false;
        while (!Thread.currentThread().isInterrupted() && !_stopWorker) {
          try {
            int bytesAvailable = btInputStream.available();
            int expectedPacketLength = 0;
            if (bytesAvailable > 0) {
              byte[] packetBytes = new byte[bytesAvailable];
              btInputStream.read(packetBytes);
              for (int i = 0; i < bytesAvailable; i++) {
                byte b = packetBytes[i];
                if (b == Packet.SOF) {
                  _readBufferPosition = 0;
                } else if (b == Packet.EOF) {
                  Log.d("Receipt", "Message Starts here...");
                  byte[] encodedBytes = new byte[_readBufferPosition];
                  System.arraycopy(_readBuffer, 0, encodedBytes, 0,
                      encodedBytes.length);
                  Log.d(
                      "Receipt",
                      "Hex: "
                          + toHexString(encodedBytes, 0, encodedBytes.length));
                  _readBufferPosition = 0;
                  if (stateType == State.ConfigSent) {
                    if (_readBuffer[0] == (byte) 0x84) {
                      Log.d("Packet", "Config Reply Received as expected");
                      stateType = State.ConfigRepRecvd;
                    } else if (_readBuffer[0] == (byte) 0x83) {
                      Log.d("Packet", "Error Packet Received");
                    } else {
                      Log.d("Packet",
                          "Config Reply not received as expected !!!");
                    }
                  } else if (stateType == State.StartSent) {
                    if (_readBuffer[0] == (byte) 0x80) {
                      Log.d("Packet", "Start Reply Received as expected");
                      stateType = State.StartRepRecvd;
                    } else if (_readBuffer[0] == (byte) 0x83) {
                      Log.d("Packet", "Error Packet Received");
                    } else {
                      Log.d("Packet",
                          "Start Reply not received as expected !!!");
                    }
                  } else if (stateType == State.StartRepRecvd) {
                    if (_readBuffer[0] == (byte) 0x82) {
                      Log.d("Packet", "Receiving Report as expected");
                      stateType = State.RecvReport;
                    } else if (_readBuffer[0] == (byte) 0x83) {
                      Log.d("Packet", "Error Packet Received");
                    }
                  }
                  if (stateType == State.StopSent) {
                    if (_readBuffer[0] == (byte) 0x81) {
                      Log.d("Packet", "Stop Reply Received as expected");
                      stateType = State.StopRepRecvd;
                    } else if (_readBuffer[0] == (byte) 0x83) {
                      Log.d("Packet", "Error Packet Received");
                    } else {
                      Log.d("Packet", "Stop Reply not received as expected !!!");
                    }
                  }
                } else {
                  if (_readBufferPosition == 0) {
                    /*if (b == (byte) 0x83) { // error packet
                      expectedPacketLength = 4; // length of error packet
                    }*/
                  }
                  if (b == 0x7D) {// ESC byte xor the next byte
                    escFlag = true;
                  } else {
                    if (escFlag) {
                      byte xor = 0x20;
                      b = (byte) (b ^ xor);
                    }
                    escFlag = false;
                  }
                  if (!escFlag) {
                    _readBuffer[_readBufferPosition++] = b;
                  }
                }
              }
            } else {
              if (System.currentTimeMillis() > time + millisPassed) {
                Log.d(TAG,
                    "No bytes to read... GUI State:" + stateType.toString());
                time = System.currentTimeMillis();
              }
            }
          } catch (IOException ex) {
            _stopWorker = true;
          }
        }
      }
    });

    _workerThread.start();
  }

  private final class OkOnClickListener implements
      DialogInterface.OnClickListener {
    public void onClick(DialogInterface dialog, int which) {

      Intent intent = new Intent(StartTestMsg.this, HomeScreen.class);
      startActivity(intent);
    }
  }

  private final static char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5', '6',
      '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

  public static String toHexString(byte[] array, int offset, int length) {
    char[] buf = new char[length * 2];

    int bufIndex = 0;
    for (int i = offset; i < offset + length; i++) {
      byte b = array[i];
      buf[bufIndex++] = HEX_DIGITS[(b >>> 4) & 0x0F];
      buf[bufIndex++] = HEX_DIGITS[b & 0x0F];
    }

    return new String(buf);
  }

}
