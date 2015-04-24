package com.energynews.app.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.energynews.app.R;

public class HelperActivity extends Activity {

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, HelperActivity.class);
        context.startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_helper);
        ///overridePendingTransition(R.anim.bounce, R.anim.slide_up);
        setFakeBoldText(R.id.title_name);
        setFakeBoldText(R.id.title_developers);
        setFakeBoldText(R.id.title_introduction);
        setFakeBoldText(R.id.title_instructions);
        setFakeBoldText(R.id.title_statement);
        setFakeBoldText(R.id.title_email);
    }

    private void setFakeBoldText(int id) {
        TextView tv = (TextView)findViewById(id);
        TextPaint tp = tv.getPaint();
        tp.setFakeBoldText(true);
    }

    float xdown, ydown;
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            xdown = event.getX();
            ydown = event.getY();
        } else if (action == MotionEvent.ACTION_UP) {
            xdown = event.getX() - xdown;
            ydown = event.getY() - ydown;
            if (Math.abs(xdown) + Math.abs(ydown) > 5 && Math.abs(xdown) > Math.abs(ydown)) {
                finish();//左右滑动关闭窗口
                return false;
            }
        }
        return super.dispatchTouchEvent(event);
    }
}
