package com.cattsoft.coolsql.pub.display;

import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

/**
 *
 * 
 */
public class CompoundEdit
	implements UndoableEdit
{
	private ArrayList edits;
	private boolean acceptNew = true;
	
	public CompoundEdit()
	{
		edits = new ArrayList();
	}

	public int getSize()
	{
		return edits.size();
	}
	
	public void clear()
	{
		this.edits.clear();
	}
	
	public void finished()
	{
		acceptNew = false;
	}
	
	public UndoableEdit getLast()
	{
		if (edits.size() == 0) return null;
		return (UndoableEdit)edits.get(edits.size() - 1);
	}

	public void undo() 
		throws CannotUndoException
	{
		if (edits.size() == 0) return;
		for (int i=edits.size() - 1; i > -1; i--)
		{
			UndoableEdit edit = (UndoableEdit)edits.get(i);
			if (edit.canUndo() && edit.isSignificant()) edit.undo();
		}
	}

	public boolean canUndo()
	{
		if (edits.size() == 0) return false;
		for (int i=0; i < edits.size(); i++)
		{
			UndoableEdit edit = (UndoableEdit)edits.get(i);
			if (!edit.canUndo()) return false;
		}
		return true;
	}

	public void redo() 
		throws CannotRedoException
	{
		if (edits.size() == 0) return;
		
		for (int i=0; i < edits.size(); i++)
		{
			UndoableEdit edit = (UndoableEdit)edits.get(i);
			edit.redo();
		}
	}

	public boolean canRedo()
	{
		if (edits.size() == 0) return false;
		for (int i=0; i < edits.size(); i++)
		{
			UndoableEdit edit = (UndoableEdit)edits.get(i);
			if (!edit.canRedo()) return false;
		}
		return true;
	}

	public void die()
	{
		Iterator itr = edits.iterator();
		while (itr.hasNext())
		{
			UndoableEdit edit = (UndoableEdit)itr.next();
			edit.die();
		}
	}

	public boolean addEdit(UndoableEdit anEdit)
	{
		if (!acceptNew) return false;
		return edits.add(anEdit);
	}

	public boolean replaceEdit(UndoableEdit anEdit)
	{
		return false;
	}

	public boolean isSignificant()
	{
		Iterator itr = edits.iterator();
		while (itr.hasNext())
		{
			UndoableEdit edit = (UndoableEdit)itr.next();
			if (edit.isSignificant()) return true;;
		}
		return false;
	}

	public String getPresentationName()
	{
		UndoableEdit edit = getLast();
		if (edit == null) return "";
		return edit.getPresentationName();
	}

	public String getUndoPresentationName()
	{
		UndoableEdit edit = getLast();
		if (edit == null) return "";
		return edit.getUndoPresentationName();
	}

	public String getRedoPresentationName()
	{
		UndoableEdit edit = getLast();
		if (edit == null) return "";
		return edit.getRedoPresentationName();
	}

	
}

