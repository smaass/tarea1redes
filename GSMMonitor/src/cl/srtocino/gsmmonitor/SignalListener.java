package cl.srtocino.gsmmonitor;

import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;

public class SignalListener extends PhoneStateListener {
    private int signalStrengthValue;

    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        super.onSignalStrengthsChanged(signalStrength);
        if (signalStrength.isGsm()) {
            if (signalStrength.getGsmSignalStrength() != 99)
                signalStrengthValue = signalStrength.getGsmSignalStrength() * 2 - 113;
            else
                signalStrengthValue = signalStrength.getGsmSignalStrength();
        } else {
            signalStrengthValue = signalStrength.getCdmaDbm();
        }
    }

    public String getSignalStrength() {
    	if (signalStrengthValue == 99) {
    		return "unknown";
    	}
    	else {
    		return signalStrengthValue+"";
    	}
    }
}