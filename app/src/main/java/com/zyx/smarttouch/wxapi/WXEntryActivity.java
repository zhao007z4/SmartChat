package com.zyx.smarttouch.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import com.zyx.smarttouch.R;
import com.zyx.smarttouch.gui.EntryActivity;


public class WXEntryActivity extends Activity implements IWXAPIEventHandler{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		IWXAPI api = WXAPIFactory.createWXAPI(this, EntryActivity.AppId, false);
		api.handleIntent(getIntent(),this);
		finish();
	}

	@Override
	public void onReq(BaseReq baseReq) {

	}

	@Override
	public void onResp(BaseResp baseResp) {
		String result;
		switch (baseResp.errCode) {
			case BaseResp.ErrCode.ERR_OK:
				result = "分享成功";
				break;
			case BaseResp.ErrCode.ERR_USER_CANCEL:
				result = null;
				break;
			default:
				result = "分享失败";
				break;
		}
		if (result != null) {
			Toast.makeText(this, baseResp.errCode + result, Toast.LENGTH_SHORT).show();
		}
	}
}