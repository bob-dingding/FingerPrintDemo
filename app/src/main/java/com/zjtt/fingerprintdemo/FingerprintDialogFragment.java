package com.zjtt.fingerprintdemo;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import javax.crypto.Cipher;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

/**
 * Created by HaiyuKing
 * Used
 */
public class FingerprintDialogFragment extends DialogFragment {
	private FingerprintManager fingerprintManager;

	private CancellationSignal mCancellationSignal;

	private Cipher mCipher;

	private LoginActivity mActivity;

	private TextView errorMsg;

	/**
	 * 标识是否是用户主动取消的认证。
	 */
	private boolean isSelfCancelled;

	public void setCipher(Cipher cipher) {
		mCipher = cipher;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		mActivity = (LoginActivity) getActivity();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			fingerprintManager = getContext().getSystemService(FingerprintManager.class);
		}
		setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fingerprint_dialog, container, false);
		errorMsg = v.findViewById(R.id.error_msg);
		TextView cancel = v.findViewById(R.id.cancel);
		cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
				stopListening();
			}
		});
		return v;
	}

	@Override
	public void onResume() {
		super.onResume();
		// 开始指纹认证监听
		startListening(mCipher);
	}

	@Override
	public void onPause() {
		super.onPause();
		// 停止指纹认证监听
		stopListening();
	}

	private void startListening(Cipher cipher) {
		isSelfCancelled = false;
		mCancellationSignal = new CancellationSignal();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			//第一个参数是CryptoObject对象，这里我们只需要将刚才传入的Cipher对象包装成CryptoObject对象就可以了
			//第二个参数是CancellationSignal对象，可以使用它来取消指纹认证操作
			//第三个参数是可选参数，官方的建议是直接传0就可以了。
			//第四个参数用于接收指纹认证的回调
			//第五个参数用于指定处理回调的Handler，这里直接传null表示回调到主线程即可
			fingerprintManager.authenticate(new FingerprintManager.CryptoObject(cipher), mCancellationSignal, 0, new FingerprintManager.AuthenticationCallback() {
				@Override
				public void onAuthenticationError(int errorCode, CharSequence errString) {
					if (!isSelfCancelled) {
						errorMsg.setText(errString);
						if (errorCode == FingerprintManager.FINGERPRINT_ERROR_LOCKOUT) {
							Toast.makeText(mActivity, errString, Toast.LENGTH_SHORT).show();
							dismiss();
						}
					}
				}

				@Override
				public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
					errorMsg.setText(helpString);
				}

				@Override
				public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
					Toast.makeText(mActivity, "指纹认证成功", Toast.LENGTH_SHORT).show();
					mActivity.onAuthenticated();
				}

				@Override
				public void onAuthenticationFailed() {
					errorMsg.setText("指纹认证失败，请再试一次");
				}
			}, null);
		}
	}

	private void stopListening() {
		if (mCancellationSignal != null) {
			mCancellationSignal.cancel();
			mCancellationSignal = null;
			isSelfCancelled = true;
		}
	}
}
