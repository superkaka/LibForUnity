package com.xiayu.ultraman;

import com.qihoo.gamecenter.sdk.demosp.utils.QihooUserInfo;

public interface GameMainInterface
{
	public void onGotUserInfo(QihooUserInfo userInfo, String json);
	
	public void onGotToken(String token);
	
	public void onPayFinish(int code);
	
	public void UnitySendMessage(String methodName, String arg);
}
