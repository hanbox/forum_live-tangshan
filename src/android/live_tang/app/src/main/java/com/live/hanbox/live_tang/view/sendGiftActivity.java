package com.live.hanbox.live_tang.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.PopupWindow;
import android.widget.ViewFlipper;

import com.live.hanbox.live_tang.R;

public class sendGiftActivity extends PopupWindow {

    private View mMenuView;
    ViewFlipper viewFlipper = null;
    float startX;

    public sendGiftActivity(Activity context, View.OnClickListener itemsOnClick) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.activity_send_gift, null);

        viewFlipper = (ViewFlipper) mMenuView.findViewById(R.id.viewFlipper);

        //设置SelectPicPopupWindow的View
        this.setContentView(mMenuView);
        //设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(ActionBar.LayoutParams.FILL_PARENT);
        //设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(ActionBar.LayoutParams.WRAP_CONTENT);
        //设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        //设置SelectPicPopupWindow弹出窗体动画效果
        this.setAnimationStyle(android.R.style.Animation_Toast);
        //实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        //设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);
        //mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
        mMenuView.setOnTouchListener(new OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                int height = mMenuView.findViewById(R.id.pop_layout).getTop();
                int y=(int) event.getY();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        break;
                    case MotionEvent.ACTION_UP:
                        if(y<height){
                            dismiss();
                        }
                        if (event.getX() > startX) { // 向右滑动
                            viewFlipper.setInAnimation(mMenuView.getContext(), R.anim.in_leftright);
                            viewFlipper.setOutAnimation(mMenuView.getContext(), R.anim.out_leftright);
                            viewFlipper.showNext();
                        } else if (event.getX() < startX) { // 向左滑动
                            viewFlipper.setInAnimation(mMenuView.getContext(), R.anim.in_rightleft);
                            viewFlipper.setOutAnimation(mMenuView.getContext(), R.anim.out_rightleft);
                            viewFlipper.showPrevious();
                        }
                        break;
                }

                return true;
            }
        });
    }

    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                break;
            case MotionEvent.ACTION_UP:

                if (event.getX() > startX) { // 向右滑动
                    viewFlipper.setInAnimation(mMenuView.getContext(), R.anim.in_leftright);
                    viewFlipper.setOutAnimation(mMenuView.getContext(), R.anim.out_leftright);
                    viewFlipper.showNext();
                } else if (event.getX() < startX) { // 向左滑动
                    viewFlipper.setInAnimation(mMenuView.getContext(), R.anim.in_rightleft);
                    viewFlipper.setOutAnimation(mMenuView.getContext(), R.anim.out_rightleft);
                    viewFlipper.showPrevious();
                }
                break;
        }

        return this.onTouchEvent(event);
    }

}
