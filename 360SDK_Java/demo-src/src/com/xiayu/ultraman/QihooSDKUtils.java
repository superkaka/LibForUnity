
package com.xiayu.ultraman;

import com.qihoo.gamecenter.sdk.common.IDispatcherCallback;
import com.qihoo.gamecenter.sdk.demosp.utils.QihooUserInfo;
import com.qihoo.gamecenter.sdk.protocols.ProtocolConfigs;
import com.qihoo.gamecenter.sdk.protocols.ProtocolKeys;
import com.qihoo.gamecenter.sdk.matrix.Matrix;
import com.qihoo.gamecenter.sdk.activity.ContainerActivity;
import com.unity3d.player.UnityPlayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.os.Process;

/**
 * SdkUserBaseActivity这个基类，处理请求360SDK的登录和支付接口。
 * 使用方的Activity继承SdkUserBaseActivity，调用doSdkLogin接口发起登录请求；调用doSdkPay接口发起支付请求。
 * 父类通过onGotAuthorizationCode通知子类登录获取的授权码
 * ，子类实现onGotAuthorizationCode接口接收授权码，做后续处理。
 */
public class QihooSDKUtils {

    // 登录响应模式：CODE模式。
    protected static final String RESPONSE_TYPE_CODE = "code";

    private static final String TAG = "UnityJava";
    
	private volatile static QihooSDKUtils uniqueInstance = null;
    
	public static QihooSDKUtils getInstance() {
	
		if (uniqueInstance == null) {
			// second check need to synchronize, but only run limit times.
			synchronized (QihooSDKUtils.class) {
				if (uniqueInstance == null) {
					uniqueInstance = new QihooSDKUtils();
				}
			}
		}
		return uniqueInstance;
	}

    public void init(Activity activity)
	{
        Matrix.init(activity);
	}
	
    // ---------------------------------调用360SDK接口------------------------------------

    /**
     * 使用360SDK的登录接口
     * 
     * @param isLandScape 是否横屏显示登录界面
     * @param isBgTransparent 是否以透明背景显示登录界面
     */
    public void doSdkLogin(Activity activity, boolean isLandScape, boolean isBgTransparent) {
        Log.d(TAG, "doSdkLogin");

        Intent intent = getLoginIntent(activity, isLandScape, isBgTransparent);

        Matrix.invokeActivity(activity, intent, mLoginCallback);
    }

    /**
     * 使用360SDK的切换账号接口
     * 
     * @param isLandScape 是否横屏显示登录界面
     * @param isBgTransparent 是否以透明背景显示登录界面
     */
    public void doSdkSwitchAccount(Activity activity, boolean isLandScape, boolean isBgTransparent) {
        Log.d(TAG, "doSdkSwitchAccount");

        Intent intent = getSwitchAccountIntent(activity, isLandScape, isBgTransparent);

        Matrix.invokeActivity(activity, intent, mAccountSwitchCallback);
    }

    /**
     * 使用360SDK的支付接口
     * 
     * @param isLandScape 是否横屏显示支付界面
     * @param isFixed 是否定额支付
     */
    public void doSdkPay(Activity activity, 
    		final boolean isLandScape, 
    		final boolean isFixed,
            final boolean isBgTransparent,
    		String accessToken,
    		String qihooUserId,
    		String moneyAmount,
    		String exchangeRate,
    		String productName,
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
    	
    	Log.d(TAG, "doSdkPay");
    	Log.d(TAG, "accessToken:" + accessToken);
    	Log.d(TAG, "qihooUserId:" + qihooUserId);
    	Log.d(TAG, "moneyAmount:" + moneyAmount);
    	Log.d(TAG, "exchangeRate:" + exchangeRate);
    	Log.d(TAG, "productName:" + productName);
    	Log.d(TAG, "productId:" + productId);
    	Log.d(TAG, "notifyURL:" + notifyURL);
    	Log.d(TAG, "appName:" + appName);
    	Log.d(TAG, "appUserName:" + appUserName);
    	Log.d(TAG, "appUserId:" + appUserId);
    	Log.d(TAG, "extId:" + extId);
    	Log.d(TAG, "ext2Id:" + ext2Id);
    	Log.d(TAG, "orderId:" + orderId);
    	
        // 支付基础参数
        Intent intent = getPayIntent(
        		activity, 
        		isLandScape, 
        		isFixed,
        		accessToken,
        		qihooUserId,
        		moneyAmount,
        		exchangeRate,
        		productName,
        		productId,
        		notifyURL,
        		appName,
        		appUserName,
        		appUserId,
        		extId,
        		ext2Id,
        		orderId
        		);

        // 必需参数，使用360SDK的支付模块。
        intent.putExtra(ProtocolKeys.FUNCTION_CODE, ProtocolConfigs.FUNC_CODE_PAY);

        // 界面相关参数，360SDK登录界面背景是否透明。
        intent.putExtra(ProtocolKeys.IS_LOGIN_BG_TRANSPARENT, isBgTransparent);

        Matrix.invokeActivity(activity, intent, mPayCallback);
    }

