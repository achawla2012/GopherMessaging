package edu.umn.gophermagsensing;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class SendEmail extends Activity {

  /** Called when the activity is first created. */
  String fileAbsolutePath = null;
  @Override
  public void onCreate(Bundle savedInstanceState) {
    fileAbsolutePath = null;
    super.onCreate(savedInstanceState);
    setContentView(R.layout.sendemail);

  }

  public void pickFile(View view) {
    if (view.getId() == R.id.buttonSelectFile) {

      FileChooserDialog dialog = new FileChooserDialog(this);
      dialog.show();

      dialog.addListener(new FileChooserDialog.OnFileSelectedListener() {
        public void onFileSelected(Dialog source, File file) {
          source.hide();

          EditText fileTosend =
 (EditText) findViewById(R.id.selectFileText);
          fileTosend.setText(file.getName());
          fileAbsolutePath = file.getAbsolutePath();
        }

        public void onFileSelected(Dialog source, File folder, String name) {
          source.hide();
          Toast toast =
              Toast.makeText(source.getContext(),
                  "File created: " + folder.getName() + "/" + name,
                  Toast.LENGTH_LONG);
          toast.show();
        }
      });
    }
  }

  public void doneEmail(View view) {
    if (view.getId() == R.id.buttonEmailDone) {
      String alertMsg = "E-mail Sent to the Doctor";
      String alertTitle = "Email Alert";
      new AlertDialog.Builder(this).setTitle(alertTitle).setMessage(alertMsg)
          .setNeutralButton("OK", null).show();

      Intent intent = new Intent(this, HomeScreen.class);
      startActivity(intent);
    }
  }

  public void sendEmail(View view) {
    if (view.getId() == R.id.buttonSendEmailFinal) {
      Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
      emailIntent.setType("text/html");
      emailIntent.putExtra(android.content.Intent.EXTRA_TITLE,
          "Early Stage Cancer Detector Results");
      emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
          "Early Stage Cancer Detector Results");
      emailIntent.putExtra(Intent.EXTRA_STREAM,
          Uri.parse("file://" + fileAbsolutePath));
      // emailIntent.putExtra(Intent.EXTRA_EMAIL, //can be used if required
      // new String[] { "<abc>@gmail.com" });


      // Obtain reference to (hard-coded) String and pass it to Intent
      emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
 "Hello");
      startActivity(emailIntent);



      // Intent intent = new Intent(this, HomeScreen.class);
      // startActivity(intent);
    }
  }

}

