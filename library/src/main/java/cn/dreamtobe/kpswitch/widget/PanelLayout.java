/*
 * Copyright (C) 2015-2016 Jacksgong(blog.dreamtobe.cn)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.dreamtobe.kpswitch.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import cn.dreamtobe.kpswitch.util.KeyboardUtil;


/**
 * @see CustomRootLayout
 */
public class PanelLayout extends LinearLayout {

    private boolean mIsShow = false;

    public PanelLayout(Context context) {
        super(context);
        init();
    }

    public PanelLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public PanelLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        refreshHeight();
    }

    public void refreshHeight() {
        if (isInEditMode()) {
            return;
        }

        if (getHeight() == KeyboardUtil.getValidPanelHeight(getContext())) {
            return;
        }

        post(new Runnable() {
            @Override
            public void run() {
                ViewGroup.LayoutParams layoutParams = getLayoutParams();
                if (layoutParams == null) {
                    layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, KeyboardUtil.getValidPanelHeight(getContext()));
                } else {
                    layoutParams.height = KeyboardUtil.getValidPanelHeight(getContext());
                }

                setLayoutParams(layoutParams);
            }
        });
    }

    /**
     * 键盘是否显示，从{@link CustomRootLayout#onLayout(boolean, int, int, int, int)}中获知
     */
    private boolean mIsKeyboardShowing = false;

    public void setKeyboardShowing(final boolean isKeyboardShowing) {
        this.mIsKeyboardShowing = isKeyboardShowing;
    }

    public boolean isKeyboardShowing() {
        return mIsKeyboardShowing;
    }

    public void hide() {
        mIsShow = false;
    }

    public void show() {
        mIsShow = true;
        //面板和键盘都未显示的时候
        if (!mIsKeyboardShowing && mIsShow) {
            setVisibility(VISIBLE);
        }
    }

    public boolean isShow() {
        return mIsShow;
    }
}
