package org.xxh.flabbybird;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;

import java.util.Random;

/**
 * Created by xiexinhong on 16/5/3.
 */
public class Pipe {


    //上下管道间的距离
    private static final float RADIO_BETWEEN_UP_DOWN = 1 / 5F;

    //上管道的最大高度
    private static final float RADIO_MAX_HEIGHT = 2 / 5F;

    // 上管道的最小高度
    private static final float RADIO_MIN_HEIGHT = 1 / 5F;

    //管道横坐标
    private int mPipeX;

    //上管道的高度
    private int mHeight;

    //上下管道间距
    private int mMargin;

    //上管道图片
    private Bitmap mTopBitmap;

    private Bitmap mBottomBitmap;

    private static Random mRandom = new Random();


    public Pipe(int gameWidth,int gameHeight,Bitmap topBitmap,Bitmap bottomBitmap) {
        mMargin = (int)(gameHeight * RADIO_BETWEEN_UP_DOWN);
        mPipeX = gameWidth; //从最左边出现。
        mTopBitmap = topBitmap;
        mBottomBitmap = bottomBitmap;
        randomHeihgt(gameHeight);
    }

    private void randomHeihgt(int gameHeight) {
        mHeight = mRandom.nextInt((int) (gameHeight * (RADIO_MAX_HEIGHT - RADIO_MIN_HEIGHT)));
        mHeight = (int) (mHeight + gameHeight * RADIO_MIN_HEIGHT);
    }

    public void draw(Canvas canvas,RectF rect) {

        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        // rect为整个管道，假设完整管道为100，需要绘制20，则向上偏移80
        canvas.translate(mPipeX, -(rect.bottom - mHeight));
        canvas.drawBitmap(mTopBitmap, null, rect, null);
        // 下管道，便宜量为，上管道高度+margin
        canvas.translate(0, (rect.bottom - mHeight) + mHeight + mMargin);
        canvas.drawBitmap(mBottomBitmap, null, rect, null);
        canvas.restore();
    }

    public int getPipeX() {
        return this.mPipeX;
    }

    public void setPipeX(int x) {
        this.mPipeX = x;
    }

    /**
     * 判断和鸟是否触碰
     * @param mBird
     * @return
     */
    public boolean touchBird(Bird mBird)
    {
        //如果bird已经触碰到管道
        if (mBird.getBirdX() + mBird.getWidth() > mPipeX
                && (mBird.getBirdY() < mHeight || mBird.getBirdY() + mBird.getHeight() > (mHeight + mMargin))) {
            return true;
        }

        return false;
    }


}