    /**
     * 使用360SDK的退出接口
     * 
     * @param isLandScape 是否横屏显示支付界面
     */
    public void doSdkQuit(Activity activity, boolean isLandScape) {
        Bundle bundle = new Bundle();

        // 界面相关参数，360SDK界面是否以横屏显示。
        bundle.putBoolean(ProtocolKeys.IS_SCREEN_ORIENTATION_LANDSCAPE, isLandScape);

        // 可选参数，登录界面的背景图片路径，必须是本地图片路径
        bundle.putString(ProtocolKeys.UI_BACKGROUND_PICTRUE, "");

        // 必需参数，使用360SDK的退出模块。
        bundle.putInt(ProtocolKeys.FUNCTION_CODE, ProtocolConfigs.FUNC_CODE_QUIT);
        Intent intent = new Intent(activity, ContainerActivity.class);
        intent.putExtras(bundle);

        Matrix.invokeActivity(activity, intent, mQuitCallback);
    }

    /**
     * 使用360SDK的论坛接口
     * 
     * @param isLandScape 是否横屏显示支付界面
     */
    public void doSdkBBS(Activity activity, boolean isLandScape) {
        Bundle bundle = new Bundle();

        // 界面相关参数，360SDK界面是否以横屏显示。
        bundle.putBoolean(ProtocolKeys.IS_SCREEN_ORIENTATION_LANDSCAPE, isLandScape);

        // 必需参数，使用360SDK的论坛模块。
        bundle.putInt(ProtocolKeys.FUNCTION_CODE, ProtocolConfigs.FUNC_CODE_BBS);

        Intent intent = new Intent(activity, ContainerActivity.class);
        intent.putExtras(bundle);

        Matrix.invokeActivity(activity, intent, null);
    }

    /**
     * 使用360SDK客服中心接口
     * 
     * @param isLandScape 是否横屏显示界面
     */
    public void doSdkCustomerService(Activity activity, boolean isLandScape) {

        Intent intent = getCustomerServiceIntent(activity, isLandScape);

        Matrix.invokeActivity(activity, intent, null);
    }

    /**
     * 使用360SDK实名注册接口
     * 
     * @param isLandScape 是否横屏显示登录界面
     * @param isBgTransparent 是否以透明背景显示登录界面
     */
    public void doSdkRealNameRegister(Activity activity, boolean isLandScape, boolean isBgTransparent,
            String qihooUserId) {

        Intent intent = getRealNameRegisterIntent(activity, isLandScape, isBgTransparent, qihooUserId);

        Matrix.invokeActivity(activity, intent, mRealNameRegisterCallback);
    }

    /**
     * 使用360SDK绑定手机号接口
     * 
     * @param isLandScape 是否横屏显示登录界面
     */
    public void doSdkBindPhoneNum(Activity activity, boolean isLandScape) {

        Intent intent = getBindPhoneNumIntent(activity, isLandScape);

        Matrix.invokeActivity(activity, intent, mBindPhoneNumCallback);
    }

    /**
     * 使用360SDK截屏发帖接口
     * 
     * @param isLandScape 是否横屏显示界面
     */
    public void doSdkBBSPost(Activity activity, boolean isLandScape) {

        Intent intent = getBBSPostIntent(activity, isLandScape);

        Matrix.invokeActivity(activity, intent, null);
    }

