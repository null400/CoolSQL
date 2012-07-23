/*
 * �������� 2006-12-27
 */
package com.cattsoft.coolsql.system.start;

import com.cattsoft.coolsql.pub.parse.PublicResource;
import com.cattsoft.coolsql.system.SystemGarbageCollectThread;
import com.cattsoft.coolsql.system.Task;

/**
 * @author liu_xlin
 *������������߳�
 */
public class LaunchGarbageCollectorTask implements Task {

    /* ���� Javadoc��
     * @see com.coolsql.system.Task#getDescribe()
     */
    public String getDescribe() {
        return PublicResource.getString("system.launch.loadgarbagecollector");
    }

    /* ���� Javadoc��
     * @see com.coolsql.system.Task#execute()
     */
    public void execute() {
        SystemGarbageCollectThread sgc = new SystemGarbageCollectThread(60000);
        sgc.start();
    }

    /* ���� Javadoc��
     * @see com.coolsql.system.Task#getTaskLength()
     */
    public int getTaskLength() {
        return 1;
    }

}
