package cleargl;

import com.jogamp.opengl.GL4;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Convenience class for handling OpenGL Framebuffers
 *
 * @author Ulrik Günther
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class GLFramebuffer
{
  protected int framebufferId[];

  protected LinkedHashMap<String, GLTexture> backingTextures;

  protected LinkedHashMap<String, GLTexture> depthBuffers;

  protected int width;

  protected int height;

  protected boolean initialized;

  protected boolean sRGB;

  public GLFramebuffer(final GL4 gl, final int width, final int height)
  {
    this(gl, width, height, false);
  }

  public GLFramebuffer(final GL4 gl, final int width, final int height, final boolean sRGB)
  {
    framebufferId = new int[1];
    backingTextures = new LinkedHashMap<>();
    depthBuffers = new LinkedHashMap<>();
    this.width = width;
    this.height = height;
    this.sRGB = sRGB;

    gl.getGL().glGenFramebuffers(1, framebufferId, 0);

    initialized = true;
  }

  public boolean hasDepthAttachment()
  {
    return depthBuffers.size() > 0;
  }

  public boolean hasColorAttachment()
  {
    return backingTextures.size() > 0;
  }

  private void addFloatBufferInternal(final GL4 gl, final String name, final int channelCount, final int channelDepth)
  {
    if (!initialized)
    {
      return;
    }

    gl.getGL().glBindFramebuffer(GL4.GL_FRAMEBUFFER, getId());

    backingTextures.put(name, new GLTexture(gl, GLTypeEnum.Float, channelCount, width, height, 1, true, 1, channelDepth));

    gl.glFramebufferTexture(GL4.GL_FRAMEBUFFER, getCurrentFramebufferColorAttachment(), backingTextures.get(name).getId(), 0);

    gl.getGL().glBindFramebuffer(GL4.GL_FRAMEBUFFER, 0);
  }

  public void addFloatRBuffer(final GL4 gl, final String name, final int channelDepth)
  {
    addFloatBufferInternal(gl, name, 1, channelDepth);
  }

  public void addFloatRGBuffer(final GL4 gl, final String name, final int channelDepth)
  {
    addFloatBufferInternal(gl, name, 2, channelDepth);
  }

  public void addFloatRGBBuffer(final GL4 gl, final String name, final int channelDepth)
  {
    addFloatBufferInternal(gl, name, 3, channelDepth);
  }

  public void addFloatRGBABuffer(final GL4 gl, final String name, final int channelDepth)
  {
    addFloatBufferInternal(gl, name, 4, channelDepth);
  }

  private void addUnsignedByteBufferInternal(final GL4 gl, final String name, final int channelCount, final int channelDepth)
  {
    if (!initialized)
    {
      return;
    }

    gl.getGL().glBindFramebuffer(GL4.GL_FRAMEBUFFER, getId());

    backingTextures.put(name, new GLTexture(gl, GLTypeEnum.UnsignedByte, channelCount, width, height, 1, true, 1, channelDepth, true, sRGB));

    gl.glFramebufferTexture(GL4.GL_FRAMEBUFFER, getCurrentFramebufferColorAttachment(), backingTextures.get(name).getId(), 0);

    gl.getGL().glBindFramebuffer(GL4.GL_FRAMEBUFFER, 0);
  }


  public void addUnsignedByteRBuffer(final GL4 gl, final String name, final int channelDepth)
  {
    addUnsignedByteBufferInternal(gl, name, 1, channelDepth);
  }

  public void addUnsignedByteRGBuffer(final GL4 gl, final String name, final int channelDepth)
  {
    addUnsignedByteBufferInternal(gl, name, 2, channelDepth);
  }

  public void addUnsignedByteRGBBuffer(final GL4 gl, final String name, final int channelDepth)
  {
    addUnsignedByteBufferInternal(gl, name, 3, channelDepth);
  }

  public void addUnsignedByteRGBABuffer(final GL4 gl, final String name, final int channelDepth)
  {
    addUnsignedByteBufferInternal(gl, name, 4, channelDepth);
  }

  public void addDepthBuffer(final GL4 gl, final String name, final int depth)
  {
    addDepthBuffer(gl, name, depth, 1);
  }

  public void addDepthBuffer(final GL4 gl, final String name, final int depth, final int scale)
  {
    if (!initialized)
    {
      return;
    }

    gl.getGL().glBindFramebuffer(GL4.GL_FRAMEBUFFER, getId());

    depthBuffers.put(name, new GLTexture(gl, GLTypeEnum.Float, -1, width / scale, height / scale, 1, true, 1, depth));

    gl.glFramebufferTexture(GL4.GL_FRAMEBUFFER, GL4.GL_DEPTH_ATTACHMENT, depthBuffers.get(name).getId(), 0);

    gl.getGL().glBindFramebuffer(GL4.GL_FRAMEBUFFER, 0);
  }

  public boolean checkDrawBuffers(final GL4 gl)
  {
    if (!initialized)
    {
      return false;
    }

    gl.getGL4().glBindFramebuffer(GL4.GL_FRAMEBUFFER, getId());
    final int status = gl.glCheckFramebufferStatus(GL4.GL_FRAMEBUFFER);

    if (status != GL4.GL_FRAMEBUFFER_COMPLETE)
    {
      System.err.println("Framebuffer " + framebufferId[0] + " is incomplete, " + Integer.toHexString(status));
      return false;
    }

    return true;
  }

  public void setDrawBuffers(final GL4 gl)
  {
    final int attachments[] = new int[backingTextures.size()];
    for (int i = 0; i < backingTextures.size(); i++)
    {
      attachments[i] = GL4.GL_COLOR_ATTACHMENT0 + i;
    }

    gl.glBindFramebuffer(GL4.GL_DRAW_FRAMEBUFFER, getId());
    gl.glDrawBuffers(backingTextures.size(), attachments, 0);
  }

  public void setReadBuffers(final GL4 gl)
  {
    gl.glBindFramebuffer(GL4.GL_READ_FRAMEBUFFER, getId());
  }

  public void setReadBuffers(final GL4 gl, String sourceName)
  {
    gl.glBindFramebuffer(GL4.GL_READ_FRAMEBUFFER, getId());

    for (int i = 0; i < backingTextures.size(); i++)
    {
      if (backingTextures.keySet().toArray()[i].equals(sourceName))
      {
        gl.glReadBuffer(GL4.GL_COLOR_ATTACHMENT0 + i);
      }
    }
  }

  public int bindTexturesToUnitsWithOffset(final GL4 gl, final int offset)
  {
    int totalUnits = 0;

    for (Map.Entry<String, GLTexture> entry : backingTextures.entrySet())
    {
      gl.glActiveTexture(GL4.GL_TEXTURE0 + offset + totalUnits);
      gl.glBindTexture(GL4.GL_TEXTURE_2D, entry.getValue().getId());
      totalUnits++;
    }

    for (Map.Entry<String, GLTexture> entry : depthBuffers.entrySet())
    {
      gl.glActiveTexture(GL4.GL_TEXTURE0 + offset + totalUnits);
      gl.glBindTexture(GL4.GL_TEXTURE_2D, entry.getValue().getId());
      totalUnits++;
    }

    return totalUnits;
  }

  public List<Integer> getTextureIds(final GL4 gl)
  {
    final ArrayList<Integer> list = new ArrayList<>();

    for (Map.Entry<String, GLTexture> entry : backingTextures.entrySet())
    {
      list.add(entry.getValue().getId());
    }

    return list;
  }

  public int getTextureId(final String name)
  {
    if (backingTextures.containsKey(name))
    {
      return backingTextures.get(name).getId();
    } else
    {
      return -1;
    }
  }

  public int getTextureType(final String name)
  {
    if (backingTextures.containsKey(name))
    {
      return 0;
    }

    if (depthBuffers.containsKey(name))
    {
      return 1;
    }

    return -1;
  }

  public void revertToDefaultFramebuffer(final GL4 gl)
  {
    gl.glBindFramebuffer(GL4.GL_FRAMEBUFFER, 0);
  }

  public void resize(final GL4 gl, final int newWidth, final int newHeight)
  {
    final int oldIds[] = framebufferId.clone();

    gl.glBindFramebuffer(GL4.GL_FRAMEBUFFER, 0);
    gl.glGenFramebuffers(1, framebufferId, 0);
    gl.glBindFramebuffer(GL4.GL_FRAMEBUFFER, getId());

    final LinkedHashMap<String, GLTexture> newBackingTextures = new LinkedHashMap<>();
    final LinkedHashMap<String, GLTexture> newDepthBuffers = new LinkedHashMap<>();

    for (Map.Entry<String, GLTexture> entry : backingTextures.entrySet())
    {
      final GLTexture newT = new GLTexture(gl, entry.getValue().getNativeType(), entry.getValue().getChannels(), newWidth, newHeight, 1, true, 1, entry.getValue().getBitsPerChannel());

      newT.clear();
      newBackingTextures.put(entry.getKey(), newT);
      entry.getValue().close();

      gl.glFramebufferTexture(GL4.GL_FRAMEBUFFER, getCurrentFramebufferColorAttachment(newBackingTextures.size()), newBackingTextures.get(entry.getKey()).getId(), 0);
    }

    for (Map.Entry<String, GLTexture> entry : depthBuffers.entrySet())
    {
      final GLTexture newT = new GLTexture(gl, entry.getValue().getNativeType(), -1, newWidth, newHeight, 1, true, 1, entry.getValue().getBitsPerChannel());

      newDepthBuffers.put(entry.getKey(), newT);
      entry.getValue().close();

      gl.glFramebufferTexture(GL4.GL_FRAMEBUFFER, GL4.GL_DEPTH_ATTACHMENT, newDepthBuffers.get(entry.getKey()).getId(), 0);
    }

    backingTextures.clear();
    depthBuffers.clear();

    backingTextures.putAll(newBackingTextures);
    depthBuffers.putAll(newDepthBuffers);

    width = newWidth;
    height = newHeight;

    gl.glDeleteFramebuffers(1, oldIds, 0);
    gl.glBindFramebuffer(GL4.GL_FRAMEBUFFER, 0);
  }

  public void destroy(GL4 gl)
  {
    gl.glDeleteFramebuffers(1, framebufferId, 0);
    gl.glBindFramebuffer(GL4.GL_FRAMEBUFFER, 0);

    for (GLTexture bt : backingTextures.values())
    {
      bt.delete();
    }

    for (GLTexture dt : depthBuffers.values())
    {
      dt.delete();
    }

    backingTextures.clear();
    depthBuffers.clear();
  }

  @Override
  public String toString()
  {
    StringBuilder info;
    if (!initialized)
    {
      info = new StringBuilder("GLFramebuffer (not initialized)\n");
    } else
    {
      info = new StringBuilder("GLFramebuffer " + framebufferId[0] + "\n|\n");

      for (final GLTexture att : backingTextures.values())
      {
        info.append(String.format("+-\tColor Attachment %s, %dx%d/%d*%d, 0x%s\n", Integer.toHexString(att.getId()), att.getWidth(), att.getHeight(), att.getChannels(), att.getBitsPerChannel(), Integer.toHexString(att.getInternalFormat())));
      }

      info.append("|\n");

      for (final GLTexture att : depthBuffers.values())
      {
        info.append(String.format("+-\tDepth Attachment %s, %dx%d/%d*%d, 0x%s\n", Integer.toHexString(att.getId()), att.getWidth(), att.getHeight(), att.getChannels(), att.getBitsPerChannel(), Integer.toHexString(att.getInternalFormat())));
      }
    }
    return info.toString();
  }

  public int getId()
  {
    if (initialized)
    {
      return framebufferId[0];
    } else
    {
      return -1;
    }
  }

  public int getWidth()
  {
    return this.width;
  }

  public int getHeight()
  {
    return this.height;
  }

  public int getBoundBufferNum()
  {
    return backingTextures.size() + depthBuffers.size();
  }

  private int getCurrentFramebufferColorAttachment()
  {
    return GL4.GL_COLOR_ATTACHMENT0 + backingTextures.size() - 1;
  }

  private int getCurrentFramebufferColorAttachment(final int base)
  {
    return GL4.GL_COLOR_ATTACHMENT0 + base - 1;
  }
}
