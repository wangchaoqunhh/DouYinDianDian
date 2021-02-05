package com.wcq.douyindiandian.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.wcq.douyindiandian.R;

public class SuspendButton extends LinearLayout {
    private Context mContext;
    private int lastX;
    private int lastY;
    public Button button;
//    public TextView tvStart;
//    private ImageView iv2;
//    private ImageView iv3;
//    private ImageView iv4;

    private int visibility;

    public SuspendButton(Context context) {
        super(context);
        initView(context);

    }

    public SuspendButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);

    }

    public SuspendButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);

    }

    public SuspendButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void showOrHide(int visibility) {
//        tvStart.setVisibility(visibility);
//        iv2.setVisibility(visibility);
//        iv3.setVisibility(visibility);
//        iv4.setVisibility(visibility);
    }

    public void initView(Context context) {
        this.mContext = context;
        View view = LayoutInflater.from(context).inflate(R.layout.view_suspend_but, this);
        button = findViewById(R.id.button);
//        tvStart = view.findViewById(R.id.tv_start);
//        iv2 = view.findViewById(R.id.iv_2);
//        iv3 = view.findViewById(R.id.iv_3);
//        iv4 = view.findViewById(R.id.iv_4);
        visibility = View.INVISIBLE;
        showOrHide(visibility);

//        button.setOnClickListener(v -> {
//            if (v.getId() == R.id.button) {
//                if (visibility == View.INVISIBLE) {
//                    visibility = View.VISIBLE;
//                } else if (visibility == View.VISIBLE) {
//                    visibility = View.INVISIBLE;
//                }
//                showOrHide(visibility);
//            }
//        });

    }



    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_MOVE:
                return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        super.dispatchTouchEvent(ev);
        return true;
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        int x = (int) event.getX();
//        int y = (int) event.getY();
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                lastX = x;
//                lastY = y;
//                break;
//            case MotionEvent.ACTION_MOVE:
//                int offsetX = x - lastX;
//                int offsetY = y - lastY;
//                layout(getLeft() + offsetX,
//                        getTop() + offsetY,
//                        getRight() + offsetX,
//                        getBottom() + offsetY);
//                break;
//        }
//        return true;
//    }
}
