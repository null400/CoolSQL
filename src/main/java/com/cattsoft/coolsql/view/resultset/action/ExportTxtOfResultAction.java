/*
 * �������� 2006-10-25
 */
package com.cattsoft.coolsql.view.resultset.action;

import java.awt.event.ActionEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.cattsoft.coolsql.action.common.ExportTextOfTableAction;
import com.cattsoft.coolsql.pub.display.GUIUtil;
import com.cattsoft.coolsql.view.ResultSetView;
import com.cattsoft.coolsql.view.log.LogProxy;
import com.cattsoft.coolsql.view.resultset.DataSetTable;

/**
 * @author liu_xlin
 * �����ͼ�����չʾ��ؼ����ı������¼�����
 */
public class ExportTxtOfResultAction extends ExportTextOfTableAction {

    public ExportTxtOfResultAction() {
        super(null);
    }
    public void actionPerformed(ActionEvent e)
    {
//        ResultSetView view=ViewManage.getInstance().getResultView();
//        ITabbedPane pane=view.getResultTab();
//        JComponent com=((DataSetPanel)pane.getSelectedComponent()).getContent();
//        if(!(com instanceof JScrollPane))
//        {
//            return;
//        }
//        com=(JComponent)((JScrollPane)com).getViewport().getView();
//        if(!(com instanceof JTable))  //���Ϊ��ؼ�,��ô��������
//        {
//            return;
//        }
        JPopupMenu popMenu=GUIUtil.getTopMenu((JMenuItem)e.getSource());
        if(popMenu==null)
            return ;
        
        DataSetTable table=(DataSetTable)popMenu.getClientProperty(ResultSetView.DataTable);
        if(table==null)
        {
            LogProxy.errorMessage("can't get data table object!");
            return ;
        }
        this.setTable(table);
        super.actionPerformed(e);
    }
}