    /**
     * 使用360SDK悬浮窗设置接口
     */
//    public void doSdkSettings(Activity activity, boolean isLandScape) {
//
//        Intent intent = getSettingIntent(activity, isLandScape);
//
//        Matrix.execute(SdkUserBaseActivity.activity, intent,
//                new IDispatcherCallback() {
//
//                    @Override
//                    public void onFinished(String data) {
//
//                    }
//                });
//    }

    // -----------------------------------参数Intent-------------------------------------

    /***
     * 生成调用360SDK登录接口的Intent
     * 
     * @param isLandScape 是否横屏
     * @param isBgTransparent 是否背景透明
     * @return Intent
     */
    private Intent getLoginIntent(Activity activity, boolean isLandScape, boolean isBgTransparent) {

        Intent intent = new Intent(activity, ContainerActivity.class);

        // 界面相关参数，360SDK界面是否以横屏显示。
        intent.putExtra(ProtocolKeys.IS_SCREEN_ORIENTATION_LANDSCAPE, isLandScape);

        // 必需参数，使用360SDK的登录模块。
        intent.putExtra(ProtocolKeys.FUNCTION_CODE, ProtocolConfigs.FUNC_CODE_LOGIN);

        //是否显示关闭按钮
        intent.putExtra(ProtocolKeys.IS_LOGIN_SHOW_CLOSE_ICON, false);

        // 可选参数，是否支持离线模式，默认值为false
        intent.putExtra(ProtocolKeys.IS_SUPPORT_OFFLINE,false);

        // 可选参数，是否在自动登录的过程中显示切换账号按钮
        intent.putExtra(ProtocolKeys.IS_SHOW_AUTOLOGIN_SWITCH, true);

        // 可选参数，是否隐藏欢迎界面
        intent.putExtra(ProtocolKeys.IS_HIDE_WELLCOME, true);

        // 可选参数，登录界面的背景图片路径，必须是本地图片路径
//        intent.putExtra(ProtocolKeys.UI_BACKGROUND_PICTRUE, getUiBackgroundPicPath());

        // 可选参数，指定assets中的图片路径，作为背景图
//        intent.putExtra(ProtocolKeys.UI_BACKGROUND_PICTURE_IN_ASSERTS, getUiBackgroundPathInAssets());

        //-- 以下参数仅仅针对自动登录过程的控制
        // 可选参数，自动登录过程中是否不展示任何UI，默认展示。
        intent.putExtra(ProtocolKeys.IS_AUTOLOGIN_NOUI, true);

        // 可选参数，静默自动登录失败后是否显示登录窗口，默认不显示
        intent.putExtra(ProtocolKeys.IS_SHOW_LOGINDLG_ONFAILED_AUTOLOGIN, false);

        // 测试参数，发布时要去掉
//        intent.putExtra(ProtocolKeys.IS_SOCIAL_SHARE_DEBUG, getCheckBoxBoolean(R.id.isDebugSocialShare));

        return intent;
    }

    /***
     * 生成调用360SDK切换账号接口的Intent
     * 
     * @param isLandScape 是否横屏
     * @param isBgTransparent 是否背景透明
     * @return Intent
     */
    private Intent getSwitchAccountIntent(Activity activity, boolean isLandScape, boolean isBgTransparent) {

        Bundle bundle = new Bundle();

        // 界面相关参数，360SDK界面是否以横屏显示。
        bundle.putBoolean(ProtocolKeys.IS_SCREEN_ORIENTATION_LANDSCAPE, isLandScape);

        // 界面相关参数，360SDK登录界面背景是否透明。
        bundle.putBoolean(ProtocolKeys.IS_LOGIN_BG_TRANSPARENT, isBgTransparent);

        // *** 以下非界面相关参数 ***

        // 必需参数，登录回应模式：CODE模式，即返回Authorization Code的模式。
        bundle.putString(ProtocolKeys.RESPONSE_TYPE, RESPONSE_TYPE_CODE);

        // 必需参数，使用360SDK的切换账号模块。
        bundle.putInt(ProtocolKeys.FUNCTION_CODE, ProtocolConfigs.FUNC_CODE_SWITCH_ACCOUNT);

        Intent intent = new Intent(activity, ContainerActivity.class);
        intent.putExtras(bundle);

        return intent;
    }

