package com.hyf.takephotovideolib;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hyf.takephotovideolib.view.RecordStartView;
import com.hyf.takephotovideolib.view.SizeSurfaceView;
import com.mabeijianxi.smallvideorecord2.jniinterface.FFmpegBridge;

import java.io.File;

import top.zibin.luban.CompressionPredicate;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

/**
 * Created by  HYF on 2018/6/29.
 * Email：775183940@qq.com
 */

public class RecordVideoFragment extends BaseRecordFragment implements RecordStartView.OnRecordButtonListener, RecordVideoInterface, View.OnClickListener {
    public static final String MODE = "MODE";
    public static final String DURATION = "DURATION";
    public static final String SAVE_PATH = "SAVE_PATH";
    public static final String MAX_SIZE = "MAX_SIZE";

    private int mode;
    private int duration;
    private String savePath;
    private long maxSize;

    private final String TAG = "RecordVideoFragment";
    private SizeSurfaceView mRecordView;
    private RecordStartView mRecorderBtn;//录制按钮

    private ImageView mFacing;//前置后置切换按钮

    private ImageView mFlash;//闪光灯

    private RelativeLayout mBaseLayout;

    private RecordVideoControl mRecordControl;
    private TextView mRecordTV;
    private ImageView mCancel;

    // 是否删除源文件
    private static final boolean isDeleteOriginFile = true;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        mode = bundle.getInt(MODE);
        duration = bundle.getInt(DURATION);
        savePath = bundle.getString(SAVE_PATH);
        maxSize = bundle.getLong(MAX_SIZE);
        View view = inflater.inflate(R.layout.hyf_fragment_record_video, container, false);
        initView(view);
        initData();
        initListener();
        return view;
    }

    private void initView(View view) {
        mRecordView = (SizeSurfaceView) view.findViewById(R.id.hyf_fragment_recorder_video_sv);
        mBaseLayout = (RelativeLayout) view.findViewById(R.id.hyf_fragment_recorder_video_rl_container);
        mRecorderBtn = (RecordStartView) view.findViewById(R.id.hyf_fragment_recorder_video_btn_record);
        mFacing = (ImageView) view.findViewById(R.id.hyf_fragment_recorder_video_ib_switch);
        mFlash = (ImageView) view.findViewById(R.id.hyf_fragment_recorder_video_ib_flash);
        mCancel = (ImageView) view.findViewById(R.id.hyf_fragment_recorder_video_iv_close);
        mRecordTV = (TextView) view.findViewById(R.id.hyf_fragment_recorder_video_tv_des);
        RelativeLayout topContentView = view.findViewById(R.id.hyf_fragment_recorder_video_top_container);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            topContentView.setPadding(0, RecordVideoUtils.getStatusBarHeight(getContext()), 0, 0);
        }
    }


    private void initData() {
        mRecordTV.setText(RecordVideoUtils.getDesByMode(mode));
        mRecorderBtn.setMaxTime(duration);
        mRecorderBtn.setMode(mode);

        mRecordControl = new RecordVideoControl(getActivity(), savePath, mRecordView, this);
        mRecordControl.setMaxSize(maxSize);
        mRecordControl.setMaxTime(duration);

        setupFlashMode();
    }


    private void initListener() {
        mRecorderBtn.setOnRecordButtonListener(this);
        mCancel.setOnClickListener(this);
        mFlash.setOnClickListener(this);
        mFacing.setOnClickListener(this);
    }

    public static RecordVideoFragment newInstance(int mode, int duration, String savePath, long maxSize) {
        Bundle args = new Bundle();
        args.putInt(MODE, mode);
        args.putInt(DURATION, duration);
        args.putString(SAVE_PATH, savePath);
        args.putLong(MAX_SIZE, maxSize);
        RecordVideoFragment fragment = new RecordVideoFragment();
        fragment.setArguments(args);
        return fragment;
    }


    /**
     * 检测开启闪光灯
     */
    private void setupFlashMode() {
        if (mRecordControl.getCameraFacing() == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT) {
            mFlash.setVisibility(View.GONE);
            return;
        } else {
            mFlash.setVisibility(View.VISIBLE);
        }

        final int res;
        switch (mRecordControl.flashType) {
            case RecordVideoControl.FLASH_MODE_ON:
                res = R.drawable.hyf_ic_take_photo_video_flash_on_24dp;
                break;
            case RecordVideoControl.FLASH_MODE_OFF:
                res = R.drawable.hyf_ic_take_photo_video_flash_off_24dp;
                break;
            default:
                res = R.drawable.hyf_ic_take_photo_video_flash_off_24dp;
        }
        mFlash.setImageResource(res);
    }

    /**
     * 打开预览的fragment进行预览  照片/ 视频
     *
     * @param type
     * @param filePath
     */
    private final void startPreview(int type, String filePath) {
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.hyf_take_photo_video_fragment_container,
                        new VideoPlayFragment(filePath, type),
                        VideoPlayFragment.TAG)
                .addToBackStack(null)
                .commit();
    }

    // ——————————————————————————————————————————————————————

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.hyf_fragment_recorder_video_iv_close) {
            finish();
        } else if (i == R.id.hyf_fragment_recorder_video_ib_flash) {
            if (mRecordControl.getCameraFacing() == android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK) {
                mRecordControl.setFlashMode(mRecordControl.flashType == RecordVideoControl.FLASH_MODE_ON
                        ? RecordVideoControl.FLASH_MODE_OFF
                        : RecordVideoControl.FLASH_MODE_ON);
            }
            setupFlashMode();
        } else if (i == R.id.hyf_fragment_recorder_video_ib_switch) {
            mRecordControl.changeCamera(mFacing);
            setupFlashMode();
        }
    }

    @Override
    public void finish() {
        getActivity().finish();
    }

    // ——————————————————————————————————————————————————————

    @Override
    public void onStartRecord() {
        mRecordControl.startRecording();
    }

    @Override
    public void onStopRecord() {
        mRecordControl.stopRecording(true);
    }

    @Override
    public void onTakePhoto() {
        if (!mRecordControl.isTakeing())
            mRecordControl.takePhoto();
    }

    // —————————————————————————————————————————————————————
    @Override
    public void startRecord() {
        Log.v(TAG, "startRecord");
    }

    @Override
    public void onRecording(long recordTime) {
        Log.v(TAG, "onRecording:" + recordTime);
        if (recordTime / 1000 >= 1) {
            mRecordTV.setText(recordTime / 1000 + "秒");
        }
    }

    @Override
    public void onRecordFinish(final String videoPath) {
        // 重新预览
        mRecordControl.returnPreview();
        // 根据比例算出宽度
        int width = mRecordControl.getWindowWidth() * 960 / mRecordControl.getWindowHeight();
        // 开启任务压缩视频
        new CompressTask(videoPath, mRecordControl.getOrientation(), width, mRecordControl.getDefaultVideoFrameRate(), mRecordControl.getCameraFacing()).execute();
        //startPreview(VideoPlayFragment.FILE_TYPE_VIDEO, videoPath);
    }

    @Override
    public void onRecordError() {
        Log.v(TAG, "onRecordError");
    }

    @Override
    public void onTakePhoto(final File photo) {
        Log.v(TAG, "onTakePhoto");
        // 将图片保存在 DIRECTORY_DCIM 内存卡中
        try {
            // 压缩图片
            Luban.with(getContext())
                    .load(photo)
                    .ignoreBy(200)
                    .setTargetDir(savePath)
                    .filter(new CompressionPredicate() {
                        @Override
                        public boolean apply(String path) {
                            return !(TextUtils.isEmpty(path) || path.toLowerCase().endsWith(".gif"));
                        }
                    })
                    .setCompressListener(new OnCompressListener() {
                        ProgressDialog dialog;

                        @Override
                        public void onStart() {
                            dialog = ProgressDialog.show(getContext(), "提示", "正在处理图片中...", false, false);
                        }

                        @Override
                        public void onSuccess(File file) {
                            if (dialog != null) dialog.dismiss();
                            //切换fragment 预览刚刚的拍照
                            startPreview(VideoPlayFragment.FILE_TYPE_PHOTO, file.getAbsolutePath());
                            if (photo.exists() && isDeleteOriginFile) photo.delete();
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (dialog != null) dialog.dismiss();
                            Log.e(TAG, "compress photo error:::::" + e.getMessage());
                            // 如果压缩失败  直接使用原图
                            startPreview(VideoPlayFragment.FILE_TYPE_PHOTO, photo.getAbsolutePath());
                        }
                    })
                    .launch();

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    // ——————————————————————————————————————————————————

    /**
     * 压缩视频的任务
     */
    private final class CompressTask extends AsyncTask<Void, Void, String> {
        private String filePath;
        private int orientation;
        private int width;
        private int rate;
        private int cameraId;

        public CompressTask(String filePath, int orientation, int width, int rate, int cameraId) {
            this.filePath = filePath;
            this.orientation = orientation;
            this.width = width;
            this.rate = rate;
            this.cameraId = cameraId;
        }

        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            Log.v(TAG, "compress video prepare()");
            dialog = ProgressDialog.show(getActivity(), "提示", "正在处理视频中...", false, false);
        }

        @Override
        protected String doInBackground(Void... voids) {
            long startTime = System.currentTimeMillis();
            String newFilePath = getNewFilePath();
//            String wh = isVertical(orientation) ? width + ":960" : "960:" + width;
            String wh = isVertical(orientation) ? "-1:960" : "960:-1";
//            int cropSize = Math.abs(540 - width) / 2;
//            String commd = "ffmpeg -y -i " + filePath + " -vf scale=" + wh + " -r 25 -vf crop=" + wh + ":0:" + cropSize + " -acodec aac -vcodec h264 -b 1400k " + newFilePath;
            String commd = "ffmpeg -y -i " + filePath + " -vf scale=" + wh + " -r " + rate + " -acodec aac -vcodec h264 -vb 1400k " + newFilePath;
            Log.v(TAG, "compress video command>>>>>" + commd);
            int ret = FFmpegBridge.jxFFmpegCMDRun(commd);
            boolean success = ret == 0;
            Log.v(TAG, "compress video reslut>>>>>" + success);
            // 如果压缩成功 删除之前的视频
            File originFile = new File(filePath);
            if (originFile.exists() && isDeleteOriginFile && success) {
                originFile.delete();
            }
            long endTime = System.currentTimeMillis();
            Log.v(TAG, "compress video time>>>>>" + ((endTime - startTime) / 1000) + "s");
            return success ? newFilePath : filePath;
        }

        @Override
        protected void onPostExecute(String path) {
            if (dialog != null) dialog.dismiss();
            Log.v(TAG, "compress video completed>>>>>" + path);
            startPreview(VideoPlayFragment.FILE_TYPE_VIDEO, path);
        }

        /**
         * 判断用户拍摄视频是是否是  视屏 拍摄
         *
         * @param orientation
         * @return
         */
        private boolean isVertical(int orientation) {
            if (orientation == 0 || orientation == 180) {
                return true;
            }
            return false;
        }

        private String getNewFilePath() {
            return savePath + File.separator + System.currentTimeMillis() + ".mp4";
        }
    }
}
