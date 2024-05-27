package pt.ulisboa.tecnico.cmov.pharmacist.ui.fragments.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import pt.ulisboa.tecnico.cmov.pharmacist.R;

public class PharmacyMarker {

    private static Bitmap defaultIconBitmap;
    private static Bitmap activeIconBitmap;
    private static Bitmap favIconBitmap;

    private static Bitmap favActiveIconBitmap;

    public static MarkerOptions createNew(Context context, LatLng position) {
        if (defaultIconBitmap == null) {
            Bitmap defaultBitmap = getBitmap(context, R.drawable.local_pharmacy_map);
            Bitmap activeBitmap = getBitmap(context, R.drawable.local_pharmacy_map_active);
            Bitmap favBitmap = getBitmap(context, R.drawable.local_pharmacy_map_fav);
            Bitmap favActiveBitmap = getBitmap(context, R.drawable.local_pharmacy_map_fav_active);

            defaultIconBitmap = addShadow(defaultBitmap, defaultBitmap.getHeight() + 20, defaultBitmap.getWidth() + 20, Color.GRAY, 5, 5, 5);
            activeIconBitmap = addShadow(activeBitmap, activeBitmap.getHeight() + 20, activeBitmap.getWidth() + 20, Color.GRAY, 5, 5, 5);
            favIconBitmap = addShadow(favBitmap, activeBitmap.getHeight() + 20, activeBitmap.getWidth() + 20, Color.GRAY, 5, 5, 5);
            favActiveIconBitmap = addShadow(favActiveBitmap, activeBitmap.getHeight() + 20, activeBitmap.getWidth() + 20, Color.GRAY, 5, 5, 5);
        }

        return new MarkerOptions().position(position).icon(BitmapDescriptorFactory.fromBitmap(defaultIconBitmap));
    }

    public static void setProps(Marker marker, boolean isActive, boolean isFavorite) {
        if (marker == null) return;

        if (isFavorite) {
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(isActive ? favActiveIconBitmap : favIconBitmap));
        } else {
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(isActive ? activeIconBitmap : defaultIconBitmap));
        }
    }

    private static Bitmap addShadow(final Bitmap bm, final int dstHeight, final int dstWidth, int color, int size, float dx, float dy) {
        final Bitmap mask = Bitmap.createBitmap(dstWidth, dstHeight, Bitmap.Config.ALPHA_8);

        final Matrix scaleToFit = new Matrix();
        final RectF src = new RectF(0, 0, bm.getWidth(), bm.getHeight());
        final RectF dst = new RectF(0, 0, dstWidth - dx, dstHeight - dy);
        scaleToFit.setRectToRect(src, dst, Matrix.ScaleToFit.CENTER);

        final Matrix dropShadow = new Matrix(scaleToFit);
        dropShadow.postTranslate(dx, dy);

        final Canvas maskCanvas = new Canvas(mask);
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        maskCanvas.drawBitmap(bm, scaleToFit, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
        maskCanvas.drawBitmap(bm, dropShadow, paint);

        final BlurMaskFilter filter = new BlurMaskFilter(size, BlurMaskFilter.Blur.OUTER);
        paint.reset();
        paint.setAntiAlias(true);
        paint.setColor(color);
        paint.setMaskFilter(filter);
        paint.setFilterBitmap(true);

        final Bitmap ret = Bitmap.createBitmap(dstWidth, dstHeight, Bitmap.Config.ARGB_8888);
        final Canvas retCanvas = new Canvas(ret);
        retCanvas.drawBitmap(mask, 0,  0, paint);
        retCanvas.drawBitmap(bm, scaleToFit, null);
        mask.recycle();
        return ret;
    }

    private static Bitmap getBitmap(Context context, int drawableRes) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableRes);
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}
