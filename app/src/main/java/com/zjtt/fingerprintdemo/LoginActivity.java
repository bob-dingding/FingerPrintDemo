package com.zjtt.fingerprintdemo;

import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.biometrics.BiometricPrompt;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.provider.Settings;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.text.TextUtils;
import android.widget.Toast;

import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import androidx.appcompat.app.AppCompatActivity;


/**
 * Created by HaiyuKing
 * Used
 */
public class LoginActivity extends AppCompatActivity {
	private static final String DEFAULT_KEY_NAME = "default_key";

	/**
	 * 、 存储密匙：Android提供的这个KeyStore最大的作用就是不需要开发者去维护这个密匙的存储问题
	 * 相比起存储在用户的数据空间或者是外部存储器都更加安全
	 */
	KeyStore keyStore;

	/**
	 * 是否跳转到了设置界面设置了指纹
	 */
	Boolean isJumpSetting =false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		if (supportFingerprint()) {
			//第一步生成一个对称加密的Key
			initKey();
			//第二步生成一个Cipher对象，这都是Android指纹认证API要求的标准用法
			initCipher();
			//如果之前指纹且版本号大于等于9.0，采用新的接口方法
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
				fingerPrintNine();
			}else{
				showFingerPrintDialog(cipher);
			}

		}
	}


	@Override
	protected void onResume() {
		super.onResume();
         if(isJumpSetting){
			if (supportFingerprint()) {
				//第一步生成一个对称加密的Key
				initKey();
				//第二步生成一个Cipher对象，这都是Android指纹认证API要求的标准用法
				initCipher();
				//如果之前指纹且版本号大于等于9.0，采用新的接口方法
				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
					fingerPrintNine();
				}else{
					showFingerPrintDialog(cipher);
				}

			}
		 }
	}

	public boolean supportFingerprint() {
		if (Build.VERSION.SDK_INT < 23) {
			Toast.makeText(this, "您的系统版本过低，不支持指纹功能", Toast.LENGTH_SHORT).show();
			return false;
		} else {
			KeyguardManager keyguardManager = getSystemService(KeyguardManager.class);
			FingerprintManager fingerprintManager = getSystemService(FingerprintManager.class);
			if (!fingerprintManager.isHardwareDetected()) {
				Toast.makeText(this, "您的手机不支持指纹功能", Toast.LENGTH_SHORT).show();
				return false;
			} else if (!keyguardManager.isKeyguardSecure()) {
				Toast.makeText(this, "您还未设置锁屏，请先设置锁屏并添加一个指纹", Toast.LENGTH_SHORT).show();

				return false;
			} else if (!fingerprintManager.hasEnrolledFingerprints()) {
				Toast.makeText(this, "您至少需要在系统设置中添加一个指纹", Toast.LENGTH_SHORT).show();
				//获得当前手机品牌，如HONOR
				 String BRAND = android.os.Build.BRAND.toUpperCase();
				String pcgName = null;
				String clsName = null;
				 if("sony".toUpperCase().indexOf(BRAND) >= 0){
					 pcgName = "com.android.settings";
					 clsName = "com.android.settings.Settings$FingerprintEnrollSuggestionActivity";
				 }else if("oppo".toUpperCase().indexOf(BRAND) >= 0){
					 pcgName = "com.coloros.fingerprint";
					 clsName = "com.coloros.fingerprint.FingerLockActivity";
				 }else if("huawei".toUpperCase().indexOf(BRAND) >= 0){
					 pcgName = "com.android.settings";
					 clsName = "com.android.settings.fingerprint.FingerprintSettingsActivity";
				}else if("honor".toUpperCase().indexOf(BRAND) >= 0){
					 pcgName = "com.android.settings";
					 clsName = "com.android.settings.fingerprint.FingerprintSettingsActivity";
				}else{
				 	if(!isJumpSetting){
						Intent intent = new Intent(Settings.ACTION_SETTINGS);
						startActivity(intent);
						isJumpSetting =true;
					}

				 }
				if (!TextUtils.isEmpty(pcgName) && !TextUtils.isEmpty(clsName)) {
					if (!isJumpSetting) {
						Intent intent = new Intent();
						ComponentName componentName = new ComponentName(pcgName, clsName);
						intent.setAction(Intent.ACTION_VIEW);
						intent.setComponent(componentName);
						startActivity(intent);
						isJumpSetting = true;
					}
				}
				return false;
			}
		}
		return true;
	}

	@TargetApi(23)
	private void initKey() {
		try {
			//获取秘钥库对象
			keyStore = KeyStore.getInstance("AndroidKeyStore");
			keyStore.load(null);
			//生成新密钥，请使用 KeyGenerator 和 KeyGenParameterSpec
			KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
			KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(DEFAULT_KEY_NAME,
					KeyProperties.PURPOSE_ENCRYPT |
							KeyProperties.PURPOSE_DECRYPT)
					.setBlockModes(KeyProperties.BLOCK_MODE_CBC)
					.setUserAuthenticationRequired(true)
					.setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7);
			keyGenerator.init(builder.build());
			keyGenerator.generateKey();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@TargetApi(23)
	Cipher cipher;
	private void initCipher() {
		try {
			SecretKey key = (SecretKey) keyStore.getKey(DEFAULT_KEY_NAME, null);
			 cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
					+ KeyProperties.BLOCK_MODE_CBC + "/"
					+ KeyProperties.ENCRYPTION_PADDING_PKCS7);
			cipher.init(Cipher.ENCRYPT_MODE, key);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void showFingerPrintDialog(Cipher cipher) {
		FingerprintDialogFragment fragment = new FingerprintDialogFragment();
		fragment.setCipher(cipher);
		fragment.show(getSupportFragmentManager(), "fingerprint");
	}

	public void onAuthenticated() {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		finish();
	}


	/**
	 * 9.0实现指纹验证的方法
	 */

	public void fingerPrintNine (){
		//fingerprintManager将被BiometricPrompt类替换
		final CancellationSignal mCancellationSignal =new CancellationSignal();



		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
			BiometricPrompt biometricPrompt =new BiometricPrompt.Builder(LoginActivity.this).setTitle("指纹验证").setDescription("9.0指纹验证").setSubtitle("指纹").setNegativeButton("密码", getMainExecutor(), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//用户选择了取消或者采取密码登录
					mCancellationSignal.cancel();
				}
			}).build();
			biometricPrompt.authenticate(new BiometricPrompt.CryptoObject(cipher),mCancellationSignal, getMainExecutor(), new BiometricPrompt.AuthenticationCallback() {
				@Override
				public void onAuthenticationError(int errorCode, CharSequence errString) {
					super.onAuthenticationError(errorCode, errString);
					Toast.makeText(LoginActivity.this,errString,Toast.LENGTH_LONG).show();
					mCancellationSignal.cancel();
				}

				@Override
				public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
					super.onAuthenticationHelp(helpCode, helpString);
					Toast.makeText(LoginActivity.this,helpString,Toast.LENGTH_LONG).show();
				}

				@Override
				public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
					super.onAuthenticationSucceeded(result);
					Toast.makeText(LoginActivity.this,"指纹验证成功",Toast.LENGTH_LONG).show();
					mCancellationSignal.cancel();
				}

				@Override
				public void onAuthenticationFailed() {
					super.onAuthenticationFailed();
					Toast.makeText(LoginActivity.this,"纹认证失败，请再试一次",Toast.LENGTH_LONG).show();

				}
			});
		}
	}
}
