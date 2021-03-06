package com.cattsoft.coolsql.view.sqleditor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;

import com.cattsoft.coolsql.pub.display.DelimiterDefinition;
import com.cattsoft.coolsql.pub.util.EncodingUtil;
import com.cattsoft.coolsql.pub.util.FileUtil;
import com.cattsoft.coolsql.pub.util.StringUtil;
import com.cattsoft.coolsql.sql.IteratingScriptParser;
import com.cattsoft.coolsql.sql.ScriptCommandDefinition;
import com.cattsoft.coolsql.system.PropertyConstant;
import com.cattsoft.coolsql.system.Setting;


/**
 * A class to parse a SQL script and return the individual commands
 * in the script. The actual parsing is done by using an instance
 * of {@link IteratingScriptParser}
 */
public class ScriptParser
	implements Iterator
{

	private static final Logger logger=Logger.getLogger(ScriptParser.class);
	
	private String originalScript = null;
	private ArrayList<ScriptCommandDefinition> commands = null;
	private DelimiterDefinition delimiter =new DelimiterDefinition( Setting.getInstance().getProperty(
			PropertyConstant.PROPERTY_VIEW_SQLEDITOR_SQL_DELIMITER,
			DelimiterDefinition.STANDARD_DELIMITER.getDelimiter()),false);
	private DelimiterDefinition alternateDelimiter;
	private int currentIteratorIndex = -42;
	private boolean checkEscapedQuotes = true;
	private IteratingScriptParser iteratingParser = null;
	private boolean emptyLineIsSeparator = false;
	private boolean supportOracleInclude = true;
	private boolean checkSingleLineCommands = true;
	private boolean returnTrailingWhitesapce = false;
	private String alternateLineComment = "--";
	private boolean useAlternateDelimiter = false;
	
	private int maxFileSize;
	
	public ScriptParser()
	{
		this(Setting.getInstance().getInMemoryScriptSizeThreshold());
	}

	/**
	 *	Create a ScriptParser for the given Script.
	 *	The delimiter to be used will be evaluated dynamically
	 */
	public ScriptParser(String aScript)
	{
		this.setScript(aScript);
	}

	
	/** Create a ScriptParser
	 *
	 *	The actual script needs to be specified with setScript()
	 *  The delimiter will be evaluated dynamically
	 */
	public ScriptParser(int fileSize)
	{
		maxFileSize = fileSize;
	}

	/**
	 *	Initialize a ScriptParser from a file.
	 *	The delimiter will be evaluated dynamically
	 */
	public ScriptParser(File f)
		throws IOException
	{
		this(f, null);
	}
	
	/**
	 *	Initialize a ScriptParser from a file.
	 *	The delimiter will be evaluated dynamically
	 */
	public ScriptParser(File f, String encoding)
		throws IOException
	{
		setFile(f, encoding);
	}

	public void setFile(File f)
		throws IOException
	{
		setFile(f, null);
	}
	
	/**
	 * Define the source file for this ScriptParser.
	 * Depending on the size the file might be read into memory or not
	 */
	public void setFile(File f, String encoding)
		throws IOException
	{
		if (!f.exists()) throw new FileNotFoundException(f.getName() + " not found");
		
		if (f.length() < this.maxFileSize)
		{
			this.readScriptFromFile(f, encoding);
			this.findDelimiterToUse();
		}
		else
		{
			this.iteratingParser = new IteratingScriptParser(f, encoding);
			configureParserInstance(this.iteratingParser);
		}
	}
	
	public void readScriptFromFile(File f)
		throws IOException
	{
		this.readScriptFromFile(f, null);
	}
	
	public void readScriptFromFile(File f, String encoding)
		throws IOException
	{
		BufferedReader in = null;
		StringBuilder content = null;
		try
		{
			content = new StringBuilder((int)f.length());
			in = EncodingUtil.createBufferedReader(f, encoding);
			String line = in.readLine();
			while (line != null)
			{
				content.append(line);
				content.append('\n');
				line = in.readLine();
			}
		}
		catch (Exception e)
		{
			logger.error( "Error reading file " + f.getAbsolutePath(), e);
			content = null;
		}
		finally
		{
			FileUtil.closeQuitely(in);
		}
		this.setScript(content == null ? "" : content.toString());
	}
	
	public void allowEmptyLineAsSeparator(boolean flag)
	{
		this.emptyLineIsSeparator = flag;
	}

	public void setAlternateLineComment(String comment)
	{
		this.alternateLineComment = comment;
	}
	
	public void setReturnStartingWhitespace(boolean flag)
	{
		this.returnTrailingWhitesapce = flag;
	}
	
	public void setCheckForSingleLineCommands(boolean flag)
	{
		this.checkSingleLineCommands = flag;
	}
	
	public void setSupportOracleInclude(boolean flag)
	{
		this.supportOracleInclude = flag;
	}
	
	/**
	 *	Define the script to be parsed.
	 *	The delimiter to be used will be checked automatically
	 *	First the it will check if the script ends with the alternate delimiter
	 *	if this is not the case, the script will be checked if it ends with GO
	 *	If so, GO will be used (MS SQL Server script style)
	 *	If none of the above is true, ; (semicolon) will be used
	 */
	public void setScript(String aScript)
	{
		if (aScript == null) throw new NullPointerException("SQL script may not be null");
		if (aScript.equals(this.originalScript))
		{
			if(!delimiter.isChanged())
				return;
			else
				delimiter.resetChanged();
		}
		this.originalScript = aScript;
		this.findDelimiterToUse();
		this.commands = null;
		this.iteratingParser = null;
	}
	
	public void setDelimiter(DelimiterDefinition delim)
	{
		this.setDelimiters(delim, null);
	}

	/**
	 * Sets the alternate delimiter. This implies that 
	 * by default the semicolon is used, and only if 
	 * the alternate delimiter is detected, that will be used.
	 * 
	 * If only one delimiter should be used (and no automatic checking
	 * for an alternate delimiter), use {@link #setDelimiter(DelimiterDefinition)}
	 */
	public void setAlternateDelimiter(DelimiterDefinition alt)
	{
		setDelimiters(DelimiterDefinition.STANDARD_DELIMITER, alt);
	}
	
	/**
	 * Define the delimiters to be used. If the (in-memory) script ends with 
	 * the defined alternate delimiter, then the alternate is used, otherwise
	 * the default
	 */
	public void setDelimiters(DelimiterDefinition defaultDelim, DelimiterDefinition alternateDelim)
	{
		this.delimiter = defaultDelim;
		this.alternateDelimiter = alternateDelim;
		
		if (this.originalScript != null)
		{
			findDelimiterToUse();
		}
	}

	/**
	 *	Try to find out which delimiter should be used for the current script.
	 *	First it will check if the script ends with the alternate delimiter
	 *	if this is not the case, the script will be checked if it ends with GO
	 *	If so, GO will be used (MS SQL Server script style)
	 *	If none of the above is true, ; (semicolon) will be used
	 */
	private void findDelimiterToUse()
	{
		if (this.alternateDelimiter == null) return;
		if (this.originalScript == null) return;
		
		useAlternateDelimiter = (alternateDelimiter.terminatesScript(originalScript));
		this.commands = null;
	}

	/**
	 * Return the index from the overall script mapped to the 
	 * index inside the specified command. For a single command
	 * script scriptCursorLocation will be the same as 
	 * the location inside the dedicated command.
	 * @param commandIndex the index for the command to check
	 * @param cursorPos the index in the overall script
	 * @return the relative index inside the command
	 */
	public int getIndexInCommand(int commandIndex, int cursorPos)
	{
		if (this.commands == null) this.parseCommands();
		if (commandIndex < 0 || commandIndex >= this.commands.size()) return -1;
		ScriptCommandDefinition b = this.commands.get(commandIndex);
		int start = b.getStartPositionInScript();
		int end = b.getEndPositionInScript();
		int relativePos = (cursorPos - start);
		int commandLength = (end - start);
		if (relativePos > commandLength)
		{
			// This can happen when trimming the statements.
			relativePos = commandLength;
		}
		return relativePos;
	}
	
	/**
	 *	Return the command index for the command which is located at
	 *	the given index of the current script.
	 */
	public int getCommandIndexAtCursorPos(int cursorPos)
	{
		if (this.commands == null) this.parseCommands();
		if (cursorPos < 0) return -1;
		int count = this.commands.size();
		if (count == 1) return 0;
		if (count == 0) return -1;
		for (int i=0; i < count - 1; i++)
		{
			ScriptCommandDefinition b = this.commands.get(i);
			ScriptCommandDefinition next = this.commands.get(i + 1);
			if (b.getStartPositionInScript() <= cursorPos && b.getEndPositionInScript() >= cursorPos) return i;
			if (cursorPos > b.getEndPositionInScript() && cursorPos < next.getEndPositionInScript()) return i + 1;
			if (b.getEndPositionInScript() > cursorPos && next.getStartPositionInScript() <= cursorPos) return i+1;
		}
		ScriptCommandDefinition b = this.commands.get(count - 1);
		if (b.getStartPositionInScript() <= cursorPos && b.getEndPositionInScript() >= cursorPos) return count - 1;
		return -1;
	}

	/**
	 *	Get the starting offset in the original script for the command indicated by index
	 */
	public int getStartPosForCommand(int index)
	{
		if (this.commands == null) this.parseCommands();
		if (index < 0 || index >= this.commands.size()) return -1;
		ScriptCommandDefinition b = this.commands.get(index);
		int start = b.getStartPositionInScript();
		return start;
	}

	/**
	 * Get the starting offset in the original script for the command indicated by index
	 */
	public int getEndPosForCommand(int index)
	{
		if (this.commands == null) this.parseCommands();
		if (index < 0 || index >= this.commands.size()) return -1;
		ScriptCommandDefinition b = this.commands.get(index);
		return b.getEndPositionInScript();
	}

	/**
	 * Find the position in the original script for the next start of line
	 */
	public int findNextLineStart(int pos)
	{
		if (this.originalScript == null) return -1;
		if (pos < 0) return pos;
		int len = this.originalScript.length();
		if (pos >= len) return pos;
		char c = this.originalScript.charAt(pos);
		while (pos < len && (c == '\n' || c == '\r'))
		{
			pos ++;
			c = this.originalScript.charAt(pos);
		}
		return pos;
	}

	/**
	 * Return the command at the given index position.
	 */
	public String getCommand(int index)
	{
		return getCommand(index, true);
	}
	
	/**
	 * Return the command at the given index position.
	 */
	public String getCommand(int index, boolean rightTrimCommand)
	{
		if (this.commands == null) this.parseCommands();
		if (index < 0 || index >= this.commands.size()) return null;
		ScriptCommandDefinition c = this.commands.get(index);
		String s = originalScript.substring(c.getStartPositionInScript(), c.getEndPositionInScript());
		if (rightTrimCommand) return StringUtil.rtrim(s);
		else return s;
	}

	public int getSize() 
	{
		if (this.commands == null) this.parseCommands();
		return this.commands.size();
	}

	/**
	 * Return an Iterator which allows to iterate over 
	 * the commands from the script. The Iterator
	 * will return objects of type {@link ScriptCommandDefinition}
	 */
	public Iterator getIterator()
	{
		startIterator();
		return this;
	}
	
	public void startIterator()
	{
		this.currentIteratorIndex = 0;
		if (this.iteratingParser == null && this.commands == null)
		{
			this.parseCommands();
		}
		else if (this.iteratingParser != null)
		{
			configureParserInstance(this.iteratingParser);
			this.iteratingParser.reset();
				
		}
	}
	
	public void done()
	{
		if (this.iteratingParser != null)
		{
			this.iteratingParser.done();
		}
		this.currentIteratorIndex = -42;
	}

	/**
	 * Check for quote characters that are escaped using a 
	 * backslash. If turned on (flag == true) the following
	 * SQL statement would be valid (different to the SQL standard):
	 * <pre>INSERT INTO myTable (column1) VALUES ('Arthurs\'s house');</pre>
	 * but the following Script would generate an error: 
	 * <pre>INSERT INTO myTable (file_path) VALUES ('c:\');</pre>
	 * because the last quote would not bee seen as a closing quote
	 */
	public void setCheckEscapedQuotes(boolean flag)
	{
		this.checkEscapedQuotes = flag;
	}

	public String getDelimiterString()
	{
		if (this.useAlternateDelimiter) return this.alternateDelimiter.getDelimiter();
		return this.delimiter.getDelimiter();
	}
	public DelimiterDefinition getDelimiter()
	{
		return delimiter;
	}
	private void configureParserInstance(IteratingScriptParser p)
	{
		p.setSupportOracleInclude(this.supportOracleInclude);
		p.allowEmptyLineAsSeparator(this.emptyLineIsSeparator);
		p.setCheckEscapedQuotes(this.checkEscapedQuotes);
		p.setDelimiter(useAlternateDelimiter ? this.alternateDelimiter : this.delimiter);
		p.setReturnStartingWhitespace(this.returnTrailingWhitesapce);
		p.setAlternateLineComment(this.alternateLineComment);
		p.setDelimiter(useAlternateDelimiter ? this.alternateDelimiter : this.delimiter);

		if (useAlternateDelimiter)
		{
			p.setCheckForSingleLineCommands(false);
		}
		else
		{
			p.setCheckForSingleLineCommands(this.checkSingleLineCommands);
		}
	}
	
	/**
	 *	Parse the given SQL Script into a List of single SQL statements.
	 */
	private void parseCommands()
	{
		this.commands = new ArrayList<ScriptCommandDefinition>();
		IteratingScriptParser p = new IteratingScriptParser();
		configureParserInstance(p);
		p.setScript(this.originalScript);

		ScriptCommandDefinition c = null; 
		int index = 0;
		
		while ((c = p.getNextCommand()) != null)
		{
			c.setIndexInScript(index);
			index++;
			this.commands.add(c);
		}
	}

	/**
	 *	Check if more commands are present. 
	 */
	public boolean hasNext()
	{
		if (this.currentIteratorIndex == -42) throw new IllegalStateException("Iterator not initialized");
		if (this.iteratingParser != null)
		{
			return this.iteratingParser.hasMoreCommands();
		}
		else
		{
			return this.currentIteratorIndex < this.commands.size();
		}
	}

	/**
	 * Return the next SQL command from the script. 
	 * This is delegated to {@link #getNextCommand()}
	 * @return a String object representing the SQL command
	 * @throws IllegalStateException if the Iterator has not been initialized using {@link #getIterator()}
	 * @see IteratingScriptParser#getNextCommand()
	 * @see #getNextCommand()
	 */
	public Object next()
		throws NoSuchElementException
	{
		if (this.currentIteratorIndex == -42) throw new NoSuchElementException("Iterator not initialized");
		return getNextCommand();
	}

	/**
	 * Return the next {@link ScriptCommandDefinition} from the script. 
	 * 
	 * @throws IllegalStateException if the Iterator has not been initialized using {@link #getIterator()}
	 * @see IteratingScriptParser#getNextCommand()
	 * @see #next()
	 */
	public String getNextCommand()
		throws NoSuchElementException
	{
		if (this.currentIteratorIndex == -42) throw new NoSuchElementException("Iterator not initialized");
		ScriptCommandDefinition command = null;
		String result = null;
		if (this.iteratingParser != null)
		{
			command = this.iteratingParser.getNextCommand();
			if (command == null) return null;
			result = command.getSQL();
		}
		else
		{
			command = this.commands.get(this.currentIteratorIndex);
			result = this.originalScript.substring(command.getStartPositionInScript(), command.getEndPositionInScript());
			this.currentIteratorIndex ++;
		}

		return result;
	}
	/**
	 * return the command definition indicated by index
	 * @param index the index of command definition
	 * @return return null if index is out of bound.
	 */
	public ScriptCommandDefinition getCommandDefinition(int index)
	{
		if(index<0||index>=commands.size())
			return null;
		return commands.get(index);
	}
	/**
	 * Not implemented, as removing commands is not possible.
	 * A call to this method simply does nothing.
	 */
	public void remove()
	{
	}

}
