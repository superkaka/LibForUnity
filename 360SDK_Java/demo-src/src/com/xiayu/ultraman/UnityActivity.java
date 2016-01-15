package com.xiayu.ultraman;

import java.io.File;
import java.util.Date;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.qihoo.gamecenter.sdk.activity.ContainerActivity;
import com.qihoo.gamecenter.sdk.common.IDispatcherCallback;
import com.qihoo.gamecenter.sdk.demosp.R;
import com.qihoo.gamecenter.sdk.demosp.activity.*;
import com.qihoo.gamecenter.sdk.demosp.payment.QihooPayInfo;
import com.qihoo.gamecenter.sdk.demosp.utils.ProgressUtil;
import com.qihoo.gamecenter.sdk.demosp.utils.QihooUserInfo;
import com.qihoo.gamecenter.sdk.demosp.utils.QihooUserInfoListener;
import com.qihoo.gamecenter.sdk.demosp.utils.QihooUserInfoTask;
import com.qihoo.gamecenter.sdk.matrix.Matrix;
import com.qihoo.gamecenter.sdk.protocols.ProtocolConfigs;
import com.qihoo.gamecenter.sdk.protocols.ProtocolKeys;
import com.qihoo.stat.QHStatDo;
import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

public class UnityActivity extends SdkUserBaseActivity implements GameMainInterface
{
	
	private QihooSDKUtils sdkUtils;
	private boolean openLog = true;
	public void SetOpenLog(boolean value)
	{
		openLog = value;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		sdkUtils = QihooSDKUtils.getInstance();
		sdkUtils.mainActivity=this;
	}

	public void UnitySendMessage(String methodName, String arg)
	{
		UnityPlayer.UnitySendMessage("GlobalObj", methodName, arg);
	}

	public void StartLogin()
	{
		// doSdkLogin(true);
		// UnityPlayer.UnitySendMessage("Main Camera", "setUserInfo",
		// getLoginResultText());
		sdkUtils.doSdkLogin(this, false, true);
	}

	public void onGotUserInfo(QihooUserInfo userInfo, String json)
	{
		// TODO Auto-generated method stub
		// super.onGotUserInfo(userInfo);
		UnitySendMessage("onGotUserInfo", json);
	}

	public void onGotToken(String token)
	{
		// TODO Auto-generated method stub
		// super.onGotUserInfo(userInfo);
		ShowMessage("onGotToken");
		ShowMessage(token);
		UnitySendMessage("onGotToken", token);

	}

	public void onPayFinish(int code)
	{
		ShowMessage("onPayFinish  code:" + code);
		UnitySendMessage("onPayFinish", "" + code);
	}

	public void DoPay(
			int money,
			String QHId,
			String token,
			String produceName,
			String productId,
			String notifyURL,
			String appName,
			String appUserName,
			String appUserId,
			String extId,
			String ext2Id,
			String orderId
			)
	{
		
		sdkUtils.doSdkPay(
				this, 
				false,
				true, 
				true, 
				token,
				QHId,
				"" + (money * 100),
				/*exchangeRate*/ "1",
				produceName,
				productId, 
				notifyURL,
				appName,
				appUserName, 
				appUserId, 
				extId,
				ext2Id,
				orderId
				);
		
		
	}

	public void ShowMessage(String msg)
	{
		if(openLog == false)
			return;
		if(msg == null)
			msg= "null";
		Log.d("UnityJava", msg);
		//Toast.makeText(XiaoKaActivity.this, msg, Toast.LENGTH_LONG).show();
	}

	//关卡统计
	public void startLevel(String level)
	{
		QHStatDo.startLevel(level);
	}

	public void finishLevel(String level)
	{
		QHStatDo.finishLevel(level);
	}

	public void failLevel(String level, String reason)
	{
		QHStatDo.failLevel(level, reason);
	}
	
	/////支付统计
	// cash：支付金额（游戏自定义，可以是分或元为单位，游戏依照实际需求来定） 
	// number：购买道具数量 
	// props：购买道具名称 
	// source：支付方式（游戏自定义，给每个支付渠道定义的整型值） 
	// level：关卡名称 
	// rank：玩家等级 
	public void pay(int cash, int number, String props, int source,
			String level, String rank)
	{
		QHStatDo.pay(cash, number, props, source, level, rank);
	}
	
	// name：物品名称 
	// number：物品数量 
	// coinType: 虚拟币类型 
	// coin：消费的虚拟币数量 
	public void buy(String name, int number, String coinType, int coin)
	{
		QHStatDo.buy(name, number, coinType, coin);
	}
	
	// name：物品名称 
	// number：物品数量 
	// coinType：虚拟币类型 
	// coin：物品对应的虚拟币数量 
	// level：关卡名称 (可用作消耗原因)
	public void use(String name, int number, String coinType, int coin,
			String level)
	{
		QHStatDo.use(name, number, coinType, coin, level);
	}
	
	public void QihooInstallAPK(String path)
	{
		ShowMessage("installAPK:" + path);
		
		String fileName = path;
		Intent intent = new Intent(Intent.ACTION_VIEW);
		//如果没有这一步的话，最后安装好了，点打开，是不会打开新版本应用的。
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
		
		intent.setDataAndType(Uri.fromFile(new File(fileName)), "application/vnd.android.package-archive");
		startActivity(intent);
		
		//如果没有这一步最后不会提示完成、打开。
		android.os.Process.killProcess(android.os.Process.myPid());
	}
	
	private void QihooRestartApplication() 
	{
        final Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        
        android.os.Process.killProcess(android.os.Process.myPid());
	}

}