    /***
     * 生成调用360SDK支付接口基础参数的Intent
     * 
     * @param isLandScape
     * @return Intent
     */
    protected Intent getPayIntent(
    		Activity activity, 
    		boolean isLandScape, 
    		boolean isFixed,
    		String accessToken,
    		String qihooUserId,
    		String moneyAmount,
    		String exchangeRate,
    		String productName,
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

        Bundle bundle = new Bundle();

        // 界面相关参数，360SDK界面是否以横屏显示。
        bundle.putBoolean(ProtocolKeys.IS_SCREEN_ORIENTATION_LANDSCAPE, isLandScape);

        // *** 以下非界面相关参数 ***

        // 设置QihooPay中的参数。
        // 必需参数，用户access token，要使用注意过期和刷新问题，最大64字符。
        bundle.putString(ProtocolKeys.ACCESS_TOKEN, accessToken);

        // 必需参数，360账号id，整数。
        bundle.putString(ProtocolKeys.QIHOO_USER_ID, qihooUserId);

        // 必需参数，所购买商品金额, 以分为单位。金额大于等于100分，360SDK运行定额支付流程； 金额数为0，360SDK运行不定额支付流程。
        bundle.putString(ProtocolKeys.AMOUNT, moneyAmount);

        // 必需参数，人民币与游戏充值币的默认比例，例如2，代表1元人民币可以兑换2个游戏币，整数。
        bundle.putString(ProtocolKeys.RATE, exchangeRate);

        // 必需参数，所购买商品名称，应用指定，建议中文，最大10个中文字。
        bundle.putString(ProtocolKeys.PRODUCT_NAME, productName);

        // 必需参数，购买商品的商品id，应用指定，最大16字符。
        bundle.putString(ProtocolKeys.PRODUCT_ID, productId);

        // 必需参数，应用方提供的支付结果通知uri，最大255字符。360服务器将把支付接口回调给该uri，具体协议请查看文档中，支付结果通知接口–应用服务器提供接口。
        bundle.putString(ProtocolKeys.NOTIFY_URI, notifyURL);

        // 必需参数，游戏或应用名称，最大16中文字。
        bundle.putString(ProtocolKeys.APP_NAME, appName);

        // 必需参数，应用内的用户名，如游戏角色名。 若应用内绑定360账号和应用账号，则可用360用户名，最大16中文字。（充值不分区服，
        // 充到统一的用户账户，各区服角色均可使用）。
        bundle.putString(ProtocolKeys.APP_USER_NAME, appUserName);

        // 必需参数，应用内的用户id。
        // 若应用内绑定360账号和应用账号，充值不分区服，充到统一的用户账户，各区服角色均可使用，则可用360用户ID最大32字符。
        bundle.putString(ProtocolKeys.APP_USER_ID, appUserId);

        // 可选参数，应用扩展信息1，原样返回，最大255字符。
        bundle.putString(ProtocolKeys.APP_EXT_1, extId);
        
        // 可选参数，应用扩展信息1，原样返回，最大255字符。
        bundle.putString(ProtocolKeys.APP_EXT_2, ext2Id);

        // 可选参数，应用扩展信息2，原样返回，最大255字符。
//        bundle.putString(ProtocolKeys.APP_EXT_2, pay.getAppExt2());

        // 可选参数，应用订单号，应用内必须唯一，最大32字符。
        bundle.putString(ProtocolKeys.APP_ORDER_ID, orderId);

        Intent intent = new Intent(activity, ContainerActivity.class);
        intent.putExtras(bundle);

        return intent;
    }

    /***
     * 生成调用360SDK退出接口的Intent
     * 
     * @param isLandScape
     * @return Intent
     */
    private Intent getQuitIntent(Activity activity, boolean isLandScape) {

        Bundle bundle = new Bundle();

        // 界面相关参数，360SDK界面是否以横屏显示。
        bundle.putBoolean(ProtocolKeys.IS_SCREEN_ORIENTATION_LANDSCAPE, isLandScape);

        // 必需参数，使用360SDK的退出模块。
        bundle.putInt(ProtocolKeys.FUNCTION_CODE, ProtocolConfigs.FUNC_CODE_QUIT);

        Intent intent = new Intent(activity, ContainerActivity.class);
        intent.putExtras(bundle);

        return intent;
    }

