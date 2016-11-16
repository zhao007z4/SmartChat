package com.zyx.smarttouch.gui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXVideoObject;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.zyx.smarttouch.Config;
import com.zyx.smarttouch.Model.SnsInfo;
import com.zyx.smarttouch.R;
import com.zyx.smarttouch.SnsStat;
import com.zyx.smarttouch.Task;
import com.zyx.smarttouch.common.Share;

import net.youmi.android.AdManager;
import net.youmi.android.normal.banner.BannerManager;
import net.youmi.android.normal.banner.BannerViewListener;
import net.youmi.android.onlineconfig.OnlineConfigCallBack;

import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by corous360 on 13/8/16.
 */
public class EntryActivity extends AppCompatActivity {

    public static boolean snsListUpdated = false;
    private IWXAPI wxApi;
    Task task = null;
    SnsStat snsStat = null;
    private SnsInfoAdapter adapter;
    private ArrayList<SnsInfo> lsSnsInf=null ;
    public static final String AppId = "wxe763dee50e64d501";
    public static final String AppSecret ="216e2e155e14c9ece5097dd2cb77ed2f";

    private static final String Adkey="isOpen";


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sync_menu_btn:
                new RunningTask().execute();
                return true;
            case R.id.entry_menu_btn:
                try {
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI"));
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }catch (Exception e)
                {
                    Toast.makeText(EntryActivity.this,R.string.STR_ENTRY_WECHAT_ERROR,Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
                break;
            case android.R.id.home:
            {
                finish();
                return true;
            }
            default:
                break;
//            case R.id.export_confirm_btn:
//                exportSelectedSns();
//                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sns_menu, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        task = new Task(this.getApplicationContext());
        setContentView(R.layout.sns_list);
        task.testRoot();


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .defaultDisplayImageOptions(defaultOptions)
                .build();
        ImageLoader.getInstance().init(config);

        //updateSnsList();

        wxApi = WXAPIFactory.createWXAPI(this, AppId,true);
        wxApi.registerApp(AppId);

        File file = new File(Config.EXT_DIR,"all_sns.json");
        if(file.exists())
        {
            RunningGetDataTask runningGetDataTask = new RunningGetDataTask();
            runningGetDataTask.execute();
        }
        else
        {
            new RunningTask().execute();
        }

        if(SnsApp.mInstance.bShowAd && SnsApp.mInstance.bInit) {
            setupBannerAd();
        }else {

            AdManager.getInstance(this).asyncGetOnlineConfig(Adkey, new OnlineConfigCallBack() {
                @Override
                public void onGetOnlineConfigSuccessful(String key, String value) {
                    // TODO Auto-generated method stub
                    // 获取在线参数成功

                    Log.i("B4A","asyncGetOnlineConfig key:"+key+" value:"+value);

                    if (key.equals(Adkey) && value.equals("1")) {
                        SnsApp.mInstance.bShowAd = true;
                        File file = new File(getFilesDir(), SnsApp.AdName);
                        if (!file.exists()) {
                            try {
                                file.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                @Override
                public void onGetOnlineConfigFailed(String key) {
                    // TODO Auto-generated method stub
                    // 获取在线参数失败，可能原因有：键值未设置或为空、网络异常、服务器异常
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    protected void updateSnsList() {

        if(adapter==null) {
            ListView snsListView = (ListView) findViewById(R.id.sns_list_view);
            SnsInfoAdapter adapter = new SnsInfoAdapter(this, R.layout.sns_item, lsSnsInf, new SnsInfoAdapter.OnDownloadListen() {
                @Override
                public void download(String sUrl, String sDir, String sName) {
                    downloadFile(sUrl, sDir, sName);
                }

                @Override
                public void shareVideo(String sUrl, String sContent,String sPath) {
                    wechatShareVideo(sUrl, sContent,sPath);
                }

                @Override
                public void shareURL(String sUrl, String sTitle, String sPath) {
                    wechatShareUrl(sUrl,sTitle,sPath);
                }
            });
            snsListView.setAdapter(adapter);
        }else
        {
            adapter.clear();
            adapter.addAll(lsSnsInf);
            adapter.notifyDataSetChanged();
        }
    }


    public void downloadFile(String sUrl,String sDir,String sName)
    {
        OkHttpUtils//
                .get()//
                .url(sUrl)//
                .build()//
                .execute(new FileCallBack(sDir, sName)//
                {

                    @Override
                    public void onBefore(Request request, int id)
                    {

                    }

                    @Override
                    public void inProgress(float progress, long total, int id)
                    {
                        Log.e(Config.TAG, "inProgress :" + (int) (100 * progress));
                    }

                    @Override
                    public void onError(Call call, Exception e, int id)
                    {
                        Log.e(Config.TAG, "onError :" + e.getMessage());
                    }

                    @Override
                    public void onResponse(File file, int id)
                    {
                        Log.e(Config.TAG, "onResponse :" + file.getAbsolutePath());
                    }
                });
    }

    private static final int THUMB_SIZE = 150;

    private byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }
        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static Bitmap LoadBitmapSample(File file, int MaxWidth, int MaxHeight) throws IOException
    {

        if (MaxWidth == 0 || MaxHeight == 0)
        {
            return null;
        }

        FileInputStream inputStream;
        inputStream = new FileInputStream(file);
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, o);
        inputStream.close();
        inputStream = null;

        double r1 =Math.ceil((float)o.outWidth / (float)MaxWidth);
        r1 = Math.max(r1, Math.ceil((float)o.outHeight / (float)MaxHeight));
        BitmapFactory.Options o2 = null;
        if (r1 > 1.0F)
        {
            o2 = new BitmapFactory.Options();
            o2.inSampleSize = (int) r1;
        }

        Bitmap bmp = null;
        boolean oomFlag = false;

        int retries = 0;
        do
        {
            try
            {
                inputStream = new FileInputStream(file);
                bmp = BitmapFactory.decodeStream(inputStream, null, o2);
                inputStream.close();
                inputStream = null;
                break;
            }
            catch (OutOfMemoryError oom)
            {
                if (inputStream != null)
                {
                    inputStream.close();
                }
                inputStream = null;

                System.gc();
                if (o2 == null)
                {
                    o2 = new BitmapFactory.Options();
                    o2.inSampleSize = 1;
                }
                o2.inSampleSize *= 2;
                oomFlag = true;
                retries++;
            }

        }
        while (retries < 5);

        if (bmp == null)
        {
            return null;
        }
        return bmp;
    }

    private void wechatShareUrl(String sUrl,String sContent,String sPath)
    {
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = sUrl;
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = sContent;
        msg.description = sContent;

        Bitmap thumb = null;
        try {
            thumb = LoadBitmapSample(new File(sPath),300,300);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this,"分享失败",Toast.LENGTH_LONG).show();
            return;
        }

        msg.thumbData = bmpToByteArray(thumb, true);
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = String.valueOf(System.currentTimeMillis());
        req.message = msg;
        req.scene = SendMessageToWX.Req.WXSceneTimeline;
        wxApi.sendReq(req);
    }

    private void wechatShareVideo(String sUrl,String sContent,String sPath)
    {
        WXVideoObject videoObject = new WXVideoObject();
        videoObject.videoUrl =sUrl;

        WXMediaMessage msg = new WXMediaMessage((videoObject));
        msg.title =sContent;
        msg.description =sContent;
        msg.messageExt = sContent;
        Bitmap thumb = BitmapFactory.decodeResource(getResources(),R.drawable.empty_background);
        Bitmap thumbBitmap =  Bitmap.createScaledBitmap(thumb, THUMB_SIZE, THUMB_SIZE, true);
        thumb.recycle();
        msg.thumbData = bmpToByteArray(thumbBitmap, true);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = String.valueOf(System.currentTimeMillis());
        req.message = msg;
        req.scene = SendMessageToWX.Req.WXSceneTimeline;
        wxApi.sendReq(req);
    }

    class RunningTask extends AsyncTask<Void, Void, Void> {

        Throwable error = null;
        private ProgressDialog progressDialog = null;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(EntryActivity.this,"","正在同步中...",true,false);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                task.copySnsDB();
                task.initSnsReader();
                task.snsReader.run();
                //snsStat = new SnsStat(task.snsReader.getSnsList());
            } catch (Throwable e) {
                this.error = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void voidParam) {
            super.onPostExecute(voidParam);
            progressDialog.dismiss();
            if (this.error != null) {
                Toast.makeText(EntryActivity.this, R.string.not_rooted, Toast.LENGTH_LONG).show();
                Log.e(Config.TAG, "exception", this.error);
                return;
            }
            //Share.snsData = snsStat;
            lsSnsInf = task.snsReader.getSnsList();
            updateSnsList();
        }
    }

    class RunningGetDataTask extends AsyncTask<Void, Void, Void> {

        ArrayList<SnsInfo> lsTmp = null;

        private ProgressDialog progressDialog = null;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(EntryActivity.this,"","正在加载中...",true,false);
        }

        @Override
        protected Void doInBackground(Void... params) {
            lsTmp = getData();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void voidParam) {
            super.onPostExecute(voidParam);
            progressDialog.dismiss();
            if(lsTmp!=null) {
                lsSnsInf = lsTmp;
                updateSnsList();
            }
        }
    }

    private ArrayList<SnsInfo> getData()
    {
        File file = new File(Config.EXT_DIR,"all_sns.json");
        if(file.exists())
        {
            String sData = ReadString(file);
            if(sData==null)
            {
                return null;
            }
            try {
                return parseJson(sData);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private String ReadString(File file)
    {
        FileInputStream fin=null;
        try
        {
            fin = new FileInputStream(file);
            int iLen =(int)file.length();
            byte[] buffer = new byte[iLen];

            int iHaveRead = 0;
            int iReadLen = 0;

            while (true)
            {
                iReadLen = fin.read(buffer, iHaveRead, iLen-iHaveRead);
                if(iReadLen==-1 || iHaveRead+iReadLen==iLen)
                {
                    break;
                }
                iHaveRead += iReadLen;
            }

            fin.close();
            fin = null;

            String sDate = new String(buffer,"UTF-8");
            return sDate;
        }
        catch(FileNotFoundException e1)
        {
            e1.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if(fin !=null)
            {
                try
                {
                    fin.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    public static String getJsonString(JSONObject jsonObject, String sName) {
        if (jsonObject == null || sName == null || !jsonObject.has(sName) || jsonObject.isNull(sName)) {
            return null;
        }
        try {
            return jsonObject.getString(sName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Long getJsonLong(JSONObject jsonObject, String sName,long iDefault) {
        if (jsonObject == null || sName == null || !jsonObject.has(sName) || jsonObject.isNull(sName)) {
            return iDefault;
        }
        try {
            return jsonObject.getLong(sName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return iDefault;
    }

    public static boolean getJsonBoolean(JSONObject jsonObject, String sName,boolean bDefault) {
        if (jsonObject == null || sName == null || !jsonObject.has(sName) || jsonObject.isNull(sName)) {
            return bDefault;
        }
        try {
            return jsonObject.getBoolean(sName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bDefault;
    }

    public static String getContent(String sContent,String sStart,String sEnd)
    {
//        int index = sContent.indexOf("<contentStyle><![CDATA[");
//        int iEndIndex = sContent.indexOf("]]></contentStyle>");

        int index = sContent.indexOf(sStart);
        if(index >=0) {
            int iEndIndex = sContent.indexOf(sEnd);
            if(iEndIndex>=0)
            {
                return sContent.substring(index+sStart.length(),iEndIndex);
            }
        }
        return null;
    }

    public ArrayList<SnsInfo> parseJson(String sData) throws JSONException {

        ArrayList<SnsInfo> snsInfos = null;
        JSONArray jsonArray = new JSONArray(sData);
        int iCount = jsonArray.length();
        snsInfos = new ArrayList<SnsInfo>();
        if(iCount<0)
        {
            return snsInfos;
        }

        JSONObject jsonObject = null;
        SnsInfo snsInfo = null;
        JSONArray jsonArrayChild = null;
        JSONObject jsonObjectChild=null;
        SnsInfo.Like like =null;
        SnsInfo.Comment comment= null;
        for(int i=0;i<iCount;i++)
        {
            jsonObject = jsonArray.getJSONObject(i);
            snsInfo = new SnsInfo();
            snsInfo.timestamp= getJsonLong(jsonObject,"timestamp",0);
            snsInfo.rawXML = getJsonString(jsonObject,"rawXML");

            String sType = getContent(snsInfo.rawXML,"<contentStyle><![CDATA[","]]></contentStyle>");
            if(sType !=null)
            {
                try {
                    snsInfo.iType = Integer.parseInt(sType);
                }catch (NumberFormatException e)
                {
                    e.printStackTrace();
                }
            }
            snsInfo.title = getContent(snsInfo.rawXML,"<title><![CDATA[","]]></title>");
            snsInfo.contentUrl = getContent(snsInfo.rawXML,"<contentUrl><![CDATA[","]]></contentUrl>");

            int j=0;
            snsInfo.mediaList = new ArrayList<String>();
            if(jsonObject.has("mediaList")) {
                jsonArrayChild = jsonObject.getJSONArray("mediaList");
                for (j = 0; j < jsonArrayChild.length(); j++) {
                    snsInfo.mediaList.add(jsonArrayChild.getString(j));
                }
            }

            snsInfo.likes = new ArrayList<SnsInfo.Like>();
            if(jsonObject.has("likes")) {
                jsonArrayChild = jsonObject.getJSONArray("likes");
                for (j = 0; j < jsonArrayChild.length(); j++) {
                    like = new SnsInfo.Like();
                    jsonObjectChild = jsonArrayChild.getJSONObject(j);
                    like.isCurrentUser = getJsonBoolean(jsonObjectChild, "isCurrentUser", false);
                    like.userName = getJsonString(jsonObjectChild, "userName");
                    like.userId = getJsonString(jsonObjectChild, "userId");
                }
            }

            snsInfo.comments = new ArrayList<SnsInfo.Comment>();
            if(jsonObject.has("comments")) {
                jsonArrayChild = jsonObject.getJSONArray("comments");
                for (j = 0; j < jsonArrayChild.length(); j++) {
                    comment = new SnsInfo.Comment();
                    jsonObjectChild = jsonArrayChild.getJSONObject(j);

                    comment.isCurrentUser = getJsonBoolean(jsonObjectChild, "isCurrentUser", false);
                    comment.authorName = getJsonString(jsonObjectChild, "authorName");
                    comment.authorId = getJsonString(jsonObjectChild, "authorId");
                    comment.content = getJsonString(jsonObjectChild, "content");
                    comment.toUser = getJsonString(jsonObjectChild, "toUserName");
                    comment.toUserId = getJsonString(jsonObjectChild, "toUserId");
                }
            }

            snsInfo.isCurrentUser = getJsonBoolean(jsonObject,"isCurrentUser",false);
            snsInfo.id = getJsonString(jsonObject,"snsId");
            snsInfo.authorName = getJsonString(jsonObject,"authorName");
            snsInfo.authorId =getJsonString(jsonObject,"authorId");
            snsInfo.content = getJsonString(jsonObject,"content");

            snsInfos.add(snsInfo);

            //Log.e("WeChatMomentStat","snsInfo snsInfo snsInfo:"+snsInfo.toString());
        }

        return snsInfos;
    }

    /**
    * 设置广告条广告
    */
    private void setupBannerAd() {
        /**
        * 普通布局
        */
        View bannerView = BannerManager.getInstance(this).getBannerView(new BannerViewListener() {
            @Override
            public void onRequestSuccess() {

               // Log.d("sns", "请求广告条成功");

            }

            @Override
            public void onSwitchBanner() {

             //   Log.d("sns", "广告条切换");
            }

            @Override
            public void onRequestFailed() {

             //   Log.d("sns", "请求广告条失败");
            }
        });
        LinearLayout bannerLayout = (LinearLayout) findViewById(R.id.lyad);
        bannerLayout.addView(bannerView);
    }

}
