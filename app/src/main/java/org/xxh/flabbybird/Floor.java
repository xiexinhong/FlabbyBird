package org.xxh.flabbybird;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;

/**
 * Created by xiexinhong on 16/4/29.
 */
public class Floor {

    //地板Y位置的百分比
    private static final float FLOOR_Y_POS_RADIO = 4 / 5F;

    private int mFloorX;

    private int mFloorY;

    private int mFloorWidth;

    private int mFloorHeight;

    private BitmapShader mFloorBitmapShader;

    public Floor(int gameWidth,int gameHeight,Bitmap floorBitmap) {
        mFloorWidth = gameWidth;
        mFloorHeight = gameHeight;
        mFloorY  = (int) (gameHeight * FLOOR_Y_POS_RADIO);
        mFloorBitmapShader = new BitmapShader(floorBitmap, Shader.TileMode.REPEAT,Shader.TileMode.CLAMP);

    }

    public void draw(Canvas canvas,Paint paint) {
        if(-mFloorX > mFloorWidth) {
            mFloorX = mFloorX % mFloorWidth;
        }
        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        //移动到指定的位置
        canvas.translate(mFloorX,mFloorY);
        paint.setShader(mFloorBitmapShader);
        canvas.drawRect(mFloorX, 0, -mFloorX+mFloorWidth,mFloorHeight - mFloorY, paint);
        canvas.restore();
        paint.setShader(null);
    }

    public int getFloorX() {
        return mFloorX;
    }

    public void setFloorX(int floorX) {
        this.mFloorX = floorX;
    }

    public int getFloorY() {
        return mFloorY;
    }

    public void setFloorY(int y) {
        this.mFloorY = y;
    }
}
