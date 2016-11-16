package com.zyx.smarttouch.gui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.zyx.smarttouch.Config;
import com.zyx.smarttouch.R;
import com.zyx.smarttouch.Task;
import com.zyx.smarttouch.common.Aes;
import com.zyx.smarttouch.common.DateTime;
import com.zyx.smarttouch.database.SystemDB;

import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

/**
 * Created by 志勇 on 2016/8/16.
 */

public class ReplaceVideoActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String WX_ROOT = Environment.getExternalStorageDirectory() +"/tencent/MicroMsg/";
    private final static String WX_DATA = "/data/data/com.tencent.mm/MicroMsg/";
    private final static String WX_SHARED = "/data/data/com.tencent.mm/shared_prefs";
    private final static String WX_DRAFT = "/draft";
    private String sRoot = null;
    private String sDraft = null;
    private String sMicroMsg = null;
    private String sPassword = null;
    private final static int ACTIVITY_RESULT_LOCAL = 1;
    public static final String TAG = Config.TAG;
    private String sSrcPath = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.replace_video);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        if(intent !=null && intent.hasExtra("path"))
        {
            sSrcPath = intent.getStringExtra("path");
        }

        Button btnLoacal = (Button)findViewById(R.id.btnLocal);
        btnLoacal.setOnClickListener(this);

        Button btnFav = (Button)findViewById(R.id.btnFav);
        btnFav.setOnClickListener(this);

        if (!TextUtils.isEmpty(sSrcPath)) {
            btnLoacal.setText("执行");
            btnFav.setVisibility(View.GONE);
        }else{
            btnLoacal.setText("本地视频");
            btnFav.setVisibility(View.VISIBLE);
        }

        String sDir = getWxDir();
        if(sDir==null)
        {
            Toast.makeText(this,"未安装微信或未点击过朋友圈，请操作后重试",Toast.LENGTH_LONG).show();
            this.finish();
            return;
        }
        sRoot = WX_ROOT+sDir;
        sDraft = sRoot+WX_DRAFT;
        sMicroMsg = WX_DATA+sDir;

        String dataDir = Environment.getDataDirectory().getAbsolutePath();
        Process su = null;
        String sPath = WX_SHARED + "/auth_info_key_prefs.xml";
        try {
            su = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
            outputStream.writeBytes("mount -o remount,rw " + dataDir + "\n");
            outputStream.writeBytes("chmod 777 " + sPath+"\n");
            outputStream.writeBytes("exit\n");
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            String sValue = getAuthUin(sPath);
            TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
            String sImei = tm.getDeviceId();
            String sNewValue = sImei+sValue;
            sNewValue = Aes.md5(sNewValue);
            sPassword = sNewValue.substring(0,7);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getWxDir()
    {

        File file = new File(WX_ROOT);
        if(!file.exists())
        {
            return null;
        }
        String[] listFile = file.list();
        ArrayList<String> lsDir = new ArrayList<String>();
        String sDir = null;
        File newFile = null;
        File oldFile = null;
        for(int i=0;i<listFile.length;i++)
        {
            if(listFile[i].length()>30)
            {
                newFile =new File(WX_ROOT+listFile[i]);
                if(newFile.isFile())
                {
                    continue;
                }
                if(sDir==null)
                {
                    sDir = listFile[i];
                }
                else
                {
                    newFile = new File(WX_ROOT+listFile[i]);
                    oldFile = new File(WX_ROOT+sDir);
                    if(newFile.lastModified()>oldFile.lastModified())
                    {
                        sDir = listFile[i];
                    }
                }
            }
        }
        return sDir;
    }

    public String getAuthUin(String sPath) throws Exception {
        FileInputStream instream = new FileInputStream(sPath);
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(instream, "UTF-8");
        int eventType = parser.getEventType();
        String sAuthUin = null;
        AuthInfo authinfoTmp = null,authinfo=null;
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    break;
                case (XmlPullParser.START_TAG):
                    String tagName = parser.getName();
                    if ("int".equals(tagName)) {
                        authinfoTmp = new AuthInfo();
                    }
                    if (authinfoTmp != null) {
                        int iCount= parser.getAttributeCount();
                        for(int i=0;i<iCount;i++)
                        {
                            tagName = parser.getAttributeName(i);
                            if ("name".equals(tagName))
                                authinfoTmp.sName = new String(parser.getAttributeValue(i));
                            else if ("value".equals(tagName))
                                authinfoTmp.sValue = new String(parser.getAttributeValue(i));
                        }

                    }
                    break;
                case (XmlPullParser.END_TAG):
                    if ("int".equals(parser.getName())) {
                        authinfo = authinfoTmp;
                        authinfoTmp = null;
                    }
                    break;
            }
            eventType = parser.next();
        }

        return authinfo.sValue;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btnLocal:
                if(!TextUtils.isEmpty(sSrcPath)) {
                    new RunningTask().execute(sSrcPath);
                }else {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("video/*");
                    startActivityForResult(intent, ACTIVITY_RESULT_LOCAL);
                }
                break;
            case R.id.btnFav:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ACTIVITY_RESULT_LOCAL:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    Uri uri = data.getData();
                    if(uri==null)
                    {
                        Toast.makeText(this,R.string.STR_GET_LOACL_FAIL,Toast.LENGTH_LONG).show();
                        return;
                    }

                    String sPath = null;
                    try {
                        String scheme = uri.getScheme();
                        if ("file".equals(scheme)) {
                            sPath = uri.getPath();
                        } else if ("content".equals(scheme)) {
                            sPath = getAbsoluteImagePath(this, uri);
                        } else {
                            return;
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }

                    if(TextUtils.isEmpty(sPath))
                    {
                        Toast.makeText(this,R.string.STR_GET_LOACL_FAIL,Toast.LENGTH_LONG).show();
                        return;
                    }

                    new RunningTask().execute(sPath);

                }
                break;
        }
    }

    public boolean isContainThumb(String sVideo) {
        String sThumb = sVideo+".thumb";
        File newfile = new File(sThumb);
        if(newfile.exists())
        {
            return true;
        }
        return false;
    }

    public static String getAbsoluteImagePath(Activity context,Uri uri)
    {
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if(currentapiVersion>=19)
        {
            return getAbsoluteImagePathFrom19(context,uri);
        }
        else
        {
            Cursor cursor = null;
            String[] proj ={ MediaStore.Images.Media.DATA };
            cursor = context.managedQuery(uri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
    }

    @SuppressLint("NewApi")
    public static String getAbsoluteImagePathFrom19(final Context context, final Uri uri)
    {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        if (isKitKat && DocumentsContract.isDocumentUri(context, uri))
        {
            if(isExternalStorageDocument(uri))
            {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type))
                {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri))
            {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri))
            {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type))
                {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                }
                else if ("video".equals(type))
                {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                }
                else if ("audio".equals(type))
                {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]
                        { split[1] };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme()))
        {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme()))
        {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context
     *            The context.
     * @param uri
     *            The Uri to query.
     * @param selection
     *            (Optional) Filter used in the query.
     * @param selectionArgs
     *            (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs)
    {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection =
                { column };

        try
        {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst())
            {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        }
        finally
        {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri)
    {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri)
    {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri)
    {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri)
    {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public Bitmap getVideoThumbnail(String filePath) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            bitmap = retriever.getFrameAtTime();
        }
        catch(IllegalArgumentException e) {
            e.printStackTrace();
        }
        catch (RuntimeException e) {
            e.printStackTrace();
        }
        finally {
            try {
                retriever.release();
            }
            catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    public static void copyFile(File oldfile, File newFile) throws IOException
    {
        InputStream inStream = null;
        BufferedInputStream inBufferedStream = null;
        FileOutputStream outputStream = null;
        BufferedOutputStream outBufferedStream = null;
        try
        {
            int byteread = 0;

            inStream = new FileInputStream(oldfile);
            inBufferedStream = new BufferedInputStream(inStream);
            outputStream = new FileOutputStream(newFile);
            outBufferedStream = new BufferedOutputStream(outputStream);

            byte[] buffer = new byte[8192];
            while ((byteread = inBufferedStream.read(buffer)) != -1)
            {
                outBufferedStream.write(buffer, 0, byteread);
            }
            outBufferedStream.flush();
        }
        finally
        {
            if(inBufferedStream != null)
            {
                inBufferedStream.close();
            }
            if (inStream != null)
            {
                inStream.close();
            }
            if(outBufferedStream != null)
            {
                outBufferedStream.close();
            }
            if (outputStream != null)
            {
                outputStream.close();
            }
        }

    }

    public static void copyFile(InputStream inputStream, File newFile) throws IOException
    {
        if (inputStream == null)
        {
            return;
        }

        FileOutputStream outputStream = null;
        try
        {
            int byteread = 0;
            outputStream = new FileOutputStream(newFile);

            byte[] buffer = new byte[8192];
            while ((byteread = inputStream.read(buffer)) != -1)
            {
                outputStream.write(buffer, 0, byteread);
            }
        }
        finally
        {
            if (outputStream != null)
            {
                outputStream.close();
            }
        }
    }

    public static String getFileMD5(File file) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bigInt.toString(16);
    }

    public static String getSHA(String val) throws NoSuchAlgorithmException{
        MessageDigest md5 = MessageDigest.getInstance("SHA-1");
        md5.update(val.getBytes());
        byte[] m = md5.digest();//加密
        return getString(m);
    }

    private static String getString(byte[] b){
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < b.length; i ++){
            sb.append(b[i]);
        }
        return sb.toString();
    }

    class RunningTask extends AsyncTask<String, Void, Integer> {

        private ProgressDialog progressDialog = null;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(ReplaceVideoActivity.this,"","正在执行中...",true,false);
        }

        @Override
        protected Integer doInBackground(String... params) {
            String sPath = params[0];
            Bitmap bmp = getVideoThumbnail(sPath);

            String sPathMd5 = Aes.md5(sPath);
            String sPathMd5Code = String.valueOf(sPath.hashCode());
            File file = new File(sPath);
            String sFileMd5 = getFileMD5(file);
            long iSize = file.length();
            int iFileStaus = 1;
            int fileDuration =6;
            DateTime dateTime = new DateTime();
            long iTime = dateTime.getNow();

            DraftInfo draftInfo = new DraftInfo();
            draftInfo.fileDuration = fileDuration;
            draftInfo.fileLength = iSize;
            draftInfo.fileMd5 = sFileMd5;
            draftInfo.fileName = sPathMd5;
            draftInfo.fileNameHash = sPathMd5Code;
            draftInfo.fileStatus = iFileStaus;
            draftInfo.createTime = iTime;

            String dataDir = Environment.getDataDirectory().getAbsolutePath();
            Process su = null;
            DataOutputStream outputStream= null;
            String sExtDB = Config.EXT_DIR + "/EnMicroMsg.db";
            File fileExtDb = new File(sExtDB);
            if(fileExtDb.exists())
            {
                fileExtDb.delete();
            }

            String sSysDB = sMicroMsg + "/EnMicroMsg.db";
            try {
                su = Runtime.getRuntime().exec("su");
                outputStream = new DataOutputStream(su.getOutputStream());
                outputStream.writeBytes("cat " + sSysDB+"> "+ sExtDB+"\n");
                outputStream.writeBytes("exit\n");
                outputStream.flush();
                outputStream.close();
                Thread.sleep(2000);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                su.destroy();
            }

            try {
                SystemDB systemDB = new SystemDB(getApplicationContext(),sExtDB,sPassword);
                systemDB.InsertDraft(draftInfo);
                systemDB.Close();

                su = Runtime.getRuntime().exec("su");
                outputStream = new DataOutputStream(su.getOutputStream());
                outputStream.writeBytes("cat "+  Config.EXT_DIR+ "/EnMicroMsg.db> "+sMicroMsg+"/EnMicroMsg.db\n");
                outputStream.writeBytes("exit\n");
                outputStream.flush();
                outputStream.close();
                Thread.sleep(2000);

                File destFile = new File(sDraft,sPathMd5);
                if(destFile.exists())
                {
                    destFile.delete();
                }
                File oldFile = new File(sPath);
                copyFile(oldFile,destFile);

                destFile = new File(sDraft,sPathMd5+".thumb");
                if(destFile.exists())
                {
                    destFile.delete();
                }
                FileOutputStream outputStream1 = new FileOutputStream(destFile);
                bmp.compress(Bitmap.CompressFormat.PNG,100,outputStream1);
                return Integer.valueOf(1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally {
                su.destroy();
            }

            return Integer.valueOf(0);
        }

        @Override
        protected void onPostExecute(Integer voidParam) {
            super.onPostExecute(voidParam);
            progressDialog.dismiss();
            if (voidParam.intValue()==0) {
                Toast.makeText(ReplaceVideoActivity.this,"执行失败，请先root权限后重试",Toast.LENGTH_LONG).show();
                return;
            }
            Toast.makeText(ReplaceVideoActivity.this,"执行成功，请到朋友圈小视屏列表选择你要分享的视频",Toast.LENGTH_LONG).show();
        }
    }

    public static class DraftInfo {
        public int localId;
        public String fileName;
        public String fileNameHash;
        public String fileMd5;
        public long fileLength;
        public int fileStatus;
        public int fileDuration;
        public long createTime;
    }

    public static class AuthInfo {
        String sName;
        String sValue;
    }
}
