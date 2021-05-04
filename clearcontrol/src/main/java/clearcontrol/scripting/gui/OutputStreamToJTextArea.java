package clearcontrol.scripting.gui;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;

public class OutputStreamToJTextArea extends OutputStream
{
  private final JTextArea mTextArea;

  public OutputStreamToJTextArea(JTextArea textArea)
  {
    this.mTextArea = textArea;
  }

  @Override
  public void write(int b) throws IOException
  {
    mTextArea.append(String.valueOf((char) b));
    mTextArea.setCaretPosition(mTextArea.getDocument().getLength());
  }
}