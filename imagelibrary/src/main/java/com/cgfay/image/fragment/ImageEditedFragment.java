package com.cgfay.image.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.cgfay.imagelibrary.R;
import com.cgfay.uitls.fragment.BackPressedDialogFragment;
import com.cgfay.uitls.fragment.PermissionConfirmDialogFragment;
import com.cgfay.uitls.fragment.PermissionErrorDialogFragment;
import com.cgfay.uitls.utils.BitmapUtils;
import com.cgfay.uitls.utils.FileUtils;
import com.cgfay.uitls.utils.PermissionUtils;

import java.io.File;


/**
 * 图片编辑页面
 */
public class ImageEditedFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "ImageEditedFragment";
    private static final boolean VERBOSE = true;

    private static final String FRAGMENT_DIALOG = "dialog";

    private static final int FRAGMENT_CROP = 0;
    private static final int FRAGMENT_FILTER = 1;

    // 存储权限使能标志
    private boolean mStorageWriteEnable = false;

    // Fragment主页面
    private View mContentView;

    // 底部按钮布局
    private FrameLayout mLayoutButton;
    // 选择页面布局
    private LinearLayout mLayoutSelectPage;
    // 滤镜按钮
    private Button mBtnFilter;
    // 编辑工具箱按钮

    // 工具箱布局
    private LinearLayout mLayoutToolbox;
    // 编辑工具箱返回
    private Button mBtnToolboxBack;
    // 调整
    private Button mBtnAdjust;
    // 效果
    private Button mBtnEffect;
    // 纹理
    private Button mBtnTexture;
    // 虚化
    private Button mBtnBlur;
    // 曲线
    private Button mBtnCurve;
    // 色相/饱和度
    private Button mBtnHueSaturation;
    // 色调分离
    private Button mBtnToneSeparation;
    // 色彩平衡
    private Button mBtnColorBalance;

    // 裁剪页面
    private ImageCropFragment mImageCropFragment;
    // 滤镜页面
    private ImageFilterFragment mImageFilterFragment;
    // 当前fragment索引
    private int mCurrentFragment = FRAGMENT_FILTER;

    // 布局加载器
    private LayoutInflater mInflater;
    private Activity mActivity;

    // 图片路径
    private String mImagePath;
    private Bitmap mBitmap;
    private boolean mDeleteInputFile;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = getActivity();
        mStorageWriteEnable = PermissionUtils.permissionChecking(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        mInflater = LayoutInflater.from(mActivity);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragment_image_edit, container, false);
        return mContentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mStorageWriteEnable) {
            initView(mContentView);
        } else {
            requestStoragePermission();
        }
    }

    /**
     * 初始化页面
     * @param view
     */
    private void initView(View view) {
        // 选择编辑页面布局
        mLayoutButton = (FrameLayout) view.findViewById(R.id.layout_button);
        mLayoutSelectPage = (LinearLayout) mInflater.inflate(R.layout.view_image_fragment_button, null);
        mLayoutButton.addView(mLayoutSelectPage);
        mBtnFilter = (Button) mLayoutSelectPage.findViewById(R.id.btn_filter);
        mBtnFilter.setOnClickListener(this);

        // 默认显示滤镜编辑页面
        showFragment(FRAGMENT_FILTER);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mContentView = null;
    }

    @Override
    public void onDetach() {
        mActivity = null;
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDeleteInputFile) {
            FileUtils.deleteFile(mImagePath);
        }
    }

    public void onBackPressed() {
        new BackPressedDialogFragment().show(getChildFragmentManager(), "");
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_filter) { // Filter
            showFragment(FRAGMENT_FILTER);
        }
    }

    /**
     * Reset the bottom view
     */
    private void resetBottomView() {
        mLayoutButton.removeAllViews();
        mLayoutButton.addView(mLayoutSelectPage);
        mBtnFilter.setTextColor(Color.WHITE);
    }

    /**
     * The edit page is displayed
     * @param index
     */
    private void showFragment(int index) {
        resetBottomView();
        mCurrentFragment = index;
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        hideFragment(ft);
        if (index == FRAGMENT_CROP) {
            if (mImageCropFragment == null) {
                mImageCropFragment = new ImageCropFragment();
                ft.add(R.id.fragment_container, mImageCropFragment);
            } else {
                ft.show(mImageCropFragment);
            }
            if (mBitmap != null && !mBitmap.isRecycled()) {
                mImageCropFragment.setBitmap(mBitmap);
            }
            if (mImageFilterFragment != null) {
                mImageFilterFragment.showGLSurfaceView(false);
            }
        } else if (index == FRAGMENT_FILTER) {
            mBtnFilter.setTextColor(Color.BLUE);
            if (mImageFilterFragment == null) {
                mImageFilterFragment = new ImageFilterFragment();
                ft.add(R.id.fragment_container, mImageFilterFragment);
            } else {
                ft.show(mImageFilterFragment);
            }
            mImageFilterFragment.showGLSurfaceView(true);
            if (mBitmap != null && !mBitmap.isRecycled()) {
                mImageFilterFragment.setBitmap(mBitmap);
            }
        }
        ft.commit();
    }

    /**
     * 隐藏fragment
     * @param ft
     */
    private void hideFragment(FragmentTransaction ft) {
        if (mImageCropFragment != null) {
            ft.hide(mImageCropFragment);
        }
        if (mImageFilterFragment != null) {
            ft.hide(mImageFilterFragment);
        }
    }

    /**
     * 显示编辑工具箱
     */
    private void showToolbox() {
        if (mCurrentFragment != FRAGMENT_FILTER) {
            showFragment(FRAGMENT_FILTER);
        }
        if (mLayoutToolbox == null) {
            mLayoutToolbox = (LinearLayout) mInflater.inflate(R.layout.view_image_toolbox_layout, null);
            mBtnToolboxBack = (Button) mLayoutToolbox.findViewById(R.id.btn_toolbox_back);
            mBtnAdjust = (Button) mLayoutToolbox.findViewById(R.id.btn_adjust);
            mBtnEffect = (Button) mLayoutToolbox.findViewById(R.id.btn_effect);
            mBtnTexture = (Button) mLayoutToolbox.findViewById(R.id.btn_texture);
            mBtnBlur = (Button) mLayoutToolbox.findViewById(R.id.btn_blur);
            mBtnCurve = (Button) mLayoutToolbox.findViewById(R.id.btn_curve);
            mBtnHueSaturation = (Button) mLayoutToolbox.findViewById(R.id.btn_hue_saturation);
            mBtnToneSeparation = (Button) mLayoutToolbox.findViewById(R.id.btn_tone_separation);
            mBtnColorBalance = (Button) mLayoutToolbox.findViewById(R.id.btn_color_balance);

            mBtnToolboxBack.setOnClickListener(this);
            mBtnAdjust.setOnClickListener(this);
            mBtnEffect.setOnClickListener(this);
            mBtnTexture.setOnClickListener(this);
            mBtnBlur.setOnClickListener(this);
            mBtnCurve.setOnClickListener(this);
            mBtnHueSaturation.setOnClickListener(this);
            mBtnToneSeparation.setOnClickListener(this);
            mBtnColorBalance.setOnClickListener(this);

            resetToolBoxButton();
            mBtnAdjust.setTextColor(Color.BLUE);
        }

        if (mLayoutButton != null) {
            mLayoutButton.removeAllViews();
            mLayoutButton.addView(mLayoutToolbox);
        }
    }

    /**
     * 隐藏编辑工具箱
     */
    private void hideToolBox() {
        showFragment(FRAGMENT_FILTER);
    }

    /**
     * 重置工具箱按钮颜色
     */
    private void resetToolBoxButton() {
        mBtnAdjust.setTextColor(Color.WHITE);
        mBtnEffect.setTextColor(Color.WHITE);
        mBtnTexture.setTextColor(Color.WHITE);
        mBtnBlur.setTextColor(Color.WHITE);
        mBtnCurve.setTextColor(Color.WHITE);
        mBtnHueSaturation.setTextColor(Color.WHITE);
        mBtnToneSeparation.setTextColor(Color.WHITE);
        mBtnColorBalance.setTextColor(Color.WHITE);
    }

    /**
     * 设置图片路径
     * @param path
     */
    public void setImagePath(String path, boolean deleteInputFile) {
        mImagePath = path;
        mDeleteInputFile = deleteInputFile;
        mBitmap = BitmapUtils.getBitmapFromFile(new File(mImagePath), 0, 0, true);
    }

    /**
     * 请求存储权限
     */
    private void requestStoragePermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            PermissionConfirmDialogFragment.newInstance(getString(R.string.request_storage_permission), PermissionUtils.REQUEST_STORAGE_PERMISSION)
                    .show(getChildFragmentManager(), FRAGMENT_DIALOG);
        } else {
            requestPermissions(new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE},
                    PermissionUtils.REQUEST_STORAGE_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionUtils.REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                PermissionErrorDialogFragment.newInstance(getString(R.string.request_storage_permission), PermissionUtils.REQUEST_STORAGE_PERMISSION, true)
                        .show(getChildFragmentManager(), FRAGMENT_DIALOG);
            } else {
                mStorageWriteEnable = true;
                initView(mContentView);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
