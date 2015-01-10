package org.zywx.wbpalmstar.plugin.uexalipay;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.base.BHtmlDecrypt;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

public class PFAlixpay {
	
	static final String PARTNER 	= "partner="; 
	static final String SELLER 		= "seller="; 
	static final String TRADE_NUM 	= "out_trade_no="; 
	static final String SUBJECT 	= "subject="; 
	static final String BODY 		= "body="; 
	static final String TOTAL_FEE 	= "total_fee=";
	static final String NOTIFY_URL 	= "notify_url=";
	static final String SIGN_TYPE 	= "sign_type=";
	static final String SIGN 		= "&sign=";
	static final String RSA 		= "RSA";
	private Context mContext;
	private PayConfig mPayConfig;
	
	private static PFAlixpay instance;
	
	private PFAlixpay(Context context){
		mContext = context;
	}
	
	public static PFAlixpay get(Context context){
		if(null == instance){
			instance = new PFAlixpay(context);
		}
		return instance;
	}
	
	public boolean pay(String inTradeNum, String inSubject, String inBody, String inTotalFee, Handler inCallBack, PayConfig payConfig){
		boolean ret = false;
		String submitInfo = "";
		try{
			String orderInfo = getOrderInfo(inTradeNum, inSubject, inBody, inTotalFee, payConfig);
			String signType = getSignType();
			String sign = sign(signType, orderInfo);
			sign = URLEncoder.encode(sign);
			submitInfo = orderInfo + SIGN + "\"" + sign + "\"&" + getSignType();
		}catch (Exception e) {
			Toast.makeText(mContext, "算法异常!", Toast.LENGTH_SHORT).show();
			return ret;
		}
		MobileSecurePayer msp = new MobileSecurePayer();
		ret = msp.pay(submitInfo, inCallBack, AlixId.RQF_PAY, (Activity)mContext);
		if (ret) {
			Toast.makeText(mContext, "正在支付", Toast.LENGTH_SHORT).show();
		} else
			;
		return ret;
	}
	
	private String getOrderInfo(String inTradeNum, String inSubject, String inBody, String inTotalFee, PayConfig payConfig){
		boolean isDebug = false;
		if(isDebug){
			inTradeNum = getTradeNum();
			inTotalFee = "0.01";
		}
		String orderInfo = PARTNER + "\"" + payConfig.mPartner + "\"";
		orderInfo += "&";
		orderInfo += SELLER + "\"" + payConfig.mSeller + "\"";
		orderInfo += "&";
		orderInfo += TRADE_NUM + "\"" + inTradeNum + "\"";
		orderInfo += "&";
		orderInfo += SUBJECT + "\"" + inSubject + "\"";
		orderInfo += "&";
		orderInfo += BODY + "\"" + inBody + "\"";
		orderInfo += "&";
		orderInfo += TOTAL_FEE + "\"" + inTotalFee + "\"";
		orderInfo += "&";
		orderInfo += NOTIFY_URL + "\"" + payConfig.mNotifyUrl + "\"";
		return orderInfo;
	}
	
	private String getSignType() {

		return SIGN_TYPE + "\"" + RSA + "\"";
	}
	
	private String sign(String signType, String content) {
		return Rsa.sign(content, mPayConfig.mRsaPrivate);
	}
	
	private String getTradeNum() { //use to test
		SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss");
		Date date = new Date();
		String strKey = format.format(date);
		java.util.Random r = new java.util.Random();
		strKey = strKey + r.nextInt();
		strKey = strKey.substring(0, 15);
		return strKey;
	}
	
	public boolean checkApp(){
		MobileSecurePayHelper mspHelper = new MobileSecurePayHelper(mContext);
		boolean isExist = mspHelper.detectPlugin(mPayConfig);
		if (!isExist){
			return false;
		}
		return true;
	}
	
	public PayConfig getPayConfig() {
		if (null == mPayConfig) {
			String url = "file:///android_asset/widget/wgtRes/payConfig.txt";
			try {
				String content = BHtmlDecrypt.decrypt(url, mContext, false, null);
				content = content.replaceAll("\\s*|\t|\r|\n", "");
				JSONObject json = string2JSON(content, ";");
				String partner = null;
				String seller = null;
				String rsaPrivate = null;
				String rsaPublic = null;
				String notifyUrl = null;
				String pluginName = null;
				try {partner = json.getString("partner");
				} catch (Exception e) {}
				
				try {seller = json.getString("seller");
				} catch (Exception e) {}

				try {rsaPrivate = json.getString("rsaPrivate");
				} catch (Exception e) {}

				try {rsaPublic = json.getString("rsaPublic");
				} catch (Exception e) {}

				try {notifyUrl = json.getString("notifyUrl");
				} catch (Exception e) {}

				try {pluginName = json.getString("pluginName");
				} catch (Exception e) {}
				mPayConfig = new PayConfig(partner, seller, rsaPrivate, rsaPublic, notifyUrl, pluginName);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return mPayConfig;
	}

	public void setPayConfig(PayConfig config) {
		mPayConfig = config;
	}

	private JSONObject string2JSON(String str, String split) throws JSONException {
		JSONObject json = new JSONObject();
		String[] arrStr = str.split(split);
		for (int i = 0; i < arrStr.length; i++) {
			String[] arrKeyValue = arrStr[i].split("=");
			json.put(arrKeyValue[0], arrStr[i].substring(arrKeyValue[0].length() + 1));
		}
		return json;
	}

}