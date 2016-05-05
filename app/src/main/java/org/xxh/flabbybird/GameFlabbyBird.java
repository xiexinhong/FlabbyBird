package org.xxh.flabbybird;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiexinhong on 16/4/29.
 */
public class GameFlabbyBird extends SurfaceView implements SurfaceHolder.Callback , Runnable {

    private static final String TAG = "GameFlabbyBird";

    private SurfaceHolder mHolder;
    private Canvas mCanvas;
    //用户控制绘制的线程
    private Thread mThread;
    //线程控制开关
    private boolean mIsRuning;

    //绘制背景相关的
    private int mWidth;
    private int mHeight;
    private RectF mGamePanelRect = new RectF();
    private Bitmap mBg;

    //绘制鸟儿相关
    private Bird mBird;
    private Bitmap mBirdBitmap;

    //绘制地板
    private Paint mPaint;
    private Floor mFloor;
    private Bitmap mFloorBg;
    private int mSpeed;

    /***********管道相关***********************/
    private Bitmap mPipeTop;
    private Bitmap mPipeBottom;
    private RectF mPipeRect;
    private int mPipeWidth;
    //管道的宽度 60dp
    private static final int PIPE_WIDTH = 60;
    private List<Pipe> mPipes = new ArrayList<>();

    /***********分数相关***********************/
    private int mGrade = 100;
    private final int[] mNums = new int[] { R.drawable.n0,
            R.drawable.n1,
            R.drawable.n2,
            R.drawable.n3,
            R.drawable.n4,
            R.drawable.n5,
            R.drawable.n6,
            R.drawable.n7,
            R.drawable.n8,
            R.drawable.n9 };
    private Bitmap[] mNumBitmap;
    //单个数字的高度的1/15
    private static final float RADIO_SINGLE_NUM_HEIGHT = 1 / 15f;
    //单个数字的宽度
    private int mSingleGradeWidth;
    //单个数字的高度
    private int mSingleGradeHeight;
    //单个数字的范围
    private RectF mSingleNumRectF;

    /****************************************/
    //触摸上升的距离。
    private static final int TOUCH_UP_SIZE = -16;
    //触摸上升的高度
    private final int BIRD_UP_DIS_SIZE = Util.dp2px(getContext(),TOUCH_UP_SIZE);
    //自由下落速度
    private final int AUTO_DOWN_SPEED = Util.dp2px(getContext(), 2);
    //游戏状态
    private GameStatus mGameStaus = GameStatus.RUNNING;
    private int mTmpBirdDis;
    //将上升的距离转化为px；这里多存储一个变量，变量在run中计算
    private final int mBirdUpDis = Util.dp2px(getContext(), TOUCH_UP_SIZE);

    //两个管道间距离
    private final int PIPE_DIS_BETWEEN_TWO = Util.dp2px(getContext(), 100);
    //记录移动的距离，达到 PIPE_DIS_BETWEEN_TWO 则生成一个管道
    private int mTmpMoveDistance;
    //记录需要移除的管道
    private List<Pipe> mNeedRemovePipe = new ArrayList<>();

    //鸟自动下落的距离
    private final int mAutoDownSpeed = Util.dp2px(getContext(), 2);

    /**构造方法组*/
    public GameFlabbyBird(Context context) {
        this(context,null);
    }

    public GameFlabbyBird(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHolder = getHolder();
        mHolder.addCallback(this);
        //设置画布背景透明
        setZOrderOnTop(true);
        mHolder.setFormat(PixelFormat.TRANSLUCENT);
        //设置可以获得焦点
        setFocusable(true);
        setFocusableInTouchMode(true);
        //设置常亮
        setKeepScreenOn(true);

        //初始化画笔
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);

        initBitMap();

        mSpeed = Util.dp2px(getContext(), 2);

