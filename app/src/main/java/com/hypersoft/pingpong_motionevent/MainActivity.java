package com.hypersoft.pingpong_motionevent;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Random;


public class MainActivity extends AppCompatActivity {

    private Button paddle,ball;
    private int screenWidth,screenHeight;
    private float x,xMove;

    private int xVelocity;
    private int yVelocity;
    private int initialSpeed = 2;
    private CharSequence text = "Izgubio si od Androida :)";
    private int duration = Toast.LENGTH_SHORT;
    private Toast toast;

    private static String TAG = MainActivity.class.getSimpleName();
    Random random;

    final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE // da sakrije navigatio bar za stalno
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        paddle = (Button) findViewById(R.id.paddle);
        ball = (Button) findViewById(R.id.ball);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;//širina ekrana u pikselima
        screenHeight = displayMetrics.heightPixels;// visina ekrana u pikselima
        toast = Toast.makeText(this,text,duration);

        getWindow().getDecorView().setSystemUiVisibility(flags);// da sakrije navigatio bar za stalno



        View.OnTouchListener handleTouch = new View.OnTouchListener(){
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent){
                Log.d(TAG,"Stisnuo:"+motionEvent.getAction());
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {   //kad stisneš dugme ide desno ili levo padle
                    Log.d(TAG, "onTouch: Initial X " + paddle.getX());
                    Log.d(TAG, "onTouch: Initial Y" + paddle.getY());
                    x = motionEvent.getX();                             // uzima koordinatu kad stisnes
                }else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {  //kad pistiš digme stane kretanje padle

                } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                    xMove = motionEvent.getX() - x;         // pravi difer. razliku (dF) pomeraj - kad stisnes
                    paddle.setX(paddle.getX() + xMove);     // tu razliku dodaješ na trenutnu poziciju paddle
                }
                return false;
            }
        };
        paddle.setOnTouchListener(handleTouch);
        direction_ball();
        ballMoving();
    }
    private void direction_ball(){
        random = new Random();
        int randomXDirection = random.nextInt(2);//-1 kreće se gore a 1 kreće se dole
        if (randomXDirection==0)
            randomXDirection--;
        setXDirection(randomXDirection*initialSpeed);

        int randomYDirection = random.nextInt(2);
        if(randomYDirection==0)
            randomYDirection--;
        setYDirection(randomYDirection*initialSpeed);

    }
    synchronized private void setXDirection(int randomXDirection){
        xVelocity = randomXDirection;
        notify();
    }
    synchronized private int getXDirection(){return xVelocity;}
    synchronized private void setYDirection(int randomYDirection){
        yVelocity = randomYDirection;
        notify();
    }
    synchronized private int getYDirection(){return yVelocity;}

    synchronized private void move(){
        ball.setX(ball.getX()+getXDirection());
        ball.setY(ball.getY()+getYDirection());
        notify();
    }
    private void ballMoving(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(!Thread.interrupted()){
                    try {
                        Thread.sleep(3);
                        synchronized (this){while(ball == null) wait();}
                        move();
                        if (ball.getY() <= 0)
                            setYDirection(-getYDirection());
                        synchronized (this){while(paddle == null) wait();}
                        // dodir padle i lopte
                        if (((ball.getY()+60) >= paddle.getY()) && (((ball.getX()>= paddle.getX())&&(ball.getX()<= (paddle.getX()+150)) || (((ball.getX()+50)<= (paddle.getX()+150))&&((ball.getX()+50)>= paddle.getX())))))
                            setYDirection(-getYDirection());
                        //izgubio, resetuj
                        if (ball.getY() >= screenHeight-50){
                            toast.show();
                            setYDirection(-getYDirection());
                        }


                        if (ball.getX() <= 0)
                            setXDirection(-getXDirection());
                        if (ball.getX() >= (screenWidth-60))
                            setXDirection(-getXDirection());
                    }catch (InterruptedException e) {
                        Log.e(TAG, "Uncaught exception,loptica:", e);// mora ovako ili izaziva prekid(e.printStackTrace();)
                    }
                }
            }
        }).start();
    }
}