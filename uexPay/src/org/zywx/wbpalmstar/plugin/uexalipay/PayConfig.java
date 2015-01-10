package org.zywx.wbpalmstar.plugin.uexalipay;

public class PayConfig {
	public String mPartner;
	public String mSeller;
	public String mRsaPrivate;
	public String mRsaPublic;
	public String mNotifyUrl;
	public String mPluginName;

	public PayConfig(String partner, String seller, String rsaPrivate, String rsaPublic, String notifyUrl, String pluginName){
		mPartner = partner;
		mSeller = seller;
		mRsaPrivate = rsaPrivate;
		mRsaPublic = rsaPublic;
		mNotifyUrl = notifyUrl;
		mPluginName = pluginName;
	}
}