    /***
     * 生成实名注册登录接口的Intent
     * 
     * @param isLandScape 是否横屏
     * @param isBgTransparent 是否背景透明
     * @param qihooUserId 奇虎UserId
     * @return Intent
     */
    private Intent getRealNameRegisterIntent(Activity activity, boolean isLandScape, boolean isBgTransparent,
            String qihooUserId) {

        Bundle bundle = new Bundle();

        // 界面相关参数，360SDK界面是否以横屏显示。
        bundle.putBoolean(ProtocolKeys.IS_SCREEN_ORIENTATION_LANDSCAPE, isLandScape);

        // 背景是否透明
        bundle.putBoolean(ProtocolKeys.IS_LOGIN_BG_TRANSPARENT, isBgTransparent);

        // 必需参数，360账号id，整数。
        bundle.putString(ProtocolKeys.QIHOO_USER_ID, qihooUserId);

        // 必需参数，使用360SDK的实名注册模块。
        bundle.putInt(ProtocolKeys.FUNCTION_CODE, ProtocolConfigs.FUNC_CODE_REAL_NAME_REGISTER);

        Intent intent = new Intent(activity, ContainerActivity.class);
        intent.putExtras(bundle);

        return intent;
    }

    /***
     * 生成绑定手机号接口的Intent
     * 
     * @param isLandScape 是否横屏
     * @return Intent
     */
    private Intent getBindPhoneNumIntent(Activity activity, boolean isLandScape) {

        Bundle bundle = new Bundle();

        bundle.putBoolean(ProtocolKeys.IS_SCREEN_ORIENTATION_LANDSCAPE, isLandScape);

        bundle.putInt(ProtocolKeys.FUNCTION_CODE, ProtocolConfigs.FUNC_CODE_BIND_PHONE_NUM);

        Intent intent = new Intent(activity, ContainerActivity.class);
        intent.putExtras(bundle);

        return intent;
    }

    /***
     * 生成浮窗设置接口的Intent
     * 
     * @return Intent
     */
    private Intent getSettingIntent(Activity activity, boolean isLandScape) {

        Bundle bundle = new Bundle();

        // 界面相关参数，360SDK界面是否以横屏显示。
        bundle.putBoolean(ProtocolKeys.IS_SCREEN_ORIENTATION_LANDSCAPE, isLandScape);

        bundle.putInt(ProtocolKeys.FUNCTION_CODE, ProtocolConfigs.FUNC_CODE_SETTINGS);

        Intent intent = new Intent(activity, ContainerActivity.class);
        intent.putExtras(bundle);

        return intent;
    }

    /**
     * 生成截屏发帖的Intent
     * 
     * @param isLandScape
     * @return
     */
    private Intent getBBSPostIntent(Activity activity, boolean isLandScape) {

        Bundle bundle = new Bundle();

        // 界面相关参数，360SDK界面是否以横屏显示。
        bundle.putBoolean(ProtocolKeys.IS_SCREEN_ORIENTATION_LANDSCAPE, isLandScape);

        // 此处可以传入您的截屏路径
        bundle.putString(ProtocolKeys.BBS_POST_EXTRA_SNAP_PATH,
                Environment.getExternalStorageDirectory() + "/DCIM/screenshot/20130621152522.png");

        bundle.putInt(ProtocolKeys.FUNCTION_CODE, ProtocolConfigs.FUNC_CODE_BBS);

        Intent intent = new Intent(activity, ContainerActivity.class);
        intent.putExtras(bundle);

        return intent;
    }

    // ---------------------------------360SDK接口的回调-----------------------------------

    // 登录、注册的回调
    private IDispatcherCallback mLoginCallback = new IDispatcherCallback() {

        @Override
        public void onFinished(String data) {
            Log.d(TAG, "mLoginCallback, data is " + data);

            // 解析User info，这里没有用户信息，需要单独从服务器获取
            //QihooUserInfo info = parseUserInfoFromLoginResult(data);
            //onGotUserInfo(info, data);
            
            // 解析access_token
            String token = parseAccessTokenFromLoginResult(data);
            if(token!=null)
            	onGotToken(token);
        }
    };

    // 切换账号的回调
    private IDispatcherCallback mAccountSwitchCallback = new IDispatcherCallback() {

        @Override
        public void onFinished(String data) {
            Log.d(TAG, "mAccountSwitchCallback, data is " + data);

            // 解析User info，这里获取到的user info 没有qid，需要单独从服务器获取
            QihooUserInfo info = parseUserInfoFromLoginResult(data);
            // 解析access_token
            String authorizationCode = parseAccessTokenFromLoginResult(data);

            onGotToken(authorizationCode);
        }
    };

