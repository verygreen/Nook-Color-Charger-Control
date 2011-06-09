package com.linuxhacker.nookchargercontrol;

import android.widget.TextView;
import android.widget.ToggleButton;
import android.view.View;
import android.view.View.OnClickListener;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import android.app.Activity;
import android.os.Bundle;

public class NookChargerControl extends Activity implements OnClickListener {
	private TextView ChargerStatus;
	private ToggleButton ChargerForcePower;
	private TextView ModeStatus;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        ChargerForcePower = (ToggleButton) findViewById(R.id.ForceHighCharge);
        ChargerStatus = (TextView) findViewById(R.id.ChargerStatus);
        ModeStatus  = (TextView) findViewById(R.id.modeStatus);
        ChargerStatus.setOnClickListener(this);

        ModeStatus.setText("");
    }
    
    public void onResume() {
    	super.onResume();
    	
    	if (ChargerStatus == null)
    		return;
    	
    	ChargerStatus.setText(readChargerStatus());
    	ChargerForcePower.setChecked(isChargerForcedHigh());
    }
    
    public void onClick(View view) {
    	ChargerStatus.setText(readChargerStatus());
    }

    public void forceHighPowerClick(View view) {
    	String mode;

    	if (ChargerForcePower.isChecked()) {
    		mode = "high";
    	} else {
    		mode = "low";
    	}

    	Process p;
    	try {
    		p = Runtime.getRuntime().exec("/system/xbin/su");

    		DataOutputStream dos = new DataOutputStream(p.getOutputStream());

    		dos.writeBytes("echo " + mode + "> /sys/devices/platform/max8903_charger/mode\n");
    		dos.writeBytes("exit");
    		dos.flush();
    		dos.close();

    		if (p.waitFor() != 0) {
    			ModeStatus.setText("Error updating forced mode - exec");
    		}
    	} catch (IOException e) {
    		ModeStatus.setText("Error updating forced mode - write");
    	} catch (InterruptedException e) {
    		ModeStatus.setText("Error updating forced mode - interrupt");
    	}

    	ChargerStatus.setText(readChargerStatus());
    	ChargerForcePower.setChecked(isChargerForcedHigh());
    }

    private String readChargerStatus() {
    	try {
		InputStream instream = new FileInputStream("/sys/devices/platform/max8903_charger/status");
		
		InputStreamReader inputreader = new InputStreamReader(instream);
		BufferedReader buffreader = new BufferedReader(inputreader);
		String line;
		String text = "";
		
		while ((line = buffreader.readLine()) != null) {
			text += line + "\n";
		}
		instream.close();
		
		return text;
		
	} catch (java.io.FileNotFoundException e) {
		return "Error finding charger status";
	} catch (java.io.IOException e) {
		return "Error reading charger status";
	}
}
	
	private String readChargerMode() {
		try {
			InputStream instream = new FileInputStream("/sys/devices/platform/max8903_charger/mode");
			
			InputStreamReader inputreader = new InputStreamReader(instream);
			BufferedReader buffreader = new BufferedReader(inputreader);
			String line;
			
			line = buffreader.readLine();
			instream.close();
			
			return line;
			
		} catch (java.io.FileNotFoundException e) {
			return "Error finding charger mode";
		} catch (java.io.IOException e) {
			return "Error reading charger mode";
		}
	}
		
	private boolean isChargerForcedHigh() {
			return readChargerMode().startsWith("force high");
		}
}