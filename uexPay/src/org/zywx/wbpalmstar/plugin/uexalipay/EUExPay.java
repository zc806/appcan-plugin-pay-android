package org.zywx.wbpalmstar.plugin.uexalipay;

import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;
import org.zywx.wbpalmstar.engine.universalex.EUExCallback;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

public class EUExPay extends EUExBase{
	
	static final String onFunction = "uexPay.onStatus";
	static final String SCRIPT_HEADER = "javascript:";

	private PFPayCallBack m_eCallBack;
	private boolean m_paying;
	
	public EUExPay(Context context, EBrowserView inParent) {
		super(context, inParent);
	}

	/**
	 * @param inTradeNum 商家自己产生的无重复订单编号 ( 非空、64位字母、数字和下划线组成)
	 * @param inSubject  商品名称 (非空,64位字符,可包含中文字符)
	 * @param inBody	   商品描述 (非空,1024位字符,可包含中文字符)
	 * @param inTotalFee 商品总价格 (非空,大于0的数字,精度不超过两位小数)
	 */
	public void pay(String[] parm){
		if(parm.length < 4){
			return;
		}
		String inTradeNum = parm[0];
		String inSubject = parm[1]; 
		String inBody = parm[2]; 
		String inTotalFee = parm[3];
		//支付状态：0-成功,1-支付中,2-失败,3-支付插件不完整
		if(m_paying)return;
		m_paying = true;
		if(null == m_eCallBack){
			m_eCallBack = new PFPayCallBack();
		}
		try{
			PFAlixpay alipay = PFAlixpay.get(mContext);
			PayConfig config = alipay.getPayConfig();
			if(null == config){
				onCallback(SCRIPT_HEADER + "if(" + onFunction + "){" + onFunction + "(" + EUExCallback.F_C_PAYFAILED + ",'" + "config error!" + "');}");
				m_paying = false;
				return;
			}
			if(!alipay.checkApp()){
				m_paying = false;
				onCallback(SCRIPT_HEADER + "if(" + onFunction + "){" + onFunction + "(" + EUExCallback.F_C_PAYFAILED + ",'" + "支付插件不完整" + "');}");
				return;
			}
			boolean suc = alipay.pay(inTradeNum, inSubject, inBody, inTotalFee, m_eCallBack, config);
			String js = "";
			if(suc){
				js = SCRIPT_HEADER + "if(" + onFunction + "){" + onFunction + "(" + EUExCallback.F_C_PAYING + ",'" + "支付中" + "');}";
			}else{
				js = SCRIPT_HEADER + "if(" + onFunction + "){" + onFunction + "(" + EUExCallback.F_C_PayPLUGINERROR + ",'" + "配置错误" + "');}";
				m_paying = false;
			}
			onCallback(js);
		} catch(Exception e) {
			m_paying = false;
			errorCallback(0, 0, e.toString());
		}
	}
	
	/**
	 * @param inPartner 合作商户ID.用签约支付宝账号登录ms.alipay.com后，在账户信息页面获取.
	 * @param inSeller  账户ID.用签约支付宝账号登录ms.alipay.com后,在账户信息页面获取.
	 * @param inRsaPrivate	   商户（RSA）私钥.
	 * @param inRsaPublic 支付宝（RSA）公钥用签约支付宝账号登录ms.alipay.com后,在密钥管理页面获取.
	 * @param inNotifyUrl 商家提供的 url.订单支付结束时,支付宝服务端在回调手机客户端的同时,会回调这个url,通知商家本次支付的结果.
	 * 结果中包含支付结果详情和商家提供给支付宝的公钥信息,商家可用已有的私钥再进行验证此订单是否属于本商家,以保证支付的最大安全.
	 */
	public void setPayInfo(String[] parm){
		if(parm.length < 5){
			return;
		}
		PFAlixpay alipay = PFAlixpay.get(mContext);
		String inPartner = parm[0];
		String inSeller = parm[1];
		String inRsaPrivate = parm[2]; 
		String inRsaPublic = parm[3]; 
		String inNotifyUrl = parm[4];
		PayConfig congfig = new PayConfig(inPartner, inSeller, inRsaPrivate, inRsaPublic, inNotifyUrl, null);
		alipay.setPayConfig(congfig);
	}
	
	private class PFPayCallBack extends Handler{
		@Override
		public void handleMessage(Message msg) {
			try {
				String strRet = (String) msg.obj;
				String js = "";
				switch (msg.what) {
					case AlixId.RQF_PAY: {
						try {
							ResultChecker resultChecker = new ResultChecker(strRet);
							String memo = resultChecker.getJSONResult().getString("memo").replace("{", "").replace("}", "");
							
							int retVal = resultChecker.checkSign(PFAlixpay.get(mContext).getPayConfig());
							if (retVal == ResultChecker.RESULT_CHECK_SIGN_FAILED) { //订单信息被非法篡改
								js = SCRIPT_HEADER + "if(" + onFunction + "){" + onFunction + "(" + EUExCallback.F_C_PAYFAILED + ",'" + "订单信息被非法篡改" + "');}";
								onCallback(js);
								return;
							} else {
								String code = (String) resultChecker.getJSONResult().get("resultStatus");
								int resultCode = Integer.valueOf(code.substring(1, code.length() - 1));
								switch (resultCode) {
									case 9000://支付成功
										if(resultChecker.isPayOk(PFAlixpay.get(mContext).getPayConfig())){
											js = SCRIPT_HEADER + "if(" + onFunction + "){" + onFunction + "(" + EUExCallback.F_C_PAYSUCCSS + ",'" + memo + "');}";
										}else{
											js = SCRIPT_HEADER + "if(" + onFunction + "){" + onFunction + "(" + EUExCallback.F_C_PAYSUCCSS + ",'" + "订单可能未支付成功,请联系支付宝公司进行确认" + "');}";
										}
										onCallback(js);
										break;
									case 4000://系统异常
									case 4001://数据格式不正确
									case 4003://该用户绑定的支付宝账户被冻结或不允许支付
									case 4004://该用户已解除绑定
									case 4005://绑定失败或没有绑定
									case 4006://订单支付失败
									case 4010://重新绑定账户
									case 6000://支付服务正在进行升级操作
									case 6001://用户中途取消支付操作
									case 6002://网络错误
										js = SCRIPT_HEADER + "if(" + onFunction + "){" + onFunction + "(" + EUExCallback.F_C_PAYFAILED + ",'" + memo + "');}";
										onCallback(js);
//										BaseHelper.showDialog(m_eContext, "提示", memo, android.R.drawable.ic_dialog_info);
										break;
									default:
										js = SCRIPT_HEADER + "if(" + onFunction + "){" + onFunction + "(" + EUExCallback.F_C_PAYFAILED + ",'" + memo + "');}";
										onCallback(js);
//										BaseHelper.showDialog(m_eContext, "提示", memo, android.R.drawable.ic_dialog_info);
										break;
									}
							}
						} catch (Exception e) { //异常 提示信息为 strRet
							e.printStackTrace();
							errorCallback(0, 0, e.toString() + "//" + strRet);
						}
					}
					m_paying = false;
					break;
				}
				super.handleMessage(msg);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean clean() {
		m_paying = false;
		if(null != m_eCallBack){
			m_eCallBack = null;
		}
		return true;
	}
	
}