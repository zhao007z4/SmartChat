package com.zyx.smarttouch.gui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import com.zyx.smarttouch.Config;
import com.zyx.smarttouch.Model.SnsInfo;
import com.zyx.smarttouch.R;



/**
 * Created by chiontang on 3/26/16.
 */
public class SnsInfoAdapter extends ArrayAdapter<SnsInfo> implements View.OnClickListener{

    protected ArrayList<SnsInfo> snsList = null;
    private String sVideoPath = null;
    private OnDownloadListen onDownloadListen;

    private final int TYPE_IMAGE = 0;
    private final int TYPE_VIDEO = 1;
    private final int TYPE_URL = 2;
    private final String TAG = Config.TAG;


    public SnsInfoAdapter(Context context, int resource, ArrayList<SnsInfo> snsList,OnDownloadListen onDownloadListen) {
        super(context, resource, snsList);
        this.snsList = snsList;
        File file = new File(Config.EXT_DIR,"Video");
        if(!file.exists())
        {
            file.mkdirs();
        }
        sVideoPath = file.toString();
        this.onDownloadListen = onDownloadListen;
    }

    @Override
    public int getViewTypeCount()
    {
        return 3;
    }

    @Override
    public int getItemViewType(int position)
    {
        return getType(position);

    }

    private int getType(int position)
    {
        SnsInfo snsInfo = snsList.get(position);
        if(snsInfo.iType==3) {
            return TYPE_URL;
        }
        else {
            if (snsInfo.mediaList.size() > 0) {
                if (snsInfo.mediaList.get(0).startsWith("http://vweixinf.tc.qq.com")) {
                    return TYPE_VIDEO;
                } else {
                    return TYPE_IMAGE;
                }
            } else {
                return TYPE_IMAGE;
            }
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        final ViewHolder viewHolder;
        SnsInfo snsInfo = snsList.get(position);
        int iType = getType(position);

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            viewHolder = new ViewHolder();
            if(iType == TYPE_VIDEO)
            {
                view = inflater.inflate(R.layout.sns_item_video, null);
                viewHolder.videoView = (VideoView)view.findViewById(R.id.videoView);
                viewHolder.btnShareReplace = (Button) view.findViewById(R.id.btnShareReplace);
            }
            else if(iType == TYPE_URL)
            {
                view = inflater.inflate(R.layout.sns_item_url, null);
                viewHolder.imgvUrl = (ImageView) view.findViewById(R.id.imgvUrl);
                viewHolder.tvUrlTitle = (TextView)view.findViewById(R.id.tvUrlTitle);
            }
            else
            {
                view = inflater.inflate(R.layout.sns_item, null);
                for (int i=0;i<10;i++) {
                    ImageView snsImageView = new ImageView(getContext());
                    viewHolder.imageViewList.add(snsImageView);
                }
                viewHolder.btnShareReplace = null;
            }

            final CheckBox selectedCheckBox = (CheckBox) view.findViewById(R.id.sns_item_username);
            TextView snsContentTextView = (TextView) view.findViewById(R.id.sns_item_text_content);
            TextView snsTimeTextView = (TextView) view.findViewById(R.id.sns_item_time);
            LinearLayout photoContainer = (LinearLayout) view.findViewById(R.id.sns_item_photo_layout);

            viewHolder.selectedCheckBox = selectedCheckBox;
            viewHolder.snsContentTextView = snsContentTextView;
            viewHolder.snsTimeTextView = snsTimeTextView;
            viewHolder.photoContainer = photoContainer;
            viewHolder.btnShare = (Button) view.findViewById(R.id.btnShare);

            view.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder)view.getTag();
        }

