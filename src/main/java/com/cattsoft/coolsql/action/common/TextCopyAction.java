/*
 * �������� 2006-7-3
 *
 */
package com.cattsoft.coolsql.action.common;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.text.JTextComponent;

/**
 * @author liu_xlin
 *�༭���ĸ����¼�����
 */
public class TextCopyAction extends AbstractAction {
	private JTextComponent _com=null;
	public TextCopyAction(JTextComponent com)
	{
	   _com=com;	
	}
	/* ���� Javadoc��
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if(_com!=null)
			_com.copy();
	}

}
