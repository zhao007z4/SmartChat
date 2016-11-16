package com.zyx.smarttouch.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import android.content.Context;
import android.database.Cursor;

public class Config {
	
	public SQL sql;
	

	public Config(Context context,String sPath, String sPassword, boolean CreateIfNecessary)
	{
		sql= new SQL(context,sPath,sPassword,CreateIfNecessary);
	}
	
	public boolean isOpen()
	{
		if(sql==null)
		{
			return false;
		}
		return sql.IsInitialized();
	}
	
	public void CreateTable(String sTableName, HashMap<Object, Object>FieldsAndTypes, String PrimaryKey,boolean bAUTOINCREMENT)
	{
		try
        {
			if(sql==null)
			{
				return;
			}
			StringBuilder sb = new StringBuilder();
			sb = sb.append("(");
			
			Iterator<Entry<Object, Object>> iterator =FieldsAndTypes.entrySet().iterator();
			boolean bFirst = true;
			while(iterator.hasNext()) 
			{
				Entry<Object, Object> next = iterator.next();
				if(!bFirst)
				{
					sb =sb.append(", ");
				}
				sb = sb.append("[").append(next.getKey()).append("] ").append(next.getValue());
				
				if(PrimaryKey!=null && next.getKey().toString().equalsIgnoreCase(PrimaryKey))
				{
					sb.append(" PRIMARY KEY");
					if(bAUTOINCREMENT)
					{
						sb = sb.append(" AUTOINCREMENT");
					}
				}
				bFirst = false;
			}
			sb = sb.append(")");
			
			String sQuery ="CREATE TABLE IF NOT EXISTS [" + sTableName + "] " + sb.toString();
			sql.ExecNonQuery(sQuery);
        }
        catch (Exception e)
        {
	        e.printStackTrace();
        }
		
	}
	
	public void DropTable(String sTableName)
	{
		if(sql==null)
		{
			return;
		}
		String sQuery="DROP TABLE IF EXISTS [" + sTableName + "]";
		if(sql!=null)
		{
			sql.ExecNonQuery(sQuery);
		}
	}
	
	public void InsertMaps(String sTableName, ArrayList<HashMap<String, Object>> ListOfMaps)
	{
		if(sql==null || !sql.IsInitialized())
		{
			return;
		}
		
		boolean bFirst=true;
		if(ListOfMaps==null || ListOfMaps.size()<1)
		{
			return;
		}
		
		sql.BeginTransaction();
		for(int i=0;i<ListOfMaps.size();i++)
		{
			bFirst= true;
			StringBuilder sb=new StringBuilder();
			StringBuilder columns=new StringBuilder();
			StringBuilder values=new StringBuilder();
			sb = sb.append("INSERT INTO [" + sTableName + "] (");
			
			ArrayList<Object>listOfValues = new ArrayList<Object>();
			HashMap<String, Object> map = ListOfMaps.get(i);
			
			Iterator<Entry<String, Object>> iterator =map.entrySet().iterator();
			while(iterator.hasNext()) 
			{
				Entry<String, Object> next = iterator.next();
				if(!bFirst)
				{
					columns = columns.append(", ");
					values = values.append(", ");
				}
				columns.append("[").append(next.getKey()).append("]");
				values.append("?");
				listOfValues.add(next.getValue());
				bFirst = false;
			}
			sb = sb.append(columns.toString()).append(") VALUES (").append(values.toString()).append(")");
			sql.ExecNonQuery2(sb.toString(), listOfValues);	
		}
		sql.TransactionSuccessful();
		sql.EndTransaction();
	}
	
	
	public void replaceMaps(String sTableName, ArrayList<HashMap<String, Object>> ListOfMaps)
	{
		if(sql==null)
		{
			return;
		}
		boolean bFirst=true;
		if(ListOfMaps==null || ListOfMaps.size()<1)
		{
			return;
		}
		
		sql.BeginTransaction();
		for(int i=0;i<ListOfMaps.size();i++)
		{
			bFirst= true;
			StringBuilder sb=new StringBuilder();
			StringBuilder columns=new StringBuilder();
			StringBuilder values=new StringBuilder();
			sb = sb.append("REPLACE INTO [" + sTableName + "] (");
			
			ArrayList<Object>listOfValues = new ArrayList<Object>();
			HashMap<String, Object> map = ListOfMaps.get(i);
			
			Iterator<Entry<String, Object>> iterator =map.entrySet().iterator();
			while(iterator.hasNext()) 
			{
				Entry<String, Object> next = iterator.next();
				if(!bFirst)
				{
					columns = columns.append(", ");
					values = values.append(", ");
				}
				columns.append("[").append(next.getKey()).append("]");
				values.append("?");
				listOfValues.add(next.getValue());
				bFirst = false;
			}
			sb = sb.append(columns.toString()).append(") VALUES (").append(values.toString()).append(")");
			sql.ExecNonQuery2(sb.toString(), listOfValues);	
		}
		sql.TransactionSuccessful();
		sql.EndTransaction();
	}


