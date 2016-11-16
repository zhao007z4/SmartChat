package com.zyx.smarttouch.database;

import android.content.Context;
import android.util.Log;

import com.zyx.smarttouch.gui.ReplaceVideoActivity;

import net.sqlcipher.Cursor;
import net.sqlcipher.DatabaseUtils;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteDatabaseHook;
import net.sqlcipher.database.SQLiteStatement;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class SQL
{
	private SQLiteDatabase db;

	public SQL(Context context, String sPath, String sPassword, boolean bCreateIfNecessary)
	{

		SQLiteDatabase.loadLibs(context);

        SQLiteDatabaseHook hook = new SQLiteDatabaseHook(){
            public void preKey(SQLiteDatabase database){
            }
            public void postKey(SQLiteDatabase database){
                database.rawExecSQL("PRAGMA cipher_migrate;");  //最关键的一句！！！
            }
        };

        this.db = SQLiteDatabase.openDatabase(sPath,sPassword, null, (bCreateIfNecessary ? 268435456 : 0) | 0x10,hook);
        int iVersion = this.db.getVersion();
    }

    private void checkNull()
    {
        if (this.db == null) {
            throw new RuntimeException("Object should first be initialized.");
        }
    }

    public boolean IsInitialized()
    {
        if (this.db == null) {
            return false;
        }
        return this.db.isOpen();
    }

    public void ExecNonQuery(String paramString)
    {
        checkNull();
        this.db.execSQL(paramString);
    }

    public void ExecNonQuery2(String paramString, List<Object> paramList)
    {
        checkNull();
        SQLiteStatement localSQLiteStatement = this.db.compileStatement(paramString);
        try
        {
            int i = paramList.size();
            for (int j = 0; j < i; j++) {
                DatabaseUtils.bindObjectToProgram(localSQLiteStatement, j + 1, paramList.get(j));
            }
            localSQLiteStatement.execute();
        }
        finally
        {
            localSQLiteStatement.close();
        }
    }

    public android.database.Cursor ExecQuery(String paramString)
    {
        checkNull();
        return ExecQuery2(paramString, null);
    }

    public android.database.Cursor ExecQuery2(String paramString, String[] paramArrayOfString)
    {
        checkNull();
        return this.db.rawQuery(paramString, paramArrayOfString);
    }

    public String ExecQuerySingleResult(String paramString)
    {
        return ExecQuerySingleResult2(paramString, null);
    }

    public String ExecQuerySingleResult2(String paramString, String[] paramArrayOfString)
    {
        checkNull();
        net.sqlcipher.Cursor localCursor = this.db.rawQuery(paramString, paramArrayOfString);
        try
        {
            String str;
            if (!localCursor.moveToFirst()) {
                return null;
            }
            if (localCursor.getColumnCount() == 0) {
                return null;
            }
            return localCursor.getString(0);
        }
        finally
        {
            localCursor.close();
        }
    }

    public void BeginTransaction()
    {
        checkNull();
        this.db.beginTransaction();
    }

    public void TransactionSuccessful()
    {
        this.db.setTransactionSuccessful();
    }

    public void EndTransaction()
    {
        this.db.endTransaction();
    }

    public void Close()
    {
        if ((this.db != null) && (this.db.isOpen())) {
            this.db.close();
        }
    }
}
