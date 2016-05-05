package org.xxh.flabbybird;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;

/**
 * Created by xiexinhong on 16/4/29.
 */
public class Bird {

    //鸟儿占屏幕的高度
    private final static float RADIO_POS_HEIGHT = 2 / 3f;

    //鸟儿的大小
    private final static int BIRD_SIZE = 30;

    //鸟儿的坐标
    private int mBirdX;

    //鸟儿的纵坐标
    private int mBirdY;

    //鸟儿的宽度
    private int mBirdWidth;

    //鸟儿的高度
    private int mBirdHeiht;

    //鸟儿图片
    private Bitmap mBirdBitmap;

    private RectF mBirdRecF = new RectF();


    public Bird(Context context,int gameWidth,int gameHeight,Bitmap birdBitmap) {
        mBirdBitmap = birdBitmap;
        mBirdX = gameWidth / 2 - mBirdBitmap.getWidth()/2;
        mBirdY = (int) (gameHeight * RADIO_POS_HEIGHT);

        mBirdWidth = Util.dp2px(context,BIRD_SIZE);
        mBirdHeiht = mBirdWidth / (mBirdBitmap.getWidth()/mBirdBitmap.getHeight());
    }

    public void draw(Canvas canvas) {
        mBirdRecF.set(mBirdX,mBirdY,mBirdX+mBirdWidth,mBirdY+mBirdHeiht);
        canvas.drawBitmap(mBirdBitmap,null,mBirdRecF,null);
    }

    public int getBirdY() {
        return mBirdY;
    }

    public void setBirdY(int y) {
       mBirdY = y;
    }

    public int getBirdX() {
        return mBirdX;
    }

    public void setBirdX(int x) {
        this.mBirdX = x;
    }

    public int getWidth() {
        return mBirdWidth;
    }

    public int getHeight() {
        return mBirdHeiht;
    }

}