        viewHolder.selectedCheckBox.setText(snsInfo.authorName);
        viewHolder.selectedCheckBox.setChecked(snsInfo.selected);
        viewHolder.snsContentTextView.setText(snsInfo.content);
        viewHolder.snsTimeTextView.setText(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault()).format(new Date(snsInfo.timestamp * 1000)));
        viewHolder.selectedCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SnsInfo snsInfo = snsList.get(position);
                snsInfo.selected = viewHolder.selectedCheckBox.isChecked();
            }
        });

        viewHolder.btnShare.setOnClickListener(this);
        viewHolder.btnShare.setTag(snsInfo);

        if(viewHolder.btnShareReplace !=null) {
            viewHolder.btnShareReplace.setTag(snsInfo);
            viewHolder.btnShareReplace.setOnClickListener(this);
        }

        if(iType == TYPE_VIDEO) {
            VideoView videoView = viewHolder.videoView;
            if(videoView==null)
            {
                videoView = (VideoView)view.findViewById(R.id.videoView);
            }
            String imageUrl = snsInfo.mediaList.get(0);
            File file = new File(sVideoPath+"/"+snsInfo.id);
            if(!file.exists()) {
                if(onDownloadListen !=null)
                {
                    onDownloadListen.download(imageUrl,sVideoPath,snsInfo.id);
                }
            }
            else {
                Uri uri = Uri.parse(sVideoPath + "/" + snsInfo.id);
                videoView.setMediaController(new MediaController(getContext()));
                videoView.setVideoURI(uri);
                videoView.start();
                videoView.setMediaController(null);
                //videoView.requestFocus();
            }
        } else if(iType== TYPE_URL) {
            if(snsInfo.mediaList.size()>0)
            {
                String imageUrl = snsInfo.mediaList.get(0);
                ImageView snsImageView = viewHolder.imgvUrl;
                snsImageView.setImageBitmap(null);
                ImageLoader imageLoader = ImageLoader.getInstance();
                imageLoader.displayImage(imageUrl, snsImageView);
            }
            viewHolder.tvUrlTitle.setText(snsInfo.title);
        }
        else{
            viewHolder.photoContainer.removeAllViews();
            ImageView snsImageView = null;
            int iCount = snsInfo.mediaList.size();
            if(iCount>10)
            {
                iCount =10;
            }
            for (int i=0;i<iCount;i++) {
                String imageUrl = snsInfo.mediaList.get(i);
                snsImageView = viewHolder.imageViewList.get(i);
                try {
                    viewHolder.photoContainer.addView(snsImageView);

                    snsImageView.setImageBitmap(null);
                    ImageLoader imageLoader = ImageLoader.getInstance();
                    imageLoader.displayImage(imageUrl, snsImageView);

                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) snsImageView.getLayoutParams();
                    layoutParams.setMargins(0, 0, 10, 0);
                    layoutParams.height = 200;
                    layoutParams.width = 200;
                    snsImageView.setLayoutParams(layoutParams);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return view;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btnShare: {
                SnsInfo snsInfo = (SnsInfo) v.getTag();
                if(snsInfo.iType == 3)
                {
                    if (onDownloadListen != null) {
                        if(snsInfo.mediaList.size()>0) {
                            String imageUrl = snsInfo.mediaList.get(0);
                            ImageLoader imageLoader = ImageLoader.getInstance();
                            String spath = imageLoader.getDiskCache().get(imageUrl).getPath();
                            onDownloadListen.shareURL(snsInfo.contentUrl, snsInfo.title, spath);
                        }
                        return;
                    }
                }
                if (snsInfo != null) {

                    if (snsInfo.mediaList.size() < 1) {
                        Toast.makeText(getContext(), "不支持纯文字分享", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ArrayList<Uri> imageUris = new ArrayList<Uri>();
                    File file = null;
                    if (snsInfo.mediaList.get(0).startsWith("http://mmsns.qpic.cn/mmsns")) {
                        ImageLoader imageLoader = ImageLoader.getInstance();
                        for (int i = 0; i < snsInfo.mediaList.size(); i++) {
                            String imageUrl = snsInfo.mediaList.get(i);
                            String spath = imageLoader.getDiskCache().get(imageUrl).getPath();
                            file = new File(spath);
                            if(!file.exists())
                            {
                                Toast.makeText(getContext(), R.string.STR_PIC_LOADING, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            imageUris.add(Uri.fromFile(new File(spath)));
                        }
                    }

                    // File file = new File(Config.EXT_DIR + "/share_image.jpg");
                    try {
                        Intent intent = new Intent();
                        ComponentName comp = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareToTimeLineUI");
                        intent.setComponent(comp);
                        //intent.setAction("android.intent.action.SEND");

                        //intent.setFlags(0x3000001);
                        if (imageUris.size() > 0) {
                            intent.setType("image/*");
                            intent.setAction(Intent.ACTION_SEND_MULTIPLE);
                            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
                        } else {
                            file = new File(sVideoPath, snsInfo.id);
                            if (onDownloadListen != null) {
                                onDownloadListen.shareVideo(snsInfo.mediaList.get(0), snsInfo.content, file.toString());
                                return;
                            }
                        }

                        //intent.putExtra(Intent.EXTRA_SUBJECT, snsInfo.authorName);
                        intent.putExtra("Kdescription", snsInfo.content);
                        //intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                        v.getContext().startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
            case R.id.btnShareReplace: {
                SnsInfo snsInfo = (SnsInfo) v.getTag();
                if (snsInfo != null) {
                    File file = new File(sVideoPath + "/" + snsInfo.id);
                    if (!file.exists()) {
                        Toast.makeText(getContext(),R.string.STR_VIDEO_LOADING,Toast.LENGTH_LONG).show();
                        return;
                    }
                    Intent intent = new Intent(getContext(), ReplaceVideoActivity.class);
                    intent.putExtra("path",file.toString());
                    getContext().startActivity(intent);
                }
                break;
            }
        }
    }

    static protected class ViewHolder {
        CheckBox selectedCheckBox;
        TextView snsContentTextView;
        TextView snsTimeTextView;
        Button btnShare;
        Button btnShareReplace;
        LinearLayout photoContainer;
        ArrayList<ImageView> imageViewList = new ArrayList<ImageView>();
        VideoView videoView;
        ImageView imgvUrl;
        TextView tvUrlTitle;
    }

    public interface OnDownloadListen
    {
        public void download(String sUrl,String sDir,String sName);
        public void shareVideo(String sUrl,String sContent,String sPath);
        public void shareURL(String sUrl,String sTitle,String sPath);
    }
}
