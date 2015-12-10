package cn.dreamtobe.kpswitch.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import cn.dreamtobe.kpswitch.util.KeyboardUtil;


/**
 * Copyright 2015 Jacks Blog(blog.dreamtobe.cn).
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p/>
 * Created by Jacksgong on 15/6/29.
 * <p/>
 * Detail: http://blog.dreamtobe.cn/2015/09/01/keyboard-panel-switch/
 *
 * @see PanelLayout
 */
public class CustomRootLayout extends LinearLayout implements ViewTreeObserver.OnGlobalLayoutListener{

    private final static String TAG = "JFrame.CustomRootLayout";

    public CustomRootLayout(Context context) {
        super(context);
        init();
    }

    public CustomRootLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomRootLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CustomRootLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private final static String STATUS_BAR_DEFPACKAGE = "android";
    private final static String STATUS_BAR_DEFTYPE = "dimen";
    private final static String STATUS_BAR_NAME = "status_bar_height";

    private void init() {
        getViewTreeObserver().addOnGlobalLayoutListener(this);

        if (!mAlreadyGetStatusBarHeight) {
            int resourceId = getResources().getIdentifier(STATUS_BAR_NAME, STATUS_BAR_DEFTYPE, STATUS_BAR_DEFPACKAGE);
            if (resourceId > 0) {
                mStatusBarHeight = getResources().getDimensionPixelSize(resourceId);
                mAlreadyGetStatusBarHeight = true;
                Log.d(TAG, String.format("Get status bar height %d", mStatusBarHeight));
            }
        }

    }

    private int mStatusBarHeight = 50;

    private boolean mAlreadyGetStatusBarHeight = false;



    private int mOldHeight = -1;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        // 由当前布局被键盘挤压，获知，由于键盘的活动，导致布局将要发生变化。
        do {
            final int width = MeasureSpec.getSize(widthMeasureSpec);
            final int height = MeasureSpec.getSize(heightMeasureSpec);

            Log.d(TAG, "onMeasure, width: " + width + " height: " + height);
            if (height < 0) {
                break;
            }

            if (mOldHeight < 0) {
                mOldHeight = height;
                break;
            }

            final int offset = mOldHeight - height;

            if (offset == 0) {
                Log.d(TAG, "" + offset + " == 0 break;");
                break;
            }

            if (offset == -mStatusBarHeight) {
                Log.w(TAG, String.format("offset just equal statusBar height %d", offset));
                // 极有可能是 相对本页面的二级页面的主题是全屏&是透明，但是本页面不是全屏，因此会有status bar的布局变化差异，进行调过
                break;
            }

            mOldHeight = height;
            final PanelLayout bottom = getPanelLayout(this);

            if (bottom == null) {
                Log.d(TAG, "bottom == null break;");
                break;
            }

            // 检测到真正的 由于键盘收起触发了本次的布局变化

            if (offset > 0) {
                //键盘弹起 (offset > 0，高度变小)
                bottom.setIsHide(true);
            } else if (mIsKeyboardShowing){
                // 1. 总得来首，在监听到键盘已经显示的前提下，键盘收回才是有效有意义的。
                // 2. 修复在Android L下使用V7.Theme.AppCompat主题，进入Activity，默认弹起面板bug，
                // 第2点的bug出现原因:在Android L下使用V7.Theme.AppCompat主题，并且不使用系统的ActionBar/ToolBar，V7.Theme.AppCompat主题,还是会先默认绘制一帧默认ActionBar，然后再将他去掉（略无语）
                //键盘收回 (offset < 0，高度变大)
                bottom.setIsShow(true);
            }

        } while (false);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    private PanelLayout mPanelLayout;

    private PanelLayout getPanelLayout(final View view) {
        if (mPanelLayout != null) {
            return mPanelLayout;
        }

        if (view instanceof PanelLayout) {
            mPanelLayout = (PanelLayout) view;
            return mPanelLayout;
        }

        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                PanelLayout v = getPanelLayout(((ViewGroup) view).getChildAt(i));
                if (v != null) {
                    mPanelLayout = v;
                    return mPanelLayout;
                }
            }
        }

        return null;

    }

    private boolean mIsKeyboardShowing = false;
    protected void onKeyboardShowing(final boolean isShowing){
        this.mIsKeyboardShowing = isShowing;
        getPanelLayout(this).setIsKeybordShowing(isShowing);
    }
    private int maxBottom = 0;

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if (b >= maxBottom && maxBottom != 0) {
            // 在底部，键盘隐藏状态
            Log.d(TAG, "keybor hiding");
            onKeyboardShowing(false);
        } else if (maxBottom != 0) {
            Log.d(TAG, "keybor showing");
            onKeyboardShowing(true);
        }

        if (maxBottom < b) {
            maxBottom = b;
        }

    }
    private int mLastHeight = 0;

    @Override
    public void onGlobalLayout() {
        final int height = getHeight();

        if (mLastHeight == 0) {
            mLastHeight = height;
            return;
        }

        if (mLastHeight == height) {
            return;
        }

        final int keyboardHeight = Math.abs(mLastHeight - height);
        mLastHeight = height;

        final boolean change = KeyboardUtil.saveKeyboardHeight(getContext(), keyboardHeight);
        if (change) {
            final int panelHeight = getPanelLayout(this).getHeight();
            final int validPanelHeight = KeyboardUtil.getValidPanelHeight(getContext());
            if (panelHeight != validPanelHeight) {
                Log.d(TAG, "refresh panel height");
                getPanelLayout(this).refreshHeight();
            }
        }

    }

}
