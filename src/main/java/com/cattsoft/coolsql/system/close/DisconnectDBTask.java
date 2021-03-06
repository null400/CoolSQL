/*
 * �������� 2006-12-25
 */
package com.cattsoft.coolsql.system.close;

import com.cattsoft.coolsql.pub.parse.PublicResource;
import com.cattsoft.coolsql.system.DoOnClosingSystem;
import com.cattsoft.coolsql.system.Task;

/**
 * @author liu_xlin �Ͽ���������ݿ������
 */
public class DisconnectDBTask implements Task {

    /*
     * ���� Javadoc��
     * 
     * @see com.coolsql.system.Task#getDescribe()
     */
    public String getDescribe() {
        return PublicResource.getString("system.closetask.disconnect.describe");
    }

    /*
     * ���� Javadoc��
     * 
     * @see com.coolsql.system.Task#execute()
     */
    public void execute() {

        DoOnClosingSystem.getInstance().disconnectAllDB();

    }

    /*
     * ���� Javadoc��
     * 
     * @see com.coolsql.system.Task#getTaskLength()
     */
    public int getTaskLength() {
        return 1;
    }

}
