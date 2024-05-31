package pt.ulisboa.tecnico.cmov.pharmacist.utils;

import com.google.android.gms.maps.model.LatLng;

import java.text.MessageFormat;
import java.util.Base64;

public class ChunkUtils {

    public static double precisionRound(double a, int dec) {
        return (double) Math.round(a * dec) / dec;
    }

    public static String getChunkId(LatLng coord) {
        return getChunkId(coord.latitude, coord.longitude);
    }

    public static String getChunkId(double lat, double lng) {
        return Base64.getEncoder().encodeToString(MessageFormat.format("{0}{1}", precisionRound(lat, 100), precisionRound(lng, 100)).getBytes());
    }
}
