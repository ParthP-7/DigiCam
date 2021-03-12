package com.cgfay.camera.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Camera;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cgfay.camera.loader.impl.CameraMediaLoader;
import com.cgfay.camera.presenter.CameraPreviewPresenter;
import com.cgfay.camera.utils.FileUtils1;
import com.cgfay.camera.utils.FileUtils2;
import com.cgfay.camera.widget.CainTextureView;
import com.cgfay.camera.widget.CameraPreviewTopbar;
import com.cgfay.camera.widget.PreviewMeasureListener;
import com.cgfay.camera.widget.RecordButton;
import com.cgfay.camera.widget.RecordCountDownView;
import com.cgfay.camera.widget.RecordProgressView;
import com.cgfay.cameralibrary.R;
import com.cgfay.camera.camera.CameraParam;
import com.cgfay.camera.model.GalleryType;
import com.cgfay.camera.widget.CameraMeasureFrameLayout;
import com.cgfay.camera.widget.RecordSpeedLevelBar;
import com.cgfay.media.recorder.SpeedMode;
import com.cgfay.picker.MediaPicker;
import com.cgfay.picker.loader.AlbumDataLoader;
import com.cgfay.picker.model.AlbumData;
import com.cgfay.uitls.bean.MusicData;
import com.cgfay.uitls.dialog.DialogBuilder;
import com.cgfay.uitls.fragment.MusicPickerFragment;
import com.cgfay.uitls.fragment.PermissionErrorDialogFragment;
import com.cgfay.uitls.utils.BrightnessUtils;
import com.cgfay.uitls.utils.PermissionUtils;
import com.cgfay.uitls.widget.RoundOutlineProvider;
import com.cgfay.widget.CameraTabView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 相机预览页面
 */
