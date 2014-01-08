package edu.umn.gophermagsensing;

import android.util.Log;

public class Packet
{
    public enum Type
    {
        Config,
 Start, Stop
    };

  public static final byte SOF = (byte) 0x12;
  public static final byte EOF = (byte) 0x13;

    private byte _command;
    private byte[] _payload;
    private byte _payloadLength;
    private byte _xor;
    private byte[] _buffer;
    public byte[] getBuffer() { return _buffer; };

  public Packet(Type type, String hexBytes) // signature should be modified,
                                            // hexBytes not required
    {
        if ( type == Type.Config )
            _command = (byte)0x04;
        else if ( type == Type.Start )
            _command = (byte)0x00;

    if (type == Type.Config) {
      _payload = generateConfigPayload();
    } else if (type == Type.Start) {
      _payload = generateStartPayload();
    } else if (type == Type.Stop) {
      //_payload = generateStopPayload();
      _payloadLength = (byte) 0;
    }

    if(type != Type.Stop) {
        _payloadLength = (byte)_payload.length;
    }
        computeXor();
        computeBuffer();
    // printBuffer();
    }

    private void computeXor()
    {
        _xor = (byte)0x00;
        _xor ^= _command;
        _xor ^= _payloadLength;
        for ( int i = 0; i < _payloadLength; i++ )
            _xor ^= _payload[i];
    }

    private void computeBuffer()
    {
    _buffer = new byte[5 + _payloadLength]; // SOF, EOF, command, payloadlength,
                                            // checksum
        _buffer[0] = SOF;
        _buffer[1] = _command;
        _buffer[2] = _payloadLength;
    if (_command != (byte) 0x01) {
      System.arraycopy(_payload, 0, _buffer, 3, _payloadLength);
    }
        _buffer[3 + _payloadLength] = _xor;
    _buffer[4 + _payloadLength] = EOF;
    }

    private byte[] hexStringToByteArray(String s)
    {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

  private byte[] generateConfigPayload() // TODO update new values
    {
    byte[] p = new byte[29];
    p[0] = (byte) 46;
    p[1] = (byte) 47;
    p[2] = (byte) 45;
    p[3] = (byte) 16;
    p[4] = (byte) 44;
    p[5] = (byte) 17;
    p[6] = (byte) 43;
    p[7] = (byte) 18;
    p[8] = (byte) 42;
    p[9] = (byte) 19;
    p[10] = (byte) 41;
    p[11] = (byte) 20;
    p[12] = (byte) 40;
    p[13] = (byte) 21;
    p[14] = (byte) 39;
    p[15] = (byte) 38;
    p[16] = (byte) 22;
    p[17] = (byte) 37;
    p[18] = (byte) 23;
    p[19] = (byte) 36;
    p[20] = (byte) 24;
    p[21] = (byte) 35;
    p[22] = (byte) 25;
    p[23] = (byte) 34;
    p[24] = (byte) 26;
    p[25] = (byte) 33;
    p[26] = (byte) 27;
    p[27] = (byte) 32;
    p[28] = (byte) 28;
    return p;
  }

  private byte[] generateStartPayload() {
    byte[] payload = new byte[25];
    float[] data = new float[6]; // Equivalent to 20 bytes = 5*4
    // setting default values

    float multiplier = 1f;
    // data[0] = multiplier * 300 / 1000;
    data[0] = 300f / 1000f;
    data[1] = 1000f;
    data[2] = 500f / 1000f;
    data[3] = 50f;
    data[4] = 1f;
    data[5] = 20f; /*Added if packet length is 25*/



    for (int i = 0; i < data.length; i++) {
      int floatBits = Float.floatToIntBits(data[i]); // int int bits
      // converting into Little Endian
      payload[4 * i + 0] = (byte) (floatBits);
      payload[4 * i + 1] = (byte) (floatBits >> 8);
      payload[4 * i + 2] = (byte) (floatBits >> 16);
      payload[4 * i + 3] = (byte) (floatBits >> 24);

    }
    payload[24] = (byte) 0x01;

    return payload;
    }

  private byte[] generateStopPayload() {
    return null;
  }

  private void printBuffer() {
    for (int i = 0; i < _buffer.length; i++) {
      if (i == 0)
        Log.d("PacketClass",
            Integer.toString(i) + " SOF:" + String.format("%02X ", _buffer[i]));
      else if (i == (_buffer.length - 1))
        Log.d("PacketClass",
            Integer.toString(i) + " EOF:" + String.format("%02X ", _buffer[i]));
      else
        Log.d("PacketClass",
            Integer.toString(i) + String.format("%02X ", _buffer[i]));
    }
  }
}