
package com.cgfay.camera.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;

import com.cgfay.cameralibrary.R;
import com.cgfay.camera.fragment.CameraPreviewFragment;
import com.cgfay.filter.glfilter.resource.FilterHelper;
import com.cgfay.filter.glfilter.resource.MakeupHelper;
import com.cgfay.filter.glfilter.resource.ResourceHelper;
import com.cgfay.uitls.utils.NotchUtils;

/**
 * 相机预览页面
 */
public class CameraActivity extends AppCompatActivity {

    private static final String FRAGMENT_CAMERA = "fragment_camera";
    String sound_name,sound_path;
    String stitch_video,stitch_path;

    private CameraPreviewFragment mPreviewFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        sound_name = getIntent().getStringExtra("sound_name");
        sound_path = getIntent().getStringExtra("sound_path");


        stitch_video = getIntent().getStringExtra("stitch_video");
        stitch_path = getIntent().getStringExtra("stitch_path");


        if (null == savedInstanceState && mPreviewFragment == null) {
            mPreviewFragment = new CameraPreviewFragment();
            Bundle args = new Bundle();
            args.putString("sound_name", sound_name);
            args.putString("sound_path", sound_path);
            args.putString("stitch_video",stitch_video);
            args.putString("stitch_path",stitch_path);

            mPreviewFragment.setArguments(args);
            initResources();

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, mPreviewFragment, FRAGMENT_CAMERA)
                    .commit();
        }

   
    }

    /**
     * 人脸检测SDK验证，可以替换成自己的SDK
     */


    @Override
    protected void onResume() {
        super.onResume();
        handleFullScreen();
        registerHomeReceiver();
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//
//        System.out.println("CameraActivity.onActivityResult");
//
//        super.onActivityResult(requestCode, resultCode, data);
//
//        System.out.println("CameraActivity.onActivityResult");
//
//
//    }

    private void initResources() {
        new Thread(() -> {
            ResourceHelper.initAssetsResource(CameraActivity.this);
            FilterHelper.initAssetsFilter(CameraActivity.this);
            MakeupHelper.initAssetsMakeup(CameraActivity.this);
        }).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unRegisterHomeReceiver();
    }

    private void handleFullScreen() {
        // 是否全面屏
        if (NotchUtils.hasNotchScreen(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
                getWindow().setAttributes(lp);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mPreviewFragment == null || !mPreviewFragment.onBackPressed()) {
            Log.d("mmm","Camera Activity inside");
            super.onBackPressed();
            overridePendingTransition(0, R.anim.anim_slide_down);
        }
    }

    /**
     * 注册服务
     */
    private void registerHomeReceiver() {
        IntentFilter homeFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mHomePressReceiver, homeFilter);
    }

    /**
     * 注销服务
     */
    private void unRegisterHomeReceiver() {
        unregisterReceiver(mHomePressReceiver);
    }

    /**
     * Home按键监听服务
     */
    private BroadcastReceiver mHomePressReceiver = new BroadcastReceiver() {
        private final String SYSTEM_DIALOG_REASON_KEY = "reason";
        private final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (TextUtils.isEmpty(reason)) {
                    return;
                }
                // 当点击了home键时需要停止预览，防止后台一直持有相机
                if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
                    CameraPreviewFragment fragment = (CameraPreviewFragment) getSupportFragmentManager()
                            .findFragmentByTag(FRAGMENT_CAMERA);
                    if (fragment != null) {
                        fragment.cancelRecordIfNeeded();
                    }
                }
            }
        }
    };
}