        mPipeWidth = Util.dp2px(getContext(),PIPE_WIDTH);
    }

    private void initBitMap() {
        mBg = BitmapFactory.decodeResource(getResources(),R.drawable.bg_default_game);
        mBirdBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.ic_bird);
        mFloorBg = BitmapFactory.decodeResource(getResources(),R.drawable.bg_floor_two);
        mPipeTop = BitmapFactory.decodeResource(getResources(),R.drawable.img_top_pipe);
        mPipeBottom = BitmapFactory.decodeResource(getResources(), R.drawable.img_bottom_pipe);
        //加载分数bitmap
        mNumBitmap = new Bitmap[mNums.length];
        for (int i = 0; i < mNumBitmap.length; i++) {
            mNumBitmap[i] = BitmapFactory.decodeResource(getResources(),mNums[i]);
        }

    }

    public GameFlabbyBird(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public GameFlabbyBird(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        mGamePanelRect.set(0, 0, w, h);
        mBird = new Bird(getContext(),mWidth,mHeight,mBirdBitmap);
        mFloor = new Floor(mWidth, mHeight, mFloorBg);
        // 初始化管道范围
        mPipeRect = new RectF(0, 0, mPipeWidth, mHeight);
        Pipe pipe = new Pipe(w, h, mPipeTop, mPipeBottom);
        mPipes.add(pipe);
        // 初始化分数
        mSingleGradeHeight = (int) (h * RADIO_SINGLE_NUM_HEIGHT);
        mSingleGradeWidth = (int) (mSingleGradeHeight * 1.0f/ mNumBitmap[0].getHeight() * mNumBitmap[0].getWidth());
        mSingleNumRectF = new RectF(0, 0, mSingleGradeWidth,mSingleGradeHeight);
    }

    /**
     * 处理一些逻辑上的计算
     */
    private void gameLogic() {

        switch (mGameStaus){

            case RUNNING: {
                //管道逻辑
                gamePipeLogic();
                // 更新我们地板绘制的x坐标，地板移动
                mFloor.setFloorX(mFloor.getFloorX() - mSpeed);

                //默认下落，点击时瞬间上升
                mTmpBirdDis += mAutoDownSpeed;
                mBird.setBirdY(mBird.getBirdY() + mTmpBirdDis);
                break;
            }
            case OVER: {
                // 鸟落下
                if (mBird.getBirdY() < mFloor.getFloorY() - mBird.getWidth()) {
                    mTmpBirdDis += mAutoDownSpeed;
                    mBird.setBirdY(mBird.getBirdY() + mTmpBirdDis);
                } else {
                    mGameStaus = GameStatus.WAITING;
                    initPos();
                }
                break;
            }
            default:
                break;
        }

    }

    private void gamePipeLogic() {
        // 管道移动 以及计算即将被移除的管道
        for (Pipe pipe : mPipes) {
            if (pipe.getPipeX() < -mPipeWidth) {
                mNeedRemovePipe.add(pipe);
                continue;
            }
            pipe.setPipeX(pipe.getPipeX() - mSpeed);
        }
        //移除管道
        mPipes.removeAll(mNeedRemovePipe);
        // 管道
        mTmpMoveDistance += mSpeed;
        // 生成一个管道
        Log.i(TAG,"mTmpMoveDistance = "+mTmpMoveDistance+"mTmpMoveDistance >= PIPE_DIS_BETWEEN_TWO = "+(mTmpMoveDistance >= PIPE_DIS_BETWEEN_TWO));
        if (mTmpMoveDistance >= PIPE_DIS_BETWEEN_TWO) {
            Pipe pipe = new Pipe(getWidth(), getHeight(),mPipeTop, mPipeBottom);
            mPipes.add(pipe);
            mTmpMoveDistance = 0;
        }
    }

    private void checkGameOver()
    {

        // 如果触碰地板，gg
        if (mBird.getBirdY() > mFloor.getFloorX() - mBird.getHeight()) {
            mGameStaus = GameStatus.OVER;
        }

        // 如果撞到管道
        for (Pipe wall : mPipes) {

            //已经穿过的
            if (wall.getPipeX() + mPipeWidth < mBird.getBirdY()) {
                continue;
            }
            if (wall.touchBird(mBird)) {
                mGameStaus = GameStatus.OVER;
                break;
            }
        }
    }

    /**
     * 重置鸟的位置等数据
     */
    private void initPos()
    {
        mPipes.clear();
        mNeedRemovePipe.clear();
        //重置鸟的位置
        mBird.setBirdY(mHeight * 2 / 3);
        //重置下落速度
        mTmpBirdDis = 0;                                                                                                                                          mTmpMoveDistance = 0 ;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            switch (mGameStaus) {
                case WAITING:
                    mGameStaus = GameStatus.RUNNING;
                    break;
                case RUNNING:
                    mTmpBirdDis = mBirdUpDis;
                    break;
            }

        }
        return true;
    }

    /**callback接口start*/
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //开启线程
        mIsRuning = true;
        mThread = new Thread(this);
        mThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mIsRuning = false;
    }
    /**callback接口end*/


    /**Runnable接口start*/
    @Override
    public void run() {
        while(mIsRuning) {
            long start = System.currentTimeMillis();
            gameLogic();
            draw();
            long end = System.currentTimeMillis();
            try {
                //保证每次绘制的消耗时间都是0.05秒
                if(end - start < 50) {
                    Thread.sleep(50 - (end - start));
                }
            } catch (InterruptedException e){
                    Log.i(TAG, "e = " + e.getMessage());
            }
        }
    }
    /**Runnable接口end*/

    private void draw() {
        try {
            mCanvas = mHolder.lockCanvas();
            if(mCanvas != null) {
                // TODO: 16/4/29 draw something;
                drawBg();
                drawBird();
                drawFloor();
                // 更新我们地板绘制的x坐标
                mFloor.setFloorX(mFloor.getFloorX() - mSpeed);
                //绘制管道
                drawPipes();
                drawGrades();
            }
        } catch (Exception e) {
            Log.i(TAG,"e = "+e.getMessage());
        } finally {
             if(mCanvas != null) {
                 mHolder.unlockCanvasAndPost(mCanvas);
             }
        }
    }

    /**
     * 绘制背景
     */
    private void drawBg() {
        mCanvas.drawBitmap(mBg, null,mGamePanelRect, null);
    }

    /**
     * 绘制鸟儿
     */
    private void drawBird() {
        if(mBird != null) {
            mBird.draw(mCanvas);
        }
    }

    /**
     * 绘制地板
     */
    private void drawFloor() {
        mFloor.draw(mCanvas, mPaint);
    }

    /**
     * 绘制地板
     */
    private void drawPipes() {
        for (Pipe pipe : mPipes) {
            pipe.setPipeX(pipe.getPipeX() - mSpeed);
            pipe.draw(mCanvas, mPipeRect);
        }
    }

    /**
     * 绘制分数
     */
    private void drawGrades()
    {
        String grade = String.valueOf(mGrade);
        mCanvas.save(Canvas.MATRIX_SAVE_FLAG);
        mCanvas.translate(mWidth / 2 - grade.length() * mSingleGradeWidth / 2,1f / 8 * mHeight);
        // draw single num one by one
        for (int i = 0; i < grade.length(); i++) {
            String numStr = grade.substring(i, i + 1);
            int num = Integer.valueOf(numStr);
            mCanvas.drawBitmap(mNumBitmap[num], null, mSingleNumRectF, null);
            mCanvas.translate(mSingleGradeWidth, 0);
        }
        mCanvas.restore();
    }
}