    // 支付的回调
    protected IDispatcherCallback mPayCallback = new IDispatcherCallback() {

        @Override
        public void onFinished(String data) {
            Log.d(TAG, "mPayCallback");
            Log.d(TAG, data);
            boolean isCallbackParseOk = false;
            int errorCode = 1;
            JSONObject jsonRes;
            try {
                jsonRes = new JSONObject(data);
                // error_code 状态码： 0 支付成功， -1 支付取消， 1 支付失败， -2 支付进行中。
                // error_msg 状态描述
                errorCode = jsonRes.getInt("error_code");
                switch (errorCode) {
                    case 0:
                    case 1:
                    case -1:
                    case -2: {
                        String errorMsg = jsonRes.getString("error_msg");
                        //Log.d(TAG, "pay call back, errorCode" + errorCode + " msg "+ errorMsg);
                        isCallbackParseOk = true;
                    }
                        break;
                    default:
                        break;
                }
            } catch (JSONException e) {
            	Log.d(TAG, "解析json失败！");
            	Log.d(TAG, e.getMessage());
            }
            
            //xk 通知unity结果
            mainActivity.onPayFinish(errorCode);

            // 用于测试数据格式是否异常。
//            if (!isCallbackParseOk) {
//                Toast.makeText(SdkUserBaseActivity.activity, getString(R.string.data_format_error),
//                        Toast.LENGTH_LONG).show();
//            }
        }
    };

    // 实名注册的回调
    private IDispatcherCallback mRealNameRegisterCallback = new IDispatcherCallback() {

        @Override
        public void onFinished(String data) {
            //Log.d(TAG, "mRealNameRegisterCallback, data is " + data);
        }
    };

    // ----------------------------------------------------------------
    private boolean mShouldGameKillProcessExit = false;

