package org.zywx.wbpalmstar.plugin.uexalipay;

import org.zywx.wbpalmstar.plugin.uexalipay.IAlixPay;
import org.zywx.wbpalmstar.plugin.uexalipay.IRemoteServiceCallback;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;

public class MobileSecurePayer {

	Integer lock = 0;
	IAlixPay mAlixPay;
	boolean mbPaying;
	Activity mActivity;

	private ServiceConnection mAlixPayConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			// wake up the binder to continue.
			synchronized (lock) {
				mAlixPay = IAlixPay.Stub.asInterface(service);
				lock.notify();
			}
		}
		public void onServiceDisconnected(ComponentName className) {
			mAlixPay = null;
		}
	};

	public boolean pay(final String strOrderInfo, final Handler callback,
			final int myWhat, final Activity activity) {
		if (mbPaying){
			return false;
		}
		mbPaying = true;
		mActivity = activity;
		// bind the service.
		if (mAlixPay == null) {
			mActivity.bindService(new Intent(IAlixPay.Stub.DESCRIPTOR), mAlixPayConnection, Context.BIND_AUTO_CREATE);
		}
		// else ok.
		new Thread(new Runnable() {
			public void run() {
				try {
					synchronized (lock) {
						if (mAlixPay == null)
							lock.wait();
					}
					// register a Callback for the service.
					mAlixPay.registerCallback(mCallback);
					// call the MobileSecurePay service.
					String strRet = mAlixPay.Pay(strOrderInfo);
					// set the flag to indicate that we have finished.
					// unregister the Callback, and unbind the service.
					mbPaying = false;
					mAlixPay.unregisterCallback(mCallback);
					mActivity.unbindService(mAlixPayConnection);
					// send the result back to caller.
					Message msg = new Message();
					msg.what = myWhat;
					msg.obj = strRet;
					callback.sendMessage(msg);
				} catch (Exception e) {
					e.printStackTrace();
					// send the result back to caller.
					Message msg = new Message();
					msg.what = myWhat;
					msg.obj = e.toString();
					callback.sendMessage(msg);
				}
			}
		}).start();

		return true;
	}


	private IRemoteServiceCallback mCallback = new IRemoteServiceCallback.Stub() {
		
		public void startActivity(String packageName, String className,
				int iCallingPid, Bundle bundle) throws RemoteException {
			Intent intent = new Intent(Intent.ACTION_MAIN, null);

			if (bundle == null){
				bundle = new Bundle();
			}

			try {
				bundle.putInt("CallingPid", iCallingPid);
				intent.putExtras(bundle);
			} catch (Exception e) {
				e.printStackTrace();
			}

			intent.setClassName(packageName, className);
			mActivity.startActivity(intent);
		}
	};
}