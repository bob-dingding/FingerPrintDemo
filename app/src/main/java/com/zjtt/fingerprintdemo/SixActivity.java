package com.zjtt.fingerprintdemo;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.hardware.fingerprint.FingerprintManagerCompat;
import androidx.core.os.CancellationSignal;

/**
 * Created by HaiyuKing
 * Used
 */
public class SixActivity extends AppCompatActivity {
	private static final String TAG = "SixActivity";


	Handler handler = new Handler() {   //也可以置为null,系统自动处理
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 1:   //验证错误
					//todo 界面处理
					handleErrorCode(msg.arg1);
					break;
				case 2:   //验证成功
					//todo 界面处理
					Log.e(TAG, "handleMessage: "+ "验证成功");
					break;
				case 3:    //验证失败
					//todo 界面处理
					Log.e(TAG, "handleMessage: "+ "验证失败");
					break;
				default:
					super.handleMessage(msg);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_six);
		//v4包下的API，包装内部已经判断Android系统版本是否大于6.0，这也是官方推荐的方式
		FingerprintManagerCompat fingerprint = FingerprintManagerCompat.from(this);
		//Android 6.0包下的API
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
			FingerprintManager fingerprint2 = (FingerprintManager) getSystemService(Context.FINGERPRINT_SERVICE);
			// 判断设备是否支持指纹解锁
			if (fingerprint.isHardwareDetected()) {
				//判断设备是否以保存过指纹信息，至少需要保存过一个
				if (fingerprint.hasEnrolledFingerprints()) {
					/**
					 * @param crypto object associated with the call or null if none required.    //不太理解，加密指纹特征还是什么，可以不加密置为null
					 * @param flags optional flags; should be 0  //设置标记，暂时无用
					 * @param cancel an object that can be used to cancel authentication     //取消验证
					 * @param callback an object to receive authentication events   //系统认证完成之后，回调该接口
					 * @param handler an optional handler for events  //处理callback接口后，界面的处理，默认是主线程handler
					 */
					//fingerprint.authenticate(crypto, flags,cancel,callback,handler); //验证指纹

					//再来说明各个参数的实现
					FingerprintManagerCompat.CryptoObject crypto = null;
					int flags = 0;
					CancellationSignal cancel = new CancellationSignal();
					FingerprintManagerCompat.AuthenticationCallback callback = new FingerprintManagerCompat.AuthenticationCallback() {
						@Override
						public void onAuthenticationError(int errMsgId, CharSequence errString) {
							super.onAuthenticationError(errMsgId, errString);
							//验证错误时，回调该方法。当连续验证5次错误时，将会走onAuthenticationFailed()方法
							handler.obtainMessage(1, errMsgId, 0).sendToTarget();
						}

						@Override
						public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
							super.onAuthenticationSucceeded(result);
							//验证成功时，回调该方法。fingerprint对象不能再验证
							handler.obtainMessage(2).sendToTarget();
						}

						@Override
						public void onAuthenticationFailed() {
							super.onAuthenticationFailed();
							//验证失败时，回调该方法。fingerprint对象不能再验证并且需要等待一段时间才能重新创建指纹管理对象进行验证
							handler.obtainMessage(3).sendToTarget();
						}
					};

					fingerprint.authenticate(crypto, flags, cancel, callback, handler); //验证指纹


				}

			}

		}
	}


	/**
	 * 对应不同的错误，可以有不同的操作
	 */
	private void handleErrorCode(int code) {
		switch (code) {
			case FingerprintManager.FINGERPRINT_ERROR_CANCELED:
				//todo 指纹传感器不可用，该操作被取消
				break;
			case FingerprintManager.FINGERPRINT_ERROR_HW_UNAVAILABLE:
				//todo 当前设备不可用，请稍后再试
				break;
			case FingerprintManager.FINGERPRINT_ERROR_LOCKOUT:
				//todo 由于太多次尝试失败导致被锁，该操作被取消
				break;
			case FingerprintManager.FINGERPRINT_ERROR_NO_SPACE:
				//todo 没有足够的存储空间保存这次操作，该操作不能完成
				break;
			case FingerprintManager.FINGERPRINT_ERROR_TIMEOUT:
				//todo 操作时间太长，一般为30秒
				break;
			case FingerprintManager.FINGERPRINT_ERROR_UNABLE_TO_PROCESS:
				//todo 传感器不能处理当前指纹图片
				break;
		}
	}
}
