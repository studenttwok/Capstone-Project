package net.hklight.dmrmarc;


import com.google.android.gms.maps.model.LatLng;

// Class port from https://github.com/b4d/maidenhead
public class Maidenhead {
    public static LatLng fromMaidenhead(String maidenhead) {
        double lng = 0;
        double lat = 0;

        lng = (((int)maidenhead.charAt(0) - (int)'A') * 20) - 180;
        lat = (((int)maidenhead.charAt(1) - (int)'A') * 10) - 90;
        lng += ((int)maidenhead.charAt(2) - (int)'0') * 2;
        lat += ((int)maidenhead.charAt(3) - (int)'0') * 1;

        if (maidenhead.length() >= 5) {
            lng += ((int)maidenhead.charAt(4) - (int)'a') * 0.0833333333;
            lat += ((int)maidenhead.charAt(5) - (int)'a') * 0.0416666667;

            lng += 0.0416666667;
            lat += 0.0208333333;

        } else {
            lng += 1.0;
            lat += 0.5;
        }


        return new LatLng(lat, lng);
    }
}
