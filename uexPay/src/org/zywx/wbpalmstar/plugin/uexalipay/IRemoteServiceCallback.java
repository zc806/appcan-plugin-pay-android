package org.zywx.wbpalmstar.plugin.uexalipay;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public abstract interface IRemoteServiceCallback extends IInterface {
	
	public abstract void startActivity(String paramString1, String paramString2, int paramInt, Bundle paramBundle)
			throws RemoteException;

	public static abstract class Stub extends Binder implements IRemoteServiceCallback {
		
		public static final String DESCRIPTOR = "com.alipay.android.app.IRemoteServiceCallback";
		static final int TRANSACTION_startActivity = 1;
		
		public Stub() {
			attachInterface(this, DESCRIPTOR);
		}

		public static IRemoteServiceCallback asInterface(IBinder obj) {
			if (obj == null) {
				return null;
			}
			IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
			if ((iin != null) && ((iin instanceof IRemoteServiceCallback))) {
				return (IRemoteServiceCallback) iin;
			}
			return new Proxy(obj);
		}

		public IBinder asBinder() {
			return this;
		}

		public boolean onTransact(int code, Parcel data, Parcel reply, int flags)
				throws RemoteException {
			switch (code) {
			case INTERFACE_TRANSACTION:
				reply.writeString(DESCRIPTOR);
				return true;
			case TRANSACTION_startActivity:
				data.enforceInterface(DESCRIPTOR);
				String arg0 = data.readString();
				String arg1 = data.readString();
				int arg2 = data.readInt();
				Bundle arg3;
				if (data.readInt() != 0) {
					arg3 = (Bundle) Bundle.CREATOR.createFromParcel(data);
				} else {
					arg3 = null;
				}
				startActivity(arg0, arg1, arg2, arg3);
				reply.writeNoException();
				return true;
			}

			return super.onTransact(code, data, reply, flags);
		}

		private static class Proxy implements IRemoteServiceCallback {
			private IBinder mRemote;

			Proxy(IBinder remote) {
				mRemote = remote;
			}

			public IBinder asBinder() {
				return mRemote;
			}

			public void startActivity(String packageName, String className,
					int iCallingPid, Bundle bundle) throws RemoteException {
				Parcel data = Parcel.obtain();
				Parcel reply = Parcel.obtain();
				try {
					data.writeInterfaceToken(DESCRIPTOR);
					data.writeString(packageName);
					data.writeString(className);
					data.writeInt(iCallingPid);
					if (bundle != null) {
						data.writeInt(1);
						bundle.writeToParcel(data, 0);
					} else {
						data.writeInt(0);
					}
					mRemote.transact(1, data, reply, 0);
					reply.readException();
				} finally {
					reply.recycle();
					data.recycle();
				}
			}
		}
	}
}