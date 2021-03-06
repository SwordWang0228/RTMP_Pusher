package kim.hsl.rtmp;

import android.app.Activity;
import android.hardware.Camera;
import android.view.SurfaceHolder;

/**
 * 处理视频通道数据
 * 主要管理直播推流器 LivePusher 和 Camera 摄像头管理对象 CameraManager
 */
public class VideoChannel implements Camera.PreviewCallback, CameraManager.OnChangedSizeListener {


    /**
     * 直播推流器
     */
    private LivePusher mLivePusher;

    /**
     * Camera 摄像头管理对象
     */
    private CameraManager mCameraManager;

    /**
     * 视频码率
     */
    private int mBitrate;

    /**
     * 视频帧率
     */
    private int mFps;

    /**
     * 当前是否在直播
     */
    private boolean mIsLiving = true;

    public VideoChannel(LivePusher livePusher, Activity activity, int width, int height, int bitrate, int fps, int cameraId) {
        mLivePusher = livePusher;
        mBitrate = bitrate;
        mFps = fps;

        // 1. 初始化 Camera 相关参数
        mCameraManager = new CameraManager(activity, cameraId, width, height);
        // 2. 设置 Camera 预览数据回调接口
        //    通过该接口可以在本类中的实现的 onPreviewFrame 方法中
        //    获取到 NV21 数据
        mCameraManager.setPreviewCallback(this);
        // 3. 通过该回调接口, 可以获取到真实的 Camera 尺寸数据
        //    设置摄像头预览尺寸完成后, 会回调该接口
        mCameraManager.setOnChangedSizeListener(this);
    }

    /**
     * 设置预览图像画布
     * @param surfaceHolder
     */
    public void setPreviewDisplay(SurfaceHolder surfaceHolder) {
        mCameraManager.setPreviewDisplay(surfaceHolder);
    }


    /**
     * Camera 摄像头采集数据完毕, 通过回调接口传回数据
     * 数据格式是 nv21 格式的
     * @param data
     * @param camera
     */
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (mIsLiving) {
            mLivePusher.native_encodeCameraData(data);
        }
    }

    public void switchCamera() {
        mCameraManager.switchCamera();
    }

    /**
     * 真实摄像头数据的宽、高
     *
     * @param width
     *      真实摄像头的宽度
     * @param height
     *      真实摄像头的高度
     */
    @Override
    public void onChanged(int width, int height) {
        // 设置视频参数, 宽度, 高度, 码率, 帧率
        mLivePusher.native_setVideoEncoderParameters(width, height, mFps, mBitrate);
    }

    public void startLive() {
        mIsLiving = true;
    }

    public void stopLive() {
        mIsLiving = false;
    }
}
