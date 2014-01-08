package edu.umn.gophermagsensing;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class Settings extends Activity {

  private int wheatstoneAmp, wheatstoneFreq;
  private int coilAmp, coilFreq, coilDCoffset;
  private int measurementPeriod, digitalGain, analogGain;

  private int dwheatstoneAmp, dwheatstoneFreq;
  private int dcoilAmp, dcoilFreq, dcoilDCoffset;
  private int dmeasurementPeriod, ddigitalGain, danalogGain;

  EditText wsAmp, wsFreq, cAmp, cFreq, cDCoffset, mPeriod, dGain, aGain;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.settings);

    // setting default values
    dwheatstoneAmp = 300;
    dwheatstoneFreq = 1000;
    dcoilAmp = 500;
    dcoilFreq = 50;
    dcoilDCoffset = 0;
    dmeasurementPeriod = 1;
    ddigitalGain = 1;
    danalogGain = 20;

    wheatstoneAmp = 0;
    wheatstoneFreq = 0;
    coilAmp = 0;
    coilFreq = 0;
    coilDCoffset = 0;
    measurementPeriod = 0;
    digitalGain = 0;
    analogGain = 0;

    this.wsAmp = (EditText) findViewById(R.id.editTextWheatstoneAmp);
    this.wsFreq = (EditText) findViewById(R.id.editTextWheatstoneFreq);
    this.cAmp = (EditText) findViewById(R.id.editCoilAmp);
    this.cFreq = (EditText) findViewById(R.id.editTextCoilFreq);
    this.cDCoffset = (EditText) findViewById(R.id.editCoilDCOffset);
    this.mPeriod = (EditText) findViewById(R.id.editTextMeasurementPeriod);
    this.dGain = (EditText) findViewById(R.id.editDigitalGain);
    this.aGain = (EditText) findViewById(R.id.editAnalogGain);

    this.wsAmp.setText(Integer.toString(this.dwheatstoneAmp));
    this.wsFreq.setText(Integer.toString(this.dwheatstoneFreq));
    this.cAmp.setText(Integer.toString(this.dcoilAmp));
    this.cFreq.setText(Integer.toString(this.dcoilFreq));
    this.cDCoffset.setText(Integer.toString(this.dcoilDCoffset));
    this.mPeriod.setText(Integer.toString(this.dmeasurementPeriod));
    this.dGain.setText(Integer.toString(this.ddigitalGain));
    this.aGain.setText(Integer.toString(this.danalogGain));

    // hideAll();

  }

  public void cancelSettings(View view) {
    if (view.getId() == R.id.buttonCancel) {
      Intent intent = new Intent(this, HomeScreen.class);
      startActivity(intent);
    }
  }

  public void clearSettings(View view) {
    if (view.getId() == R.id.buttonClear) {
      this.wsAmp.setText("");
      this.wsFreq.setText("");
      this.cAmp.setText("");
      this.cFreq.setText("");
      this.cDCoffset.setText("");
      this.mPeriod.setText("");
      this.dGain.setText("");
      this.aGain.setText("");

      // Intent intent = new Intent(this, HomeScreen.class);
      // startActivity(intent);
    }
  }

  public void setDefaultSettings(View view) {
    if (view.getId() == R.id.buttonDefault) {
      this.wsAmp.setText(Integer.toString(this.dwheatstoneAmp));
      this.wsFreq.setText(Integer.toString(this.dwheatstoneFreq));
      this.cAmp.setText(Integer.toString(this.dcoilAmp));
      this.cFreq.setText(Integer.toString(this.dcoilFreq));
      this.cDCoffset.setText(Integer.toString(this.dcoilDCoffset));
      this.mPeriod.setText(Integer.toString(this.dmeasurementPeriod));
      this.dGain.setText(Integer.toString(this.ddigitalGain));
      this.aGain.setText(Integer.toString(this.danalogGain));

      // Intent intent = new Intent(this, HomeScreen.class);
      // startActivity(intent);
    }
  }

  public void doneSettings(View view) {
    if (view.getId() == R.id.buttonDone) {
      // TODO send the values back to the HomeScreen

      Intent intent = new Intent(this, HomeScreen.class);
      startActivity(intent);
    }
  }

}
