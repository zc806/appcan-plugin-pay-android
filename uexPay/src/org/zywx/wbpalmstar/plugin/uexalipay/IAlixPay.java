package org.zywx.wbpalmstar.plugin.uexalipay;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public abstract interface IAlixPay extends IInterface {
	
	public abstract String Pay(String paramString) throws RemoteException;
	public abstract String test() throws RemoteException;
	public abstract void registerCallback(IRemoteServiceCallback paramIRemoteServiceCallback)throws RemoteException;
	public abstract void unregisterCallback(IRemoteServiceCallback paramIRemoteServiceCallback)throws RemoteException;

	public static abstract class Stub extends Binder implements IAlixPay {
		public static final String DESCRIPTOR = "com.alipay.android.app.IAlixPay";
		static final int TRANSACTION_Pay = 1;
		static final int TRANSACTION_test = 2;
		static final int TRANSACTION_registerCallback = 3;
		static final int TRANSACTION_unregisterCallback = 4;

		public Stub() {
			attachInterface(this, DESCRIPTOR);
		}

		public static IAlixPay asInterface(IBinder obj) {
			if (obj == null) {
				return null;
			}
			IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
			if ((iin != null) && ((iin instanceof IAlixPay))) {
				return (IAlixPay) iin;
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
			case TRANSACTION_Pay:
				data.enforceInterface(DESCRIPTOR);
				String arg0 = data.readString();
				String result = Pay(arg0);
				reply.writeNoException();
				reply.writeString(result);
				return true;
			case TRANSACTION_test:
				data.enforceInterface(DESCRIPTOR);
				String result1 = test();
				reply.writeNoException();
				reply.writeString(result1);
				return true;
			case TRANSACTION_registerCallback:
				data.enforceInterface(DESCRIPTOR);
				IRemoteServiceCallback arg3 = IRemoteServiceCallback.Stub.asInterface(data.readStrongBinder());
				registerCallback(arg3);
				reply.writeNoException();
				return true;
			case TRANSACTION_unregisterCallback:
				data.enforceInterface(DESCRIPTOR);
				IRemoteServiceCallback arg4 = IRemoteServiceCallback.Stub.asInterface(data.readStrongBinder());
				unregisterCallback(arg4);
				reply.writeNoException();
				return true;
			}

			return super.onTransact(code, data, reply, flags);
		}

		private static class Proxy implements IAlixPay {
			private IBinder mRemote;

			Proxy(IBinder remote) {
				this.mRemote = remote;
			}

			public IBinder asBinder() {
				return this.mRemote;
			}

			public String Pay(String strInfo) throws RemoteException {
				Parcel data = Parcel.obtain();
				Parcel reply = Parcel.obtain();
				String result;
				try {
					data.writeInterfaceToken(DESCRIPTOR);
					data.writeString(strInfo);
					mRemote.transact(1, data, reply, 0);
					reply.readException();
					result = reply.readString();
				} finally {
					reply.recycle();
					data.recycle();
				}
				return result;
			}

			public String test() throws RemoteException {
				Parcel data = Parcel.obtain();
				Parcel reply = Parcel.obtain();
				String result;
				try {
					data.writeInterfaceToken(DESCRIPTOR);
					this.mRemote.transact(2, data, reply, 0);
					reply.readException();
					result = reply.readString();
				} finally {
					reply.recycle();
					data.recycle();
				}
				return result;
			}

			public void registerCallback(IRemoteServiceCallback cb) throws RemoteException {
				Parcel data = Parcel.obtain();
				Parcel reply = Parcel.obtain();
				try {
					data.writeInterfaceToken(DESCRIPTOR);
					data.writeStrongBinder(cb != null ? cb.asBinder() : null);
					mRemote.transact(3, data, reply, 0);
					reply.readException();
				} finally {
					reply.recycle();
					data.recycle();
				}
			}

			public void unregisterCallback(IRemoteServiceCallback cb) throws RemoteException {
				Parcel data = Parcel.obtain();
				Parcel reply = Parcel.obtain();
				try {
					data.writeInterfaceToken(DESCRIPTOR);
					data.writeStrongBinder(cb != null ? cb.asBinder() : null);
					mRemote.transact(4, data, reply, 0);
					reply.readException();
				} finally {
					reply.recycle();
					data.recycle();
				}
			}
		}
	}
}