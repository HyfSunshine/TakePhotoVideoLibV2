# TakePhotoVideoLibrary
自定义Android端`相机拍照`和`视频拍摄`的库，拍照后自动压缩，视频拍摄控制视频分辨率和视频码率，拍出来的视频才小的很多，也能保证其清晰度。该库参考[SmallVideoRecording](https://github.com/dalong982242260/SmallVideoRecording)的开源库，感谢作者[dalong982242260](https://github.com/dalong982242260)
的开源，在此库的基础之上进行了二次扩展和一些bug的修复。

|作者|邮箱|
| :-:| :-: |
|丨Rainbow丨|775183940@qq.com|

## 一、Download

#### Project build.gradle中

    allprojects {
        repositories {
            jcenter()
            maven {
                url "https://jitpack.io"
            }
        }
    }



#### Module build.gradle中

    dependencies {
        implementation 'com.github.HyfSunshine:TakePhotoVideoLib:0.0.3'
     }

## 二、已依赖
    // 鲁班图片压缩  https://github.com/Curzibn/Luban
    compile 'top.zibin:Luban:1.1.7'
    // GSY播放器   https://github.com/CarGuo/GSYVideoPlayer
    compile 'com.shuyu:GSYVideoPlayer:5.0.1'

## 三、使用注意事项

1. Android 6.0 运行时权限处理
   ```
    <uses-permission android:name="android.permission.CAMERA"/>
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
    <uses-permission android:name="android.permission.RECORD_AUDIO"/>
    ```

2. 项目完全集成了 GSY播放器  由于ndk的so库比较大，选择合适的平台进行编译，具体使用如下：

    在gradle配置中
    ```
    android{
        ...
        defaultConfig{
            ...
            ...
            ndk {
                //APP的build.gradle设置支持的SO库架构
                //abiFilters 'armeabi', 'armeabi-v7a', 'x86'
                abiFilters 'armeabi'
            }
        }
    }
    ```

3. `minSdkVersion 16`

## 四、使用方法

#### 启动
1. 拍照

    ``` java
    TakePhotoVideoHelper.startTakePhoto(this, 100, savePath);
    ```
2. 拍摄视频

    ``` java
    TakePhotoVideoHelper.startTakeVideo(this, 100, savePath, 15000);
    ```
3. 拍照+视频拍摄

    ``` java
    TakePhotoVideoHelper.startTakePhotoVideo(this, 100, savePath, 15000);
    ```

#### 回调

``` java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == 100 && resultCode == RESULT_OK) {
        String path = data.getStringExtra(TakePhotoVideoHelper.RESULT_DATA);
        Toast.makeText(this, path, Toast.LENGTH_SHORT).show();
    }
}
```

## 历史更新

#### 0.0.3
1. 修复拍照、录制视频方向的问题
2. 可以横屏、竖屏拍照和录制视频
3. 优化视频拍摄码率，默认使用720P录制