	public void UpdateRecord(String sTableName,String sField, Object sNewValue, HashMap<Object, Object>WhereFieldEquals)
	{
		if(sql==null)
		{
			return;
		}
		StringBuilder sb=new StringBuilder();
		sb = sb.append("UPDATE [").append(sTableName).append("] SET [").append(sField).append("] = ? WHERE ");
		if(WhereFieldEquals.size() == 0)
		{
			return;
		}
		ArrayList<Object> listArgs = new ArrayList<Object>(); 
		listArgs.add(sNewValue);
		
		boolean bFirst=true;
		Iterator<Entry<Object, Object>> iterator =WhereFieldEquals.entrySet().iterator();
		while(iterator.hasNext()) 
		{
			Entry<Object, Object> next = iterator.next();
			if(!bFirst)
			{
				sb= sb.append(" AND ");
			}
			sb = sb.append("[").append(next.getKey()).append("] = ?");
			listArgs.add(next.getValue());
		}
		sql.ExecNonQuery2(sb.toString(), listArgs);
	}

	public void UpdateRecord2(String sTableName, HashMap<Object,Object>sFields, HashMap<Object,Object>WhereFieldEquals)
	{
		if(sql==null)
		{
			return;
		}
		if(WhereFieldEquals==null || WhereFieldEquals.size()<1 || sFields==null || sFields.size()<1 )
		{
			return;
		}

		StringBuilder sb=new StringBuilder();
		sb = sb.append("UPDATE [").append(sTableName).append("] SET ");
		ArrayList<Object> ListArgs = new ArrayList<Object>();
		boolean bFirst=true;
		
		Iterator<Entry<Object, Object>> iterator =sFields.entrySet().iterator();
		while(iterator.hasNext()) 
		{
			Entry<Object, Object> next = iterator.next();
			if(!bFirst)
			{
				sb= sb.append(",");
			}
			sb = sb.append("[").append(next.getKey()).append("]=?");
			ListArgs.add(next.getValue());
		}
		
		sb = sb.append(" WHERE ");
		iterator =WhereFieldEquals.entrySet().iterator();
		while(iterator.hasNext()) 
		{
			Entry<Object, Object> next = iterator.next();
			if(!bFirst)
			{
				sb= sb.append(" AND ");
			}
			sb = sb.append("[").append(next.getKey()).append("] = ?");
		}
		sql.ExecNonQuery2(sb.toString(), ListArgs);
	}
	
	public void DeleteRecord(String sTableName)
	{
		if(sql==null)
		{
			return;
		}
		String sQuery="DELETE  FROM [" +sTableName + "]";
		sql.ExecNonQuery(sQuery);
	}
	
	public void DeleteRecord(String sTableName, String sRecord)
	{
		if(sql==null)
		{
			return;
		}
		String sQuery = "DELETE  FROM [" + sTableName + "] where " + sRecord;
		sql.ExecNonQuery(sQuery);
	}
	
	public boolean IsExistTable(String sTable)
	{
		long count;
		if(sql==null)
		{
			return false;
		}
		count = Integer.parseInt(sql.ExecQuerySingleResult("SELECT count(*) FROM sqlite_master WHERE Type='table' AND name='"+sTable+"'"));
		if(count > 0)
		{
			return true;	
		}
		return false;
	}
	
	public Cursor ExecuteMemoryTable(String sQuery, String StringArgs[])
	{
		Cursor cur;
		if(sql==null)
		{
			return null;
		}
		if(StringArgs==null || StringArgs.length<1)
		{
			cur = sql.ExecQuery(sQuery);
		}
		else
		{
			cur = sql.ExecQuery2(sQuery, StringArgs);
		}
		return cur;
	}
	public void close()
	{
		if(sql==null)
		{
			return;
		}
		sql.Close();
	}
}
