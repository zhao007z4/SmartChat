package com.zyx.smarttouch.database;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import com.zyx.smarttouch.gui.ReplaceVideoActivity;

public class SystemDB
{
	private Config userConfig;
	public final static String EnMicroMsgDB = "EnMicroMsg.db";

	public static final String DRAFT_FILED_localId = "localId";
	public static final String DRAFT_FILED_fileName = "fileName";
	public static final String DRAFT_FILED_fileNameHash = "fileNameHash";
	public static final String DRAFT_FILED_fileMd5= "fileMd5";
	public static final String DRAFT_FILED_fileLength = "fileLength";
	public static final String DRAFT_FILED_fileStatus = "fileStatus";
	public static final String DRAFT_FILED_fileDuration = "fileDuration";
    public static final String DRAFT_FILED_createTime= "createTime";

	public SystemDB(Context context,String sPath, String sPassword)
	{
		userConfig = new Config(context,sPath, sPassword, false);
	}

    public void Close()
    {
        userConfig.close();
    }

	public void InsertDraft(ReplaceVideoActivity.DraftInfo draftInfo)
	{
		if (draftInfo == null)
		{
			return;
		}

		ArrayList<HashMap<String, Object>> lsTmp = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> hashMap = new HashMap<String, Object>();

		hashMap.put(DRAFT_FILED_fileName, draftInfo.fileName);
		hashMap.put(DRAFT_FILED_fileNameHash, draftInfo.fileNameHash);
		hashMap.put(DRAFT_FILED_fileMd5, draftInfo.fileMd5);
		hashMap.put(DRAFT_FILED_fileLength, draftInfo.fileLength);
		hashMap.put(DRAFT_FILED_fileStatus, draftInfo.fileStatus);
		hashMap.put(DRAFT_FILED_fileDuration, draftInfo.fileDuration);
        hashMap.put(DRAFT_FILED_createTime,draftInfo.createTime);
		
		lsTmp.add(hashMap);
		userConfig.InsertMaps("SightDraftInfo", lsTmp);
	}
	

	
}
