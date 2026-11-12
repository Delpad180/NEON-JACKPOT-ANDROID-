package com.neonjackpot;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.HapticFeedbackConstants;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import java.util.Random;

public class MainActivity extends Activity {
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().getDecorView().setSystemUiVisibility(5894 | 1024 | 512);
        setContentView(new SlotView());
    }

    final class SlotView extends View {
        final Paint p = new Paint(3);
        final Handler h = new Handler(Looper.getMainLooper());
        final Random rng = new Random();
        final ToneGenerator audio = new ToneGenerator(AudioManager.STREAM_MUSIC, 82);
        final String[] fruits = {"🍒","🍋","🍊","🍉","🍇","🍓","🍍","🥝"};
        final int[] reels = {0,1,2,3,4};
        final int[] result = new int[5];
        final int[] travelCells = new int[5];
        final boolean[] reelStopped = new boolean[5];
        final float[] sparkX = new float[85], sparkY = new float[85], sparkV = new float[85];
        final float[] sparkSize = new float[85];
        int credits=1000, bet=10, lastWin=0;
        long spinStart=0, lastFrame=0;
        boolean spinning=false, soundOn=true, jackpot=false;
        String status="¡PREPARADO PARA JUGAR!";
        String sub="Consigue combinaciones y gana créditos";
        final int cyan=Color.rgb(0,235,255), pink=Color.rgb(255,45,190), gold=Color.rgb(255,205,75);

        SlotView() {
            super(MainActivity.this);
            setLayerType(View.LAYER_TYPE_SOFTWARE,null);
            for(int i=0;i<sparkX.length;i++) resetSpark(i);
        }
        void resetSpark(int i) {
            sparkX[i]=rng.nextInt(400); sparkY[i]=rng.nextInt(800);
            sparkV[i]=1+rng.nextFloat()*3.5f; sparkSize[i]=1+rng.nextFloat()*2.5f;
        }
        void tone(int type,int duration) {
            if(soundOn) try { audio.startTone(type,duration); } catch(Exception ignored) {}
        }
        void box(Canvas c,float l,float t,float r,float b,float rad,int color,boolean stroke,int sw) {
            p.setShader(null); p.setColor(color); p.setStyle(stroke?Paint.Style.STROKE:Paint.Style.FILL);
            p.setStrokeWidth(sw); p.clearShadowLayer();
            if(stroke) p.setShadowLayer(14,0,0,color);
            c.drawRoundRect(new RectF(l,t,r,b),rad,rad,p);
            p.setStyle(Paint.Style.FILL); p.clearShadowLayer();
        }
        void txt(Canvas c,String s,float x,float y,float size,int color,boolean bold) {
            p.setShader(null); p.setColor(color); p.setTextSize(size);
            p.setTypeface(Typeface.create(bold?"sans-serif-black":"sans-serif",bold?1:0));
            p.setTextAlign(Paint.Align.CENTER); p.setStyle(Paint.Style.FILL);
            p.setShadowLayer(bold?9:0,0,0,bold?color:Color.TRANSPARENT);
            c.drawText(s,x,y,p); p.clearShadowLayer();
        }
        float reelTravel(int i,long now) {
            long local=now-spinStart-i*260L;
            if(local<=0)return 0;
            float t=Math.min(1f,local/1650f);
            float eased=1f-(float)Math.pow(1f-t,3);
            return travelCells[i]*72f*eased;
        }
        void drawReel(Canvas c,int i,float x,long now) {
            float left=x, top=298, right=x+64, bottom=465;
            box(c,left,top,right,bottom,10,0xff050713,false,1);
            c.save();
            c.clipRect(left+2,top+2,right-2,bottom-2);
            if(!spinning) {
                txt(c,fruits[reels[i]],x+32,397,39,Color.WHITE,true);
            } else {
                float travel=reelTravel(i,now);
                int steps=(int)Math.floor(travel/72f);
                float frac=(travel/72f)-steps;
                int current=(reels[i]+steps)%fruits.length;
                float center=382-frac*72f;
                txt(c,fruits[(current+fruits.length-1)%fruits.length],x+32,center-72,38,0xff8b8b9e,true);
                txt(c,fruits[current],x+32,center,42,Color.WHITE,true);
                txt(c,fruits[(current+1)%fruits.length],x+32,center+72,38,0xff8b8b9e,true);
            }
            c.restore();
            box(c,left,top,right,bottom,10,spinning?cyan:(jackpot?gold:cyan),true,spinning?2:1);
            p.setShader(null); p.setColor(0x22ffffff); p.setStrokeWidth(1);
            c.drawLine(left+6,top+5,right-6,top+5,p);
        }
        @Override protected void onDraw(Canvas c) {
            super.onDraw(c);
            long now=System.currentTimeMillis();
            c.save(); c.scale(getWidth()/400f,getHeight()/800f);
            float pulse=(float)(0.5+0.5*Math.sin(now/190.0));
            p.setShader(new LinearGradient(0,0,400,800,new int[]{0xff160b35,0xff070814,0xff17051f},null,Shader.TileMode.CLAMP));
            c.drawRect(0,0,400,800,p); p.setShader(null);
            for(int y=0;y<800;y+=40){p.setColor(0x223b2c65);c.drawLine(0,y,400,y,p);}
            for(int x=0;x<400;x+=40){p.setColor(0x113b2c65);c.drawLine(x,0,x,800,p);}
            for(int i=0;i<sparkX.length;i++){
                p.setColor(i%3==0?0x99ff45d4:0x8871faff);
                c.drawCircle(sparkX[i],sparkY[i],sparkSize[i],p);
                if(spinning||jackpot){sparkY[i]-=sparkV[i];if(sparkY[i]<0) {sparkY[i]=800;sparkX[i]=rng.nextInt(400);}}
            }
            txt(c,"NEON",200,72,48,cyan,true); txt(c,"JACKPOT",200,116,39,pink,true);
            txt(c,"F R U I T   S L O T",200,145,11,Color.LTGRAY,false);
            box(c,22,169,378,238,18,0xff17172f,false,1);
            box(c,22,169,378,238,18,cyan,true,2);
            txt(c,"CRÉDITOS",87,197,12,Color.LTGRAY,true);
            txt(c,String.format("%,d",credits),87,226,26,gold,true);
            txt(c,"ÚLTIMO PREMIO",290,197,11,Color.LTGRAY,true);
            txt(c,String.valueOf(lastWin),290,226,24,cyan,true);
            txt(c,soundOn?"♫ SONIDO ON":"♫ SONIDO OFF",330,185,9,soundOn?cyan:Color.GRAY,true);

            box(c,15,258,385,515,22,0xff260d42,false,1);
            box(c,15,258,385,515,22,pink,true,2);
            txt(c,"✦  LÍNEA DE PREMIO  ✦",200,283,12,gold,true);
            for(int i=0;i<5;i++) drawReel(c,i,27+i*70,now);
            box(c,27,477,373,527,12,0xff171329,false,1);
            txt(c,status,200,498,13,gold,true);
            txt(c,sub,200,516,10,Color.LTGRAY,false);
            if(jackpot) {
                p.setColor((int)(0x33ffffff+(pulse*0x22ffffff))); c.drawRect(20,250,380,530,p);
                for(int i=0;i<18;i++){p.setColor(i%2==0?gold:pink);c.drawCircle((i*47+13)%390,270+(i*61)%260,2.5f+ pulse*2,p);}
                txt(c,"✦  ¡MEGA PREMIO!  ✦",200,548,15,gold,true);
            }
            txt(c,"APUESTA",200,559,12,Color.LTGRAY,true);
            box(c,70,572,330,635,15,0xff17172f,false,1);
            box(c,70,572,330,635,15,pink,true,1);
            txt(c,"−",108,615,32,pink,true); txt(c,String.valueOf(bet),200,613,25,Color.WHITE,true);
            txt(c,"+",292,615,30,cyan,true); txt(c,"MÍN. 5  •  MÁX. 100",200,653,10,Color.GRAY,false);
            box(c,55,674,345,750,22,0xff50106c,false,1);
            box(c,55,674,345,750,22,spinning?gold:cyan,true,3);
            txt(c,spinning?"GIRANDO...":"G I R A R",200,721,27,spinning?gold:cyan,true);
            txt(c,"CRÉDITOS VIRTUALES · SIN DINERO REAL",200,780,9,Color.GRAY,false);
            c.restore();
            if(spinning||jackpot)postInvalidateDelayed(16);
        }
        @Override public boolean onTouchEvent(MotionEvent e) {
            if(e.getAction()==MotionEvent.ACTION_UP) {
                float x=e.getX()*400/getWidth(), y=e.getY()*800/getHeight();
                if(y>169&&y<238&&x>245){soundOn=!soundOn;if(soundOn)tone(ToneGenerator.TONE_PROP_ACK,120);invalidate();return true;}
                if(y>674&&y<755)spin();
                else if(y>570&&y<640&&!spinning){
                    if(x<155){bet=Math.max(5,bet-5);tone(ToneGenerator.TONE_PROP_BEEP,65);}
                    if(x>245){bet=Math.min(100,bet+5);tone(ToneGenerator.TONE_PROP_BEEP,65);}
                    invalidate();
                } else if(y>755&&credits<100){credits+=500;sub="Bono de cortesía: +500 créditos";tone(ToneGenerator.TONE_PROP_ACK,220);invalidate();}
                performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                return true;
            }
            return true;
        }
        void spin() {
            if(spinning)return;
            if(credits<bet){status="CRÉDITOS INSUFICIENTES";sub="Toca la zona inferior para recibir un bono";tone(ToneGenerator.TONE_PROP_NACK,220);invalidate();return;}
            credits-=bet;lastWin=0;spinning=true;jackpot=false;spinStart=System.currentTimeMillis();
            status="¡MUCHA SUERTE!";sub="Los rodillos giran y se detienen uno a uno";
            tone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD,180);
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            for(int i=0;i<5;i++){
                result[i]=rng.nextInt(fruits.length);
                travelCells[i]=18+((result[i]-reels[i]-18)%fruits.length+fruits.length)%fruits.length;
                reelStopped[i]=false;
            }
            animateReels();
        }
        void animateReels() {
            h.postDelayed(()->{
                if(!spinning)return;
                long now=System.currentTimeMillis();
                boolean allStopped=true;
                for(int i=0;i<5;i++){
                    long local=now-spinStart-i*260L;
                    if(local>=1650&&!reelStopped[i]){
                        reelStopped[i]=true;reels[i]=result[i];
                        tone(ToneGenerator.TONE_PROP_BEEP,95);
                        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                    }
                    if(!reelStopped[i])allStopped=false;
                }
                invalidate();
                if(allStopped)finish();else animateReels();
            },16);
        }
        void finish() {
            spinning=false;
            int count=1;while(count<5&&reels[count]==reels[0])count++;
            int prize=0;
            if(count>=3){int mult=reels[0]==0?20:reels[0]==1?14:reels[0]==2?10:6;prize=bet*mult*(count==3?1:count==4?2:4);}
            if(prize==0){int[] n=new int[fruits.length];for(int v:reels)n[v]++;for(int v:n)if(v>=3)prize=Math.max(prize,bet*3);}
            lastWin=prize;credits+=prize;jackpot=prize>=bet*20;
            status=prize>0?"¡PREMIO! +"+prize+" CRÉDITOS":"SIN PREMIO ESTA VEZ";
            sub=jackpot?"¡COMBINACIÓN EXTRAORDINARIA!":prize>0?"¡Buena combinación!":"Prueba otra combinación";
            if(prize>0){tone(jackpot?ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD:ToneGenerator.TONE_PROP_ACK,jackpot?900:400);performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);}
            else tone(ToneGenerator.TONE_PROP_NACK,240);
            invalidate();
            if(jackpot)h.postDelayed(()->{jackpot=false;invalidate();},3000);
        }
        @Override protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();h.removeCallbacksAndMessages(null);audio.release();
        }
    }
}