    // 退出的回调
    private IDispatcherCallback mQuitCallback = new IDispatcherCallback() {

        @Override
        public void onFinished(String data) {
            Log.d(TAG, "mQuitCallback, data is " + data);
            JSONObject json;
            try {
                json = new JSONObject(data);
                int which = json.optInt("which", -1);
                onQuitError(which);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    // ----------------------------------------------------------------

    private IDispatcherCallback mBindPhoneNumCallback = new IDispatcherCallback() {

        @Override
        public void onFinished(String data) {
            //Log.d(TAG, "mBindPhoneNumCallback, data is " + data);
        }
    };

    // -----------------------------------------防沉迷查询接口----------------------------------------

    /**
     * 本方法中的callback实现仅用于测试, 实际使用由游戏开发者自己处理
     * 
     * @param qihooUserId
     * @param accessToken
     */
    public void doSdkAntiAddictionQuery(Activity activity, String qihooUserId, String accessToken) {
        Intent intent = getAntiAddictionIntent(activity, qihooUserId, accessToken);
        Matrix.execute(activity, intent, new IDispatcherCallback() {

            @Override
            public void onFinished(String data) {
                //Log.d("demo,anti-addiction query result = ", data);
                if (!TextUtils.isEmpty(data)) {
                    try {
                        JSONObject resultJson = new JSONObject(data);
                        int errorCode = resultJson.getInt("error_code");
                        if (errorCode == 0) {
                            JSONObject contentData = resultJson.getJSONObject("content");
                            // 保存登录成功的用户名及密码
                            JSONArray retData = contentData.getJSONArray("ret");
                            Log.d(TAG, "ret data = " + retData);
                            int status = retData.getJSONObject(0).getInt("status");
                            Log.d(TAG, "status = " + status);
                            if (status == 0) {
                                Log.d(TAG, "doSDKAntiAddictionQuery call back status=0");
                            } else if (status == 1) {
                                Log.d(TAG, "doSDKAntiAddictionQuery call back status=1");
                            } else if (status == 2) {
                                Log.d(TAG, "doSDKAntiAddictionQuery call back status=2");
                            }
                        } else {
                            Log.d(TAG, "doSDKAntiAddictionQuery call back" + "error_msg");
                        }

                    } catch (JSONException e) {
                    	Log.d(TAG, "doSDKAntiAddictionQuery call back JSONException");
                        e.printStackTrace();

                    }
                }
            }
        });
    }

    /**
     * 生成防沉迷查询接口的Intent参数
     * 
     * @param qihooUserId
     * @param accessToken
     * @return Intent
     */
    private Intent getAntiAddictionIntent(Activity activity, String qihooUserId, String accessToken) {

        Bundle bundle = new Bundle();

        // 必需参数，用户access token，要使用注意过期和刷新问题，最大64字符。
        bundle.putString(ProtocolKeys.ACCESS_TOKEN, accessToken);

        // 必需参数，360账号id，整数。
        bundle.putString(ProtocolKeys.QIHOO_USER_ID, qihooUserId);

        // 必需参数，使用360SDK的防沉迷查询模块。
        bundle.putInt(ProtocolKeys.FUNCTION_CODE, ProtocolConfigs.FUNC_CODE_ANTI_ADDICTION_QUERY);

        Intent intent = new Intent(activity, ContainerActivity.class);
        intent.putExtras(bundle);

        return intent;
    }

    /***
     * 生成调用360SDK论坛接口的Intent
     * 
     * @param isLandScape
     * @return Intent
     */
    private Intent getBBSIntent(Activity activity, boolean isLandScape) {

        Bundle bundle = new Bundle();

        // 界面相关参数，360SDK界面是否以横屏显示。
        bundle.putBoolean(ProtocolKeys.IS_SCREEN_ORIENTATION_LANDSCAPE, isLandScape);

        // 必需参数，使用360SDK的论坛模块。
        bundle.putInt(ProtocolKeys.FUNCTION_CODE, ProtocolConfigs.FUNC_CODE_BBS);

        Intent intent = new Intent(activity, ContainerActivity.class);
        intent.putExtras(bundle);

        return intent;
    }

    /***
     * 生成调用360SDK客服接口的Intent
     * 
     * @param isLandScape
     * @return Intent
     */
    private Intent getCustomerServiceIntent(Activity activity, boolean isLandScape) {

        Bundle bundle = new Bundle();

        // 界面相关参数，360SDK界面是否以横屏显示。
        bundle.putBoolean(ProtocolKeys.IS_SCREEN_ORIENTATION_LANDSCAPE, isLandScape);

        // 必需参数，使用360SDK的论坛模块。
        bundle.putInt(ProtocolKeys.FUNCTION_CODE, ProtocolConfigs.FUNC_CODE_CUSTOMER_SERVICE);

        Intent intent = new Intent(activity, ContainerActivity.class);
        intent.putExtras(bundle);

        return intent;
    }
    
    public GameMainInterface mainActivity;

	public void onGotToken(String token) 
	{
		mainActivity.onGotToken(token);
	}
	
	public void onGotUserInfo(QihooUserInfo userInfo, String json)
	{
		mainActivity.onGotUserInfo(userInfo,json);
	}

    public void onQuitError(int errCode) {
        Log.d(TAG, "onGotError : " + errCode);
        if(errCode == 0)
        {
        	//xk
        	//nativeonQuitGame(errCode);
        }
        else
        {
        	Log.d(TAG, "system.exit()");
        	
        	
        	Log.d(TAG, "system.exit2()");
        }
    }

    private QihooUserInfo parseUserInfoFromLoginResult(String loginRes) {
        try {
            JSONObject joRes = new JSONObject(loginRes);
            JSONObject joData = joRes.getJSONObject("data");
            JSONObject joUserLogin = joData.getJSONObject("user_login_res");
            JSONObject joUserLoginData = joUserLogin.getJSONObject("data");
            JSONObject joAccessInfo = joUserLoginData.getJSONObject("accessinfo");
            JSONObject joUserMe = joAccessInfo.getJSONObject("user_me");
            return QihooUserInfo.parseUserInfo(joUserMe);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String parseAccessTokenFromLoginResult(String json) {
        try {

            JSONObject joRes = new JSONObject(json);
            JSONObject joData = joRes.getJSONObject("data");
            return joData.getString("access_token");
        } catch (Exception e) {
            Log.d(TAG, "parseAccessTokenFromLoginResult 异常！"+json);
        }
        return null;
    }

}
