package com.example.jin.hellofengmap.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.example.jin.hellofengmap.R;

/**
 * @Email hezutao@fengmap.com
 * @Version 2.0.0
 * @Description 仿CheckBox的ImageView
 */
@SuppressLint("AppCompatCustomView")
public class ImageViewCheckBox extends ImageView implements View.OnClickListener {
    /**
     * 不可使用状态
     */
    public static final int CHECK_STATE_DISABLED = 0;
    /**
     * 未选中
     */
    public static final int CHECK_STATE_UNCHECKED = 1;
    /**
     * 选中状态
     */
    public static final int CHECK_STATE_CHECKED = 2;
    /**
     * 选中状态图片
     */
    private int check_bkg_id;
    /**
     * 未选中状态图片
     */
    private int uncheck_bkg_id;
    /**
     * 不可使用状态图片
     */
    private int disable_check_bkg_id;
    /**
     * 选中状态
     */
    private int mCheckState;
    private OnCheckStateChangedListener mCheckStateListener;

    public ImageViewCheckBox(Context context) {
        this(context, null);
    }

    public ImageViewCheckBox(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public ImageViewCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
//        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ImageViewCheckBox);
//        if (typedArray == null) {
//            return;
//        }
//        mCheckState = typedArray.getInteger(R.styleable.ImageViewCheckBox_default_state, CHECK_STATE_UNCHECKED);
//
//        check_bkg_id = typedArray.getResourceId(R.styleable.ImageViewCheckBox_checked_bkg, 0);
//        uncheck_bkg_id = typedArray.getResourceId(R.styleable.ImageViewCheckBox_unchecked_bkg, 0);
//        disable_check_bkg_id = typedArray.getResourceId(R.styleable.ImageViewCheckBox_checked_disabled, 0);

        setStateChangedDrawable();
        setOnClickListener(this);

//        typedArray.recycle();
    }

    /**
     * 改变状态
     */
    public void setStateChanged() {
        if (mCheckState == CHECK_STATE_DISABLED) {
            return;
        }

        if (mCheckState == CHECK_STATE_UNCHECKED) {
            mCheckState = CHECK_STATE_CHECKED;
        } else if (mCheckState == CHECK_STATE_CHECKED) {
            mCheckState = CHECK_STATE_UNCHECKED;
        }

        setStateChangedDrawable();
        invokeStateChanged();
    }

    public void setCheckDisabled() {
        this.mCheckState = CHECK_STATE_DISABLED;
        setStateChangedDrawable();
    }

    /**
     * 设置背景
     */
    private void setStateChangedDrawable() {
        if (mCheckState == CHECK_STATE_UNCHECKED) {
            setImageResource(uncheck_bkg_id);
        } else if (mCheckState == CHECK_STATE_DISABLED) {
            setImageResource(disable_check_bkg_id);
        } else {
            setImageResource(check_bkg_id);
        }
    }

    @Override
    public void onClick(View v) {
        setStateChanged();
    }

    public void setOnCheckStateChangedListener(OnCheckStateChangedListener listener) {
        this.mCheckStateListener = listener;
    }

    /**
     * 调用监听回调
     */
    private void invokeStateChanged() {
        if (this.mCheckStateListener != null) {
            if (mCheckState == CHECK_STATE_UNCHECKED) {
                this.mCheckStateListener.onCheckStateChanged(ImageViewCheckBox.this, false);
            } else if (mCheckState == CHECK_STATE_CHECKED) {
                this.mCheckStateListener.onCheckStateChanged(ImageViewCheckBox.this, true);
            }
        }
    }

    public interface OnCheckStateChangedListener {
        void onCheckStateChanged(View view, boolean isChecked);
    }
}  