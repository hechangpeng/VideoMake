package com.videocreator;

/**
 * Date：2018/1/25
 * Author：HeChangPeng
 */

public interface OnFinishListener {

    void onVideoMakeStart();

    void onVideoMakeFinish(boolean isSuccess);

    void onProgressIn(int percent);
}