public class CameraPreviewFragment extends Fragment implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "CameraPreviewFragment";
    private static final boolean VERBOSE = true;

    private static final String FRAGMENT_TAG = "FRAGMENT_TAG";
    private static final String FRAGMENT_DIALOG = "dialog";

    private static final int ALBUM_LOADER_ID = 1;

    private int REQUEST_CODE_PICK_AUDIO = 104;

    // 预览参数
    private CameraParam mCameraParam;

    // Fragment主页面
    private View mContentView;
    // 预览部分
    private CameraMeasureFrameLayout mPreviewLayout;
    private CainTextureView mCameraTextureView;
    // fps显示
    private TextView mFpsView,music_tv;

    // 录制进度条
    private RecordProgressView mProgressView;
    // 倒计时控件
    private RecordCountDownView mCountDownView;

    // 顶部topbar
    private CameraPreviewTopbar mPreviewTopbar;

    // 速度选择条
    private RecordSpeedLevelBar mSpeedBar;
    private boolean mSpeedBarShowing;
    // 贴纸按钮
    private LinearLayout mBtnStickers;
    // 录制按钮
    private RecordButton mBtnRecord;

    private View mLayoutMedia;
    // 媒体库按钮
    private ImageView mBtnMedia;

    // 刪除布局
    private LinearLayout mLayoutDelete;
    // 视频删除按钮
    private Button mBtnDelete;
    // 视频预览按钮
    private Button mBtnNext;
    // 相机指示器
    private CameraTabView mCameraTabView;
    private View mTabIndicator;

    private boolean mFragmentAnimating;
    private FrameLayout mFragmentContainer;
    // 贴纸资源页面
    private PreviewResourceFragment mResourcesFragment;
    // 滤镜页面
    private PreviewEffectFragment mEffectFragment;
    // 更多设置界面
    private PreviewSettingFragment mSettingFragment;

    private final Handler mMainHandler;
    private Activity mActivity;

    private CameraPreviewPresenter mPreviewPresenter;

    // 本地缩略图加载器
    private LoaderManager mLocalImageLoader;

    // 当前对话框
    private Dialog mDialog;

    private static final int DELAY_CLICK = 500;

    private boolean mOnClick;
    private Handler mHandler;

    public MediaPlayer mPlayer;
    public int mediaFileDuration = 0;
    public boolean isPrepaerdMp = true;
    public TextView tv_music_name;

    public String audio_path="",music_name="";

    public boolean musicadded = false;
    public float selectedSpeed = 1.0f;
    public String stitch_video="0",stitch_path="";


    public static int timer=0;

    public class MoveVideoTask extends AsyncTask<String, String, String> {
        int duration = 0;
        String newPath = "";
        String oldPath = "";

        public MoveVideoTask(String oldPath2, int duration2) {
            this.oldPath = oldPath2;
            this.duration = duration2;
        }

        public void onPreExecute() {
            super.onPreExecute();

        }

        public String doInBackground(String... strings) {
            OutputStream os;
            try {
                String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root + "/" + getString(R.string.app_name));
                myDir.mkdirs();
                //File file = new File(myDir, imageName);
                //newPath = new File(myDir, BeautyCameraActivity_demo.this + "/selected_vid" + FileUtils.getExtensionFromPath(this.oldPath);///+ FileUtils.getExtensionFromPath(selectedMusicPath));
                File f = new File(oldPath);

                Log.d("vidspaths",f.getName().toString());

                SharedPreferences preferences = getActivity().getApplicationContext().getSharedPreferences("MUSIC_FILE_NAME", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("music_name",""+f.getName());
                editor.apply();

                this.newPath = FileUtils2.getTempHiddenDirectoryPath(mActivity) + "/selected_vid" + FileUtils2.getExtensionFromPath(this.oldPath);
                File sourceLocation = new File(this.oldPath);
                if (sourceLocation.exists()) {
                    File targetLocation = new File(this.newPath);
                    if (targetLocation.exists()) {
                        targetLocation.delete();
                    }
                    InputStream is = null;
                    OutputStream os2 = null;
                    try {
                        InputStream is2 = new FileInputStream(sourceLocation);
                        try {
                            os = new FileOutputStream(targetLocation);
                        } catch (Throwable th) {
                            th = th;
                            is = is2;
                            is.close();
                            os2.close();
                            throw th;
                        }
                        try {
                            byte[] buffer = new byte[1024];
                            while (true) {
                                int length = is2.read(buffer);
                                if (length <= 0) {
                                    break;
                                }
                                os.write(buffer, 0, length);
                            }
                            is2.close();
                            os.close();
                        } catch (Throwable th2) {
                            os2 = os;
                            is = is2;
                            is.close();
                            os2.close();
                            throw th2;
                        }
                    } catch (Throwable th3) {
                        is.close();
                        os2.close();
                        throw th3;
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return newPath;
        }

        public void onPostExecute(String s) {
            super.onPostExecute(s);
            if (new File(newPath).exists()) {
//            startActivityForResult(new Intent(BeautyCameraActivity_demo.this, VideoTrimmerActivity.class).putExtra("EXTRA_PATH", newPath), VIDEO_TRIM);//OLD
//                startActivityForResult(new Intent(BeautyCameraActivity_demo.this, ChinTrimVideoActivity.class).putExtra("videoPath", newPath).putExtra("ISRecording", "0").putExtra("isFrom", ""), VIDEO_TRIM);//OLD
                try {
                    startActivity(new Intent(mActivity, Class.forName("com.digitok.VideoTrimmer.chinTrimmer.ChinTrimVideoActivity")).putExtra("videoPath", newPath).putExtra("ISRecording", "0").putExtra("isFrom", ""));//OLD
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public CameraPreviewFragment() {
        mCameraParam = CameraParam.getInstance();
        mMainHandler = new Handler(Looper.getMainLooper());
        mPreviewPresenter = new CameraPreviewPresenter(this);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            mActivity = (Activity) context;
        } else {
            mActivity = getActivity();
        }
        mPreviewPresenter.onAttach(mActivity);
        Log.d(TAG, "onAttach: ");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //music_tv = mContentView.findViewById(R.id.tv_music_name);
        Bundle bundle = getArguments();
        if (bundle.getString("sound_name") != null) {
            music_name = bundle.getString("sound_name");
            audio_path = bundle.getString("sound_path");
            Log.d("name_path",music_name + " s "+audio_path);
            musicadded=true;
        }

        Log.d("test_test",bundle.getString("stitch_path")+" "+bundle.getString("stitch_video"));

        if(bundle.getString("stitch_video")!=null){
            stitch_path = bundle.getString("stitch_path");
            stitch_video = bundle.getString("stitch_video");
        }

        mPreviewPresenter.onCreate();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        mContentView = inflater.inflate(R.layout.fragment_camera_preview, container, false);
        return mContentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (isCameraEnable()) {

            initView(mContentView);
        } else {
            PermissionUtils.requestCameraPermission(this);
        }

        if (PermissionUtils.permissionChecking(mActivity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            mLocalImageLoader = LoaderManager.getInstance(this);
            mLocalImageLoader.initLoader(ALBUM_LOADER_ID, null, this);
        }
    }

    /**
     * 初始化页面
     *
     * @param view
     */
    private void initView(View view) {
        initPreviewSurface();
        initPreviewTopbar();
        initBottomLayout(view);
        initCameraTabView();
        if(musicadded){
            mPreviewPresenter.setMusicPath(audio_path);
            loadMusic(audio_path,music_name);
        }

    }

    private  void loadMusic(String audio,String name){
        if(musicadded){
            File file = new File(audio);
            setUpMediaPlayer(audio);
            mPreviewTopbar.setSelectedMusic(name);
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(mActivity, Uri.fromFile(file));
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long timeInMillisec = Long.parseLong(time);
            if ((int)timeInMillisec/1000 <=60) {
                mPreviewPresenter.setRecordSeconds((int)timeInMillisec/1000);
            }
            else{
                Toast.makeText(mActivity, ""+"Only 60 sec video is allowed the music will be trimmed accordingly", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(mActivity, ""+"Error please try again!", Toast.LENGTH_SHORT).show();
        }


    }

    private void setUpMediaPlayer(String audioPath) {
        try {
            if(!audioPath.equals("") || audioPath!=null){
                this.isPrepaerdMp = false;
                if (this.mPlayer == null) {
                    this.mPlayer = new MediaPlayer();
                } else {
                    this.mPlayer.reset();
                }
                this.mPlayer.setDataSource(audioPath);
                this.mPlayer.setLooping(false);
                this.mPlayer.setOnPreparedListener(mp -> {
                    mPlayer = mp;
                    mPlayer.setLooping(false);
                    isPrepaerdMp = true;
                    mediaFileDuration = mPlayer.getDuration();
                    mp.start();
                    mp.pause();
//                mPlayer.start();
                });
                this.mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer mp) {
                        //Stop Recording
                    }
                });
                this.mPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        Toast.makeText(mActivity, "" + "Something went wrong!", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                });
                this.mPlayer.prepareAsync();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void releaseMediaPlayer() {
        if (this.mPlayer != null) {
            this.mPlayer.stop();
            this.mPlayer.release();
            this.mPlayer = null;
        }
    }

    private void pauseMediaPlayer() {
        if (this.mPlayer != null && this.mPlayer.isPlaying()) {
            this.mPlayer.pause();
        }
    }

    private void initPreviewSurface() {
        mFpsView = mContentView.findViewById(R.id.tv_fps);
        mPreviewLayout = mContentView.findViewById(R.id.layout_camera_preview);
        mCameraTextureView = new CainTextureView(mActivity);
        mCameraTextureView.addOnTouchScroller(mTouchScroller);
        mCameraTextureView.addMultiClickListener(mMultiClickListener);
        mCameraTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        mPreviewLayout.addView(mCameraTextureView);

        // 添加圆角显示
        if (Build.VERSION.SDK_INT >= 21) {
            mCameraTextureView.setOutlineProvider(new RoundOutlineProvider(getResources().getDimension(R.dimen.dp7)));
            mCameraTextureView.setClipToOutline(true);
        }
        mPreviewLayout.setOnMeasureListener(new PreviewMeasureListener(mPreviewLayout));
        mProgressView = mContentView.findViewById(R.id.record_progress);
        mCountDownView = mContentView.findViewById(R.id.count_down_view);
    }

    /**
     * 初始化顶部topbar
     */
    private void initPreviewTopbar() {

        mPreviewTopbar = mContentView.findViewById(R.id.camera_preview_topbar);
        mPreviewTopbar.addOnCameraCloseListener(this::closeCamera)
                .addOnCameraSwitchListener(this::switchCamera)
                .addOnShowPanelListener(type -> {
                    switch (type) {
                        case CameraPreviewTopbar.PanelMusic: {
                            openMusicPicker();
                            break;
                        }

                        case CameraPreviewTopbar.PanelSpeedBar: {
                            setShowingSpeedBar(mSpeedBar.getVisibility() != View.VISIBLE);
                            break;
                        }

                        case CameraPreviewTopbar.PanelFilter: {
                            showEffectFragment();
                            break;
                        }

                        case CameraPreviewTopbar.PanelSetting: {
                            showSettingFragment();
                            break;
                        }
                    }
                });
    }

    /**
     * 初始化底部布局
     *
     * @param view
     */
    private void initBottomLayout(@NonNull View view) {
        mFragmentContainer = view.findViewById(R.id.fragment_bottom_container);
        mSpeedBar = view.findViewById(R.id.record_speed_bar);
        mSpeedBar.setOnSpeedChangedListener((speed) -> {
            mPreviewPresenter.setSpeedMode(SpeedMode.valueOf(speed.getSpeed()));
        });


        mBtnStickers = view.findViewById(R.id.btn_stickers);
        mBtnStickers.setOnClickListener(this);
        mLayoutMedia = view.findViewById(R.id.layout_media);
        mBtnMedia = view.findViewById(R.id.btn_media);
        mBtnMedia.setOnClickListener(this);

        mBtnRecord = view.findViewById(R.id.btn_record);
        mBtnRecord.setOnClickListener(this);
        mBtnRecord.addRecordStateListener(mRecordStateListener);

        mLayoutDelete = view.findViewById(R.id.layout_delete);
        mBtnDelete = view.findViewById(R.id.btn_record_delete);
        mBtnDelete.setOnClickListener(this);
        mBtnNext = view.findViewById(R.id.btn_goto_edit);
        mBtnNext.setOnClickListener(this);

        setShowingSpeedBar(mSpeedBarShowing);
    }


    /**
     * 初始化相机底部tab view
     */
    private void initCameraTabView() {
        mTabIndicator = mContentView.findViewById(R.id.iv_tab_indicator);
        mCameraTabView = mContentView.findViewById(R.id.tl_camera_tab);

//        mCameraTabView.addTab(mCameraTabView.newTab().setText(R.string.tab_picture));
        //    mCameraTabView.addTab(mCameraTabView.newTab().setText(R.string.tab_video_15s), true);
        mCameraTabView.addTab(mCameraTabView.newTab().setText(R.string.tab_video_60s), true);
//        mCameraTabView.addTab(mCameraTabView.newTab().setText(R.string.tab_video_picture));

        mCameraTabView.setIndicateCenter(true);
        mCameraTabView.setScrollAutoSelected(true);
        mCameraTabView.addOnTabSelectedListener(new CameraTabView.OnTabSelectedListener() {
            @Override
            public void onTabSelected(CameraTabView.Tab tab) {
                int position = tab.getPosition();
//                if (position == 0) {
//                    mCameraParam.mGalleryType = GalleryType.PICTURE;
//                    if (!isStorageEnable()) {
//                        PermissionUtils.requestRecordSoundPermission(CameraPreviewFragment.this);
//                    }
//                    if (mBtnRecord != null) {
//                        mBtnRecord.setRecordEnable(false);
//                    }
//                } else
                if (position == 0) {
                    mCameraParam.mGalleryType = GalleryType.VIDEO_60S;
                    // 请求录音权限
                    if (!isRecordAudioEnable()) {
                        PermissionUtils.requestRecordSoundPermission(CameraPreviewFragment.this);
                    }
                    if (mBtnRecord != null) {
                        mBtnRecord.setRecordEnable(true);
                    }
                    mPreviewPresenter.setRecordSeconds(60);
                }
//                else if (position == 2) {
//                    mCameraParam.mGalleryType = GalleryType.VIDEO_15S;
//                    // 请求录音权限
//                    if (!isRecordAudioEnable()) {
//                        PermissionUtils.requestRecordSoundPermission(CameraPreviewFragment.this);
//                    }
//                    if (mBtnRecord != null) {
//                        mBtnRecord.setRecordEnable(true);
//                    }
//                    mPreviewPresenter.setRecordSeconds(15);
//                }
            }

            @Override
            public void onTabUnselected(CameraTabView.Tab tab) {

            }

            @Override
            public void onTabReselected(CameraTabView.Tab tab) {

            }
        });
        mPreviewPresenter.setRecordSeconds(60);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    //    super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_AUDIO) {
            try {
                if (data.getStringExtra("mAudioPath") != null || data.getStringExtra("mAudioPath") != "") {
                    musicadded = true;
                    mPreviewPresenter.setMusicPath(data.getStringExtra("mAudioPath"));
                    File file = new File(data.getStringExtra("mAudioPath"));
                    loadMusic(data.getStringExtra("mAudioPath"), file.getName());
                }
                else{
                    musicadded=false;
                }
            }
            catch (Exception e){
                musicadded=false;
            }
        }
        else if (requestCode == 791) {
            if (data != null) {
                try {
                    if (data.getData() != null) {
                        Uri uri = data.getData();
                        File video_file = FileUtils1.getFileFromUri(mActivity, uri);
                        UploadVideo(video_file.getPath());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }


    public void UploadVideo(String path) {
        if (path.isEmpty()) {
            return;
        }
        if (FileUtils2.getDurationFromFilePath(mActivity, path) >= ((long) ((int) getCalculatedSpeed(5000)))) {
            new MoveVideoTask(path, (int) FileUtils2.getDurationFromFilePath(mActivity, path)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[0]);
            return;
        }
        Toast.makeText(mActivity, "Please select minimum " + FileUtils2.makeShortTimeString(mActivity, (long) (getCalculatedSpeed(5000) / 1000.0f)) + " second video", Toast.LENGTH_LONG).show();
    }

    public float getCalculatedSpeed(int maximumTimeOut) {
        float value = (float) maximumTimeOut;
        if (this.selectedSpeed == 0.1f) {
            return ((float) (maximumTimeOut / 3)) + 500.0f;
        }
        if (this.selectedSpeed == 0.5f) {
            return ((float) (maximumTimeOut / 2)) + 500.0f;
        }
        if (this.selectedSpeed == 2.0f) {
            return (float) (maximumTimeOut * 2);
        }
        if (this.selectedSpeed == 3.0f) {
            return (float) (maximumTimeOut * 3);
        }
        return value;
    }



    /**
     * 显示影集蒙层
     */
    private void showVideoPicture() {
        // TODO 后续有时间再做
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
        mPreviewPresenter.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPreviewTopbar.tv_timer.setText(timer+" sec");
        enhancementBrightness();
        mPreviewPresenter.onResume();
        Log.d(TAG, "onResume: ");
    }

    /**
     * 增强光照
     */
    private void enhancementBrightness() {
        BrightnessUtils.setWindowBrightness(mActivity, mCameraParam.luminousEnhancement
                ? BrightnessUtils.MAX_BRIGHTNESS : mCameraParam.brightness);
    }

    @Override
    public void onPause() {
        super.onPause();
        mPreviewPresenter.onPause();
        Log.d(TAG, "onPause: ");
    }

    @Override
    public void onStop() {
        super.onStop();
        mPreviewPresenter.onStop();
        Log.d(TAG, "onStop: ");
    }

    @Override
    public void onDestroyView() {
        if(musicadded){
            releaseMediaPlayer();
        }
        if (mCountDownView != null) {
            mCountDownView.cancel();
            mCountDownView = null;
        }
        mContentView = null;
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: ");
    }

    @Override
    public void onDestroy() {
        destroyImageLoader();
        mPreviewPresenter.onDestroy();
        dismissDialog();
        mMainHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    @Override
    public void onDetach() {
        mPreviewPresenter.onDetach();
        mPreviewPresenter = null;
        mActivity = null;
        super.onDetach();
        Log.d(TAG, "onDetach: ");
    }

    /**
     * 处理返回按钮事件
     * @return 是否拦截返回按键事件
     */
    public boolean onBackPressed() {
        Fragment fragment = getChildFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (fragment != null) {
            mPreviewTopbar.setTimerText(timer+" sec");
            hideFragmentAnimating();
            return true;
        }

        else if (mCountDownView != null && mCountDownView.isCountDowning()) {
            mCountDownView.cancel();
            return true;
        }


        return false;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_stickers) {
            showStickers();
        } else if (i == R.id.btn_media) {
            if(stitch_video.equals("0")){
                openMediaPicker();
            }
            else{
                Toast.makeText(mActivity, ""+"Sorry! You can't stitch video from Gallery", Toast.LENGTH_LONG).show();
            }

        } else if (i == R.id.btn_record) {
            takePicture();
        } else if (i == R.id.btn_record_delete) {
            deleteRecordedVideo();
        } else if (i == R.id.btn_goto_edit) {
            stopRecordOrPreviewVideo();
        }
    }


    /**
     * 销毁当前的对话框
     */
    private void dismissDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    /**
     * 关闭相机
     */
    private void closeCamera() {
        if (mActivity != null) {
            mActivity.finish();
            mActivity.overridePendingTransition(0, R.anim.anim_slide_down);
        }
    }

    /**
     * 切换相机
     */
    private void switchCamera() {
        if (!isCameraEnable()) {
            PermissionUtils.requestCameraPermission(this);
            return;
        }
        mPreviewPresenter.switchCamera();
    }

    /**
     * 是否显示速度条
     * @param show
     */
    private void setShowingSpeedBar(boolean show) {
        mSpeedBarShowing = show;
        mSpeedBar.setVisibility(show ? View.VISIBLE : View.GONE);
        mPreviewTopbar.setSpeedBarOpen(show);
    }

    /**
     * 显示设置页面
     */
    private void showSettingFragment() {
        if (mFragmentAnimating) {
            return;
        }
        if (mSettingFragment == null) {
            mSettingFragment = new PreviewSettingFragment();
        }
        mSettingFragment.addStateChangedListener(mStateChangedListener);
        mSettingFragment.setEnableChangeFlash(mCameraParam.supportFlash);
        if (!mSettingFragment.isAdded()) {
            getChildFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_bottom_container, mSettingFragment, FRAGMENT_TAG)
                    .addToBackStack(FRAGMENT_TAG)
                    .commitAllowingStateLoss();
        } else {
            getChildFragmentManager()
                    .beginTransaction()
                    .show(mSettingFragment)
                    .commitAllowingStateLoss();
        }
        showFragmentAnimating();
    }

    /**
     * 显示动态贴纸页面
     */
    private void showStickers() {
        if (mFragmentAnimating) {
            return;
        }
        if (mResourcesFragment == null) {
            mResourcesFragment = new PreviewResourceFragment();
        }
        mResourcesFragment.addOnChangeResourceListener((data) -> {
            mPreviewPresenter.changeResource(data);
        });
        if (!mResourcesFragment.isAdded()) {
            getChildFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_bottom_container, mResourcesFragment, FRAGMENT_TAG)
                    .commitAllowingStateLoss();
        } else {
            getChildFragmentManager()
                    .beginTransaction()
                    .show(mResourcesFragment)
                    .commitAllowingStateLoss();
        }
        showFragmentAnimating(false);
    }

    /**
     * 显示滤镜页面
     */
    private void showEffectFragment() {
        if (mFragmentAnimating) {
            return;
        }
        if (mEffectFragment == null) {
            mEffectFragment = new PreviewEffectFragment();
        }
        mEffectFragment.addOnCompareEffectListener(compare -> {
            mPreviewPresenter.showCompare(compare);
        });
        mEffectFragment.addOnFilterChangeListener(color -> {
            mPreviewPresenter.changeDynamicFilter(color);
        });
        mEffectFragment.addOnMakeupChangeListener(makeup -> {
            mPreviewPresenter.changeDynamicMakeup(makeup);
        });
        mEffectFragment.scrollToCurrentFilter(mPreviewPresenter.getFilterIndex());
        if (!mEffectFragment.isAdded()) {
            getChildFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_bottom_container, mEffectFragment, FRAGMENT_TAG)
                    .commitAllowingStateLoss();
        } else {
            getChildFragmentManager()
                    .beginTransaction()
                    .show(mEffectFragment)
                    .commitAllowingStateLoss();
        }
        showFragmentAnimating();
    }

    /**
     * 显示Fragment动画
     */
    private void showFragmentAnimating() {
        showFragmentAnimating(true);
    }

    /**
     * 显示Fragment动画
     */
    private void showFragmentAnimating(boolean hideAllLayout) {
        if (mFragmentAnimating) {
            return;
        }
        mFragmentAnimating = true;
        mFragmentContainer.setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.preview_slide_up);
        mFragmentContainer.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mFragmentAnimating = false;
                if (hideAllLayout) {
                    hideAllLayout();
                } else {
                    hideWithoutSwitch();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    /**
     * 隐藏Fragment动画
     */
    private void hideFragmentAnimating() {
        if (mFragmentAnimating) {
            return;
        }
        mFragmentAnimating = true;
        Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.preivew_slide_down);
        mFragmentContainer.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                resetAllLayout();
                removeFragment();
                mFragmentAnimating = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    /**
     * 移除Fragment
     */
    private void removeFragment() {
        Fragment fragment = getChildFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (fragment != null) {
            getChildFragmentManager()
                    .beginTransaction()
                    .remove(fragment)
                    .commitAllowingStateLoss();
        }
    }

    /**
     * 隐藏所有布局
     */
    private void hideAllLayout() {
        mMainHandler.post(()-> {
            if (mPreviewTopbar != null) {
                mPreviewTopbar.hideAllView();
            }
            if (mSpeedBar != null) {
                mSpeedBar.setVisibility(View.GONE);
            }
            if (mBtnStickers != null) {
                mBtnStickers.setVisibility(View.GONE);
            }
            if (mBtnRecord != null) {
                mBtnRecord.setVisibility(View.GONE);
            }
            if (mLayoutMedia != null) {
                mLayoutMedia.setVisibility(View.GONE);
            }
            if (mLayoutDelete != null) {
                mLayoutDelete.setVisibility(View.GONE);
            }
            if (mCameraTabView != null) {
                mCameraTabView.setVisibility(View.GONE);
            }
            if (mTabIndicator != null) {
                mTabIndicator.setVisibility(View.GONE);
            }
        });
    }

    /**
     * 隐藏除切换相机按钮外的所有控件
     */
    private void hideWithoutSwitch() {
        mMainHandler.post(() -> {
            if (mPreviewTopbar != null) {
                mPreviewTopbar.hideWithoutSwitch();
            }
            if (mSpeedBar != null) {
                mSpeedBar.setVisibility(View.GONE);
            }
            if (mBtnStickers != null) {
                mBtnStickers.setVisibility(View.GONE);
            }
            if (mBtnRecord != null) {
                mBtnRecord.setVisibility(View.GONE);
            }
            if (mLayoutMedia != null) {
                mLayoutMedia.setVisibility(View.GONE);
            }
            if (mLayoutDelete != null) {
                mLayoutDelete.setVisibility(View.GONE);
            }
            if (mCameraTabView != null) {
                mCameraTabView.setVisibility(View.GONE);
            }
            if (mTabIndicator != null) {
                mTabIndicator.setVisibility(View.GONE);
            }
        });
    }

    /**
     * 录制状态隐藏
     */
    public void hideOnRecording() {
        mMainHandler.post(()-> {
            if (mPreviewTopbar != null) {
                mPreviewTopbar.hideAllView();
            }
            if (mSpeedBar != null) {
                mSpeedBar.setVisibility(View.GONE);
            }
            if (mBtnStickers != null) {
                mBtnStickers.setVisibility(View.GONE);
            }
            if (mLayoutMedia != null) {
                mLayoutMedia.setVisibility(View.GONE);
            }
            if (mCameraTabView != null) {
                mCameraTabView.setVisibility(View.GONE);
            }
            if (mTabIndicator != null) {
                mTabIndicator.setVisibility(View.GONE);
            }
            if (mBtnDelete != null) {
                mBtnDelete.setVisibility(View.GONE);
            }
        });
    }

    /**
     * 恢复所有布局
     */
    public void resetAllLayout() {
        mMainHandler.post(()-> {
            if (mPreviewTopbar != null) {
                mPreviewTopbar.resetAllView();
            }
            setShowingSpeedBar(mSpeedBarShowing);
            if (mBtnStickers != null) {
                mBtnStickers.setVisibility(View.VISIBLE);
            }
            if (mBtnRecord != null) {
                mBtnRecord.setVisibility(View.VISIBLE);
            }
            if (mLayoutDelete != null) {
                mLayoutDelete.setVisibility(View.VISIBLE);
            }
            if (mCameraTabView != null) {
                mCameraTabView.setVisibility(View.VISIBLE);
            }
            if (mTabIndicator != null) {
                mTabIndicator.setVisibility(View.VISIBLE);
            }
            resetDeleteButton();
            if (mBtnRecord != null) {
                mBtnRecord.reset();
            }
        });
    }

    /**
     * 复位删除按钮
     */
    private void resetDeleteButton() {
        boolean hasRecordVideo = (mPreviewPresenter.getRecordedVideoSize() > 0);
        if (mBtnNext != null) {
            mBtnNext.setVisibility(hasRecordVideo ? View.VISIBLE : View.GONE);
        }
        if (mBtnDelete != null) {
            mBtnDelete.setVisibility(hasRecordVideo ? View.VISIBLE : View.GONE);
        }
        if (mLayoutMedia != null) {
            mLayoutMedia.setVisibility(hasRecordVideo ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * 拍照
     */
    private void takePicture() {
        if (isStorageEnable()) {
            if (mCameraParam.mGalleryType == GalleryType.PICTURE) {
                if (mCameraParam.takeDelay) {
                    mCountDownView.addOnCountDownListener(new RecordCountDownView.OnCountDownListener() {
                        @Override
                        public void onCountDownEnd() {
                            mPreviewPresenter.takePicture();
                            resetAllLayout();
                        }

                        @Override
                        public void onCountDownCancel() {
                            resetAllLayout();
                        }
                    });
                    mCountDownView.start();
                    hideAllLayout();
                } else {
                    mPreviewPresenter.takePicture();
                }
            }
        } else {
            PermissionUtils.requestStoragePermission(this);
        }
    }

    /**
     * 取消录制
     */
    public void cancelRecordIfNeeded() {
        // 停止录制
        if (mPreviewPresenter.isRecording()) {
            // 取消录制
            mPreviewPresenter.cancelRecord();
        }
    }

    /**
     * 打开音乐选择页面
     */
    public void openMusicPicker() {

        if(musicadded){
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);

            builder.setTitle("Are you sure");
            builder.setMessage("You want to discard the previous music?");

            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    // Do nothing but close the dialog
                    Intent intent = null;
                    try {
                       intent = new Intent(mActivity, Class.forName("com.digitok.SoundLists.MusicLibrary_A"));
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    startActivityForResult(intent,REQUEST_CODE_PICK_AUDIO);

                    dialog.dismiss();
                }
            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Do nothing
                    dialog.dismiss();
                }
            });

            AlertDialog alert = builder.create();
            alert.show();
        }
        else{
            Intent intent = null;
            try {
                intent = new Intent(mActivity, Class.forName("com.digitok.SoundLists.MusicLibrary_A"));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            startActivityForResult(intent,REQUEST_CODE_PICK_AUDIO);
        }




    }
//        MusicPickerFragment fragment = new MusicPickerFragment();
//        fragment.addOnMusicSelectedListener(
//                new MusicPickerFragment.OnMusicSelectedListener() {
//            @Override
//            public void onMusicSelectClose() {
//                Fragment currentFragment = getChildFragmentManager().findFragmentByTag(MusicPickerFragment.TAG);
//                if (currentFragment != null) {
//                    getChildFragmentManager()
//                            .beginTransaction()
//                            .remove(currentFragment)
//                            .commitNowAllowingStateLoss();
//                }
//            }
//
//            @Override
//            public void onMusicSelected(MusicData musicData) {
//                resetAllLayout();
//                mPreviewPresenter.setMusicPath(musicData.getPath());
//                mPreviewTopbar.setSelectedMusic(musicData.getName()); //Music name setting
//            }
//        });

//        getChildFragmentManager()
//                .beginTransaction()
//                .add(fragment, MusicPickerFragment.TAG)
//                .commitAllowingStateLoss();
//    }




    /**
     * 打开媒体库选择页面
     */
    private void openMediaPicker() {
//        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
//        intent.setType("video/*");
//        startActivityForResult(intent, 791);
        mMainHandler.post(()-> {
            MediaPicker.from(this)
                    .showImage(false)
                    .showVideo(true)
                    .setMediaSelector(new NormalMediaSelector())
                    .show();
        });
    }

    // ------------------------------- TextureView 滑动、点击回调 ----------------------------------
    private CainTextureView.OnTouchScroller mTouchScroller = new CainTextureView.OnTouchScroller() {

        @Override
        public void swipeBack() {
            mPreviewPresenter.nextFilter();
        }

        @Override
        public void swipeFrontal() {
            mPreviewPresenter.previewFilter();
        }

        @Override
        public void swipeUpper(boolean startInLeft, float distance) {
            if (VERBOSE) {
                Log.d(TAG, "swipeUpper, startInLeft ? " + startInLeft + ", distance = " + distance);
            }
        }

        @Override
        public void swipeDown(boolean startInLeft, float distance) {
            if (VERBOSE) {
                Log.d(TAG, "swipeDown, startInLeft ? " + startInLeft + ", distance = " + distance);
            }
        }

    };

    /**
     * 单双击回调监听
     */
    private CainTextureView.OnMultiClickListener mMultiClickListener = new CainTextureView.OnMultiClickListener() {

        @Override
        public void onSurfaceSingleClick(final float x, final float y) {
            // 处理浮窗Fragment
            if (onBackPressed()) {
                return;
            }

            // 如果处于触屏拍照状态，则直接拍照，不做对焦处理
            if (mCameraParam.touchTake) {
                takePicture();
                return;
            }

            // todo 判断是否支持对焦模式

        }

        @Override
        public void onSurfaceDoubleClick(float x, float y) {
            switchCamera();
        }

    };

    // ---------------------------- TextureView SurfaceTexture监听 ---------------------------------
    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            mPreviewPresenter.onSurfaceCreated(surface);
            mPreviewPresenter.onSurfaceChanged(width, height);
            Log.d(TAG, "onSurfaceTextureAvailable: ");
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            mPreviewPresenter.onSurfaceChanged(width, height);
            Log.d(TAG, "onSurfaceTextureSizeChanged: ");
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            mPreviewPresenter.onSurfaceDestroyed();
            Log.d(TAG, "onSurfaceTextureDestroyed: ");
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    // ----------------------------------- 顶部状态栏点击回调 ------------------------------------
    private PreviewSettingFragment.StateChangedListener mStateChangedListener = new PreviewSettingFragment.StateChangedListener() {

        @Override
        public void flashStateChanged(boolean flashOn) {
            // todo 闪光灯切换

        }

        @Override
        public void onOpenCameraSetting() {
            mPreviewPresenter.onOpenCameraSettingPage();
        }

        @Override
        public void delayTakenChanged(boolean enable) {
            mCameraParam.takeDelay = enable;
        }

        @Override
        public void luminousCompensationChanged(boolean enable) {
            mCameraParam.luminousEnhancement = enable;
            enhancementBrightness();
        }

        @Override
        public void touchTakenChanged(boolean touchTake) {
            mCameraParam.touchTake = touchTake;
        }

        @Override
        public void changeEdgeBlur(boolean enable) {
            mPreviewPresenter.enableEdgeBlurFilter(enable);
        }
    };

    /**
     * 显示fps
     * @param fps
     */
    public void showFps(final float fps) {
        mMainHandler.post(() -> {
            if (mCameraParam.showFps) {
                mFpsView.setText("fps = " + fps);
                mFpsView.setVisibility(View.VISIBLE);
            } else {
                mFpsView.setVisibility(View.GONE);
            }
        });
    }

    /**
     * 更新录制时间
     * @param duration
     */
    public void updateRecordProgress(final float duration) {
        mMainHandler.post(() -> {
            mProgressView.setProgress(duration);
        });
    }

    /**
     * 添加一段进度
     * @param progress
     */
    public void addProgressSegment(float progress) {
        mMainHandler.post(() -> {
            mProgressView.addProgressSegment(progress);
        });
    }

    /**
     * 删除一段进度
     */
    public void deleteProgressSegment() {
        mMainHandler.post(() -> {
            mProgressView.deleteProgressSegment();
            resetDeleteButton();
        });
    }

    /**
     * 删除已录制的视频
     */
    private void deleteRecordedVideo() {
        dismissDialog();
        mDialog = DialogBuilder.from(mActivity, R.layout.dialog_two_button)
                .setText(R.id.tv_dialog_title, R.string.delete_last_video_tips)
                .setText(R.id.btn_dialog_cancel, R.string.btn_dialog_cancel)
                .setDismissOnClick(R.id.btn_dialog_cancel, true)
                .setText(R.id.btn_dialog_ok, R.string.btn_delete)
                .setDismissOnClick(R.id.btn_dialog_ok, true)
                .setOnClickListener(R.id.btn_dialog_ok, v -> {
                    if (musicadded) {
                        long timeInMillisec = mPreviewPresenter.deleteLastAudio()/1000;

                        int audio_backtime = (int) (mPlayer.getCurrentPosition() - timeInMillisec);
                        if (audio_backtime >= 0) {
                            mPlayer.seekTo(audio_backtime);
                        }
                        else{
                            mPlayer.seekTo(0);
                        }

                    }
                    mPreviewPresenter.deleteLastVideo();
                })
                .show();
    }

    /**
     * 停止录制或者预览视频
     */
    private void stopRecordOrPreviewVideo() {
        if(musicadded){
            releaseMediaPlayer();
        }
        if(stitch_video.equals("1")){
            mPreviewPresenter.stitchAndEdit(stitch_path);
            Log.d("test_test","inside_stitch");
        }
        else{
            mPreviewPresenter.mergeAndEdit();
            Log.d("test_test","inside_normal");
        }

    }

    /**
     * 录制监听器回调
     */
    private RecordButton.RecordStateListener mRecordStateListener = new RecordButton.RecordStateListener() {
        @Override
        public void onRecordStart() {

            if(timer>0){
                hideAllLayout();
                mCountDownView.addOnCountDownListener(new RecordCountDownView.OnCountDownListener() {
                    @Override
                    public void onCountDownEnd() {
                        try{
                            if(musicadded && mPlayer!=null){
                                mPlayer.start();
                            }
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                        mBtnRecord.setVisibility(View.VISIBLE);
                        mPreviewPresenter.startRecord();
                        timer=0;
                        mPreviewTopbar.tv_timer.setText("0 sec");
                    }
                    @Override
                    public void onCountDownCancel() {
                        resetAllLayout();
                    }
                });
                mCountDownView.setCountDown(timer);
                mCountDownView.start();
                timer = 0;
                mPreviewTopbar.tv_timer.setText("0 sec");

            }
            else
                {
                try{
                    if(musicadded && mPlayer!=null){
                        mPlayer.start();
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }

                mPreviewPresenter.startRecord();
            }


        }

        @Override
        public void onRecordStop() {
            try {
                if(musicadded && mPlayer!=null){
                    mPlayer.pause();
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }

            mPreviewPresenter.stopRecord();
        }

        @Override
        public void onZoom(float percent) {

        }
    };


    // -------------------------------------- 合成提示 --------------------------------------
    private Dialog mProgressDialog;
    /**
     * 显示合成进度
     */
    public void showConcatProgressDialog() {
        mMainHandler.post(() -> {
            mProgressDialog = ProgressDialog.show(mActivity, "Processing", "Processing...");
        });
    }

    /**
     * 隐藏合成进度
     */
    public void hideConcatProgressDialog() {
        mMainHandler.post(() -> {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
        });
    }

    private Toast mToast;
    /**
     * 显示Toast提示
     * @param msg
     */
    public void showToast(String msg) {
        mMainHandler.post(() -> {
            if (mToast != null) {
                mToast.cancel();
            }
            mToast = Toast.makeText(mActivity, msg, Toast.LENGTH_SHORT);
            mToast.show();
        });
    }

    // -------------------------------------- 权限逻辑处理 ---------------------------------
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionUtils.REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                PermissionErrorDialogFragment.newInstance(getString(R.string.request_camera_permission), PermissionUtils.REQUEST_CAMERA_PERMISSION, true)
                        .show(getChildFragmentManager(), FRAGMENT_DIALOG);
            } else {
                initView(mContentView);
            }
        } else if (requestCode == PermissionUtils.REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                PermissionErrorDialogFragment.newInstance(getString(R.string.request_storage_permission), PermissionUtils.REQUEST_STORAGE_PERMISSION)
                        .show(getChildFragmentManager(), FRAGMENT_DIALOG);
            }
        } else if (requestCode == PermissionUtils.REQUEST_SOUND_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                PermissionErrorDialogFragment.newInstance(getString(R.string.request_sound_permission), PermissionUtils.REQUEST_SOUND_PERMISSION)
                        .show(getChildFragmentManager(), FRAGMENT_DIALOG);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * 是否允许拍摄
     * @return
     */
    private boolean isCameraEnable() {
        return PermissionUtils.permissionChecking(mActivity, Manifest.permission.CAMERA);
    }

    /**
     * 判断是否可以录制
     * @return
     */
    private boolean isRecordAudioEnable() {
        return PermissionUtils.permissionChecking(mActivity, Manifest.permission.RECORD_AUDIO);
    }

    /**
     * 判断是否可以读取本地媒体
     * @return
     */
    private boolean isStorageEnable() {
        return PermissionUtils.permissionChecking(mActivity, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    // -------------------------------------- 缩略图加载逻辑 start ---------------------------------
    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle bundle) {
        return AlbumDataLoader.getImageLoaderWithoutBucketSort(mActivity);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.moveToFirst();
            AlbumData albumData = AlbumData.valueOf(cursor);
            if (mBtnMedia != null) {
                new CameraMediaLoader().loadThumbnail(mActivity, mBtnMedia, albumData.getCoverUri(),
                        R.drawable.ic_camera_thumbnail_placeholder,
                        (int)getResources().getDimension(R.dimen.dp4));
            }
            cursor.close();
            destroyImageLoader();
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

    /**
     * 销毁加载器
     */
    private void destroyImageLoader() {
        if (mLocalImageLoader != null) {
            mLocalImageLoader.destroyLoader(ALBUM_LOADER_ID);
            mLocalImageLoader = null;
        }
    }
    // -------------------------------------- 缩略图加载逻辑 end -----------------------------------
}
