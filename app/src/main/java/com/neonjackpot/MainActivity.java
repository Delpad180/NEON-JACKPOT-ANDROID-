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
        final ToneGenerator audio = new ToneGenerator(AudioManager.STREAM_MUSIC, 72);
        final String[] symbols = {"7", "♦", "★", "🍒", "BAR", "♛"};
        final int[] reels = {0,1,2,3,4};
        final int[] result = new int[5];
        final float[] sparkX = new float[55], sparkY = new float[55], sparkV = new float[55];
        int credits = 1000, bet = 10, lastWin = 0, tick = 0, flash = 0;
        boolean spinning = false, soundOn = true, jackpot = false;
        String status = "¡PREPARADO PARA JUGAR!";
        String sub = "Consigue combinaciones y gana créditos";
        final int cyan = Color.rgb(0,235,255), pink = Color.rgb(255,45,190);
        final int gold = Color.rgb(255,205,75);

        SlotView() {
            super(MainActivity.this);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            for(int i=0;i<sparkX.length;i++) resetSpark(i);
        }
        void resetSpark(int i) {
            sparkX[i]=rng.nextInt(400); sparkY[i]=rng.nextInt(800);
            sparkV[i]=1+rng.nextFloat()*3;
        }
        void tone(int type, int duration) {
            if(soundOn) try { audio.startTone(type,duration); } catch(Exception ignored) {}
        }
        void box(Canvas c,float l,float t,float r,float b,float rad,int color,boolean stroke,int sw) {
            p.setShader(null); p.setColor(color); p.setStyle(stroke?Paint.Style.STROKE:Paint.Style.FILL);
            p.setStrokeWidth(sw); p.clearShadowLayer();
            if(stroke) p.setShadowLayer(12,0,0,color);
            c.drawRoundRect(new RectF(l,t,r,b),rad,rad,p);
            p.setStyle(Paint.Style.FILL); p.clearShadowLayer();
        }
        void txt(Canvas c,String s,float x,float y,float size,int color,boolean bold) {
            p.setShader(null); p.setColor(color); p.setTextSize(size);
            p.setTypeface(Typeface.create(bold?"sans-serif-black":"sans-serif",bold?1:0));
            p.setTextAlign(Paint.Align.CENTER); p.setStyle(Paint.Style.FILL);
            p.setShadowLayer(bold?10:0,0,0,bold?color:Color.TRANSPARENT);
            c.drawText(s,x,y,p); p.clearShadowLayer();
        }
        @Override protected void onDraw(Canvas c) {
            super.onDraw(c);
            c.save(); c.scale(getWidth()/400f,getHeight()/800f);
            int pulse=(int)(25+20*Math.sin(System.currentTimeMillis()/180.0));
            p.setShader(new LinearGradient(0,0,400,800,new int[]{0xff160b35,0xff070814,0xff17051f},null,Shader.TileMode.CLAMP));
            c.drawRect(0,0,400,800,p); p.setShader(null);
            for(int y=0;y<800;y+=40){p.setColor(0x223b2c65);c.drawLine(0,y,400,y,p);}
            for(int i=0;i<sparkX.length;i++){
                p.setColor((i%3==0?0x88ff45d4:0x6671faff));
                c.drawCircle(sparkX[i],sparkY[i],i%4==0?2.2f:1.2f,p);
                if(spinning||jackpot){sparkY[i]+=sparkV[i];if(sparkY[i]>800)resetSpark(i);}
            }
            txt(c,"NEON",200,72,48,cyan,true); txt(c,"JACKPOT",200,116,39,pink,true);
            txt(c,"S L O T   M A C H I N E",200,145,11,Color.LTGRAY,false);
            box(c,22,169,378,238,18,0xff17172f,false,1);
            box(c,22,169,378,238,18,cyan,true,2);
            txt(c,"CRÉDITOS",87,197,12,Color.LTGRAY,true);
            txt(c,String.format("%,d",credits),87,226,26,gold,true);
            txt(c,"ÚLTIMO PREMIO",290,197,11,Color.LTGRAY,true);
            txt(c,String.valueOf(lastWin),290,226,24,cyan,true);
            txt(c,soundOn?"♫ SONIDO ON":"♫ SONIDO OFF",330,185,9,soundOn?cyan:Color.GRAY,true);

            box(c,15,258,385,515,22,0xff260d42,false,1);
            box(c,15,258,385,515,22,pink,true,2);
            txt(c,"★  LÍNEA DE PREMIO  ★",200,283,12,gold,true);
            for(int i=0;i<5;i++){
                float x=27+i*70;
                box(c,x,298,x+64,465,10,0xff050713,false,1);
                box(c,x,298,x+64,465,10,spinning?cyan:(jackpot?gold:cyan),true,spinning?2:1);
                int col=reels[i]==0?gold:reels[i]==1?pink:reels[i]==2?cyan:Color.WHITE;
                txt(c,symbols[reels[i]],x+32,397,reels[i]==4?19:34,col,true);
            }
            box(c,27,477,373,527,12,0xff171329,false,1);
            txt(c,status,200,498,13,jackpot?gold:gold,true);
            txt(c,sub,200,516,10,Color.LTGRAY,false);
            if(jackpot) {
                p.setColor(0x55ffd34f); c.drawRect(20,250,380,530,p);
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
            if(spinning||jackpot){postInvalidateDelayed(45);}
        }
        @Override public boolean onTouchEvent(MotionEvent e) {
            if(e.getAction()==MotionEvent.ACTION_UP){
                float x=e.getX()*400/getWidth(), y=e.getY()*800/getHeight();
                if(y>169&&y<238&&x>245){soundOn=!soundOn;if(soundOn)tone(ToneGenerator.TONE_PROP_ACK,100);invalidate();return true;}
                if(y>674&&y<755) spin();
                else if(y>570&&y<640&&!spinning){
                    if(x<155){bet=Math.max(5,bet-5);tone(ToneGenerator.TONE_PROP_BEEP,55);}
                    if(x>245){bet=Math.min(100,bet+5);tone(ToneGenerator.TONE_PROP_BEEP,55);}
                    invalidate();
                } else if(y>755&&credits<100){credits+=500;sub="Bono de cortesía: +500 créditos";tone(ToneGenerator.TONE_PROP_ACK,160);invalidate();}
                performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                return true;
            }
            return true;
        }
        void spin(){
            if(spinning)return;
            if(credits<bet){status="CRÉDITOS INSUFICIENTES";sub="Toca la zona inferior para recibir un bono";tone(ToneGenerator.TONE_PROP_NACK,180);invalidate();return;}
            credits-=bet;lastWin=0;spinning=true;jackpot=false;tick=0;
            status="¡MUCHA SUERTE!";sub="Los rodillos están girando...";
            tone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD,120);
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            for(int i=0;i<5;i++)result[i]=rng.nextInt(symbols.length);
            animateReels();
        }
        void animateReels(){
            h.postDelayed(()->{
                if(!spinning)return;
                tick++;
                for(int i=0;i<5;i++)if(tick<8+i*3)reels[i]=rng.nextInt(symbols.length);
                if(tick%2==0)tone(ToneGenerator.TONE_PROP_BEEP,35);
                if(tick>=22)finish();else{invalidate();animateReels();}
            },90);
            invalidate();
        }
        void finish(){
            for(int i=0;i<5;i++)reels[i]=result[i];
            spinning=false;
            int count=1;while(count<5&&reels[count]==reels[0])count++;
            int prize=0;
            if(count>=3){int mult=reels[0]==0?20:reels[0]==1?12:reels[0]==2?8:5;prize=bet*mult*(count==3?1:count==4?2:4);}
            if(prize==0){int[] n=new int[symbols.length];for(int v:reels)n[v]++;for(int v:n)if(v>=3)prize=Math.max(prize,bet*3);}
            lastWin=prize;credits+=prize;
            jackpot=prize>=bet*20;
            status=prize>0?"¡PREMIO! +"+prize+" CRÉDITOS":"SIN PREMIO ESTA VEZ";
            sub=jackpot?"¡COMBINACIÓN EXTRAORDINARIA!":prize>0?"¡Buena combinación!":"Prueba otra combinación";
            if(prize>0){tone(jackpot?ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD:ToneGenerator.TONE_PROP_ACK,jackpot?650:280);performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);}
            else tone(ToneGenerator.TONE_PROP_NACK,180);
            invalidate();
            if(jackpot)h.postDelayed(()->{jackpot=false;invalidate();},2600);
        }
        @Override protected void onDetachedFromWindow(){super.onDetachedFromWindow();h.removeCallbacksAndMessages(null);audio.release();}
    }
}
