package de.schulz.problemmelder;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

/**
 * Klasse mit Canvas Methoden zum Zeichnen des Fadenkreuzes in MapOsm
 */

public class DrawOnTop extends View {
	
	Paint paint = new Paint();
	int h;
	int w;

	
    public DrawOnTop(Context context, int Height, int Width) {
            super(context);
;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.RED);
            paint.setStrokeWidth(4);
            w=Height;
            h=Width;
    }

    protected void onDraw(Canvas canvas) {

    	// horizontale Linie
    	
            canvas.drawLine(h/2-15 , w/2, h/2+15, w/2, paint);
            canvas.drawLine(h/2, w/2-15, h/2, w/2+15, paint);
            invalidate();
    }


 
	
} 