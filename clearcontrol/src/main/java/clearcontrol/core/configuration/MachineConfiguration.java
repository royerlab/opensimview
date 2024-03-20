package clearcontrol.core.configuration;

import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.math.functions.UnivariateAffineFunction;
import clearcontrol.core.variable.bounded.BoundedVariable;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.math3.analysis.UnivariateFunction;

import java.io.*;
import java.util.*;

/**
 * MachineConfiguration is a singleton that can be accessed to query infromation
 * about the current system. Typically, it holds information such as the
 * assigned COM ports for serial devices and other configuration info that is
 * specific to a computer and its connected hardware.
 *
 * @author royer
 */
public class MachineConfiguration implements LoggingFeature
{
  private static final String cComments = "ClearControl machine configuration file";
  private static final MachineConfiguration sConfiguration = new MachineConfiguration();
  private static final ObjectMapper sObjectMapper = new ObjectMapper();

  /**
   * Returns the singleton instance of MachineConfiguration.
   *
   * @return singketon instance of MachineConfiguration
   */
  public static MachineConfiguration get()
  {
    return sConfiguration;
  }

  private Properties mProperties;

  private File mClearControlFolder;
  private File mPersistentVariablesFolder;

  /**
   * Constructs a MachineConfiguration (should be done only once).
   */
  private MachineConfiguration()
  {
    super();

    try
    {
      final String lUserHome = System.getProperty("user.home");
      final File lUserHomeFolder = new File(lUserHome);
      mClearControlFolder = new File(lUserHomeFolder, ".clearcontrol/");
      final boolean lFolderCreated = mClearControlFolder.mkdirs();
      if (lFolderCreated)
        System.out.println("ClearControl configuration file created at: "+mClearControlFolder.toString());

      mPersistentVariablesFolder = getFolder("PersistentVariables");

      final File lConfigurationFile = new File(mClearControlFolder, "configuration.txt");

      mProperties = new Properties();

      if (!lConfigurationFile.exists())
      {
        final Writer lWriter = new FileWriter(lConfigurationFile);
        mProperties.store(lWriter, cComments);
      }
      final FileInputStream lFileInputStream = new FileInputStream(lConfigurationFile);

      mProperties.load(lFileInputStream);
    } catch (final IOException e2)
    {
      e2.printStackTrace();
      mProperties = null;
    }
  }

  /**
   * Return properties used internally.
   *
   * @return properties object
   */
  public Properties getProperties()
  {
    return mProperties;
  }

  /**
   * Returns true if the properties contain a given key
   *
   * @param pKey key
   * @return true if contains key
   */
  public boolean containsKey(String pKey)
  {
    if (mProperties == null) return false;
    return mProperties.containsKey(pKey);
  }

  /**
   * Returns a string property for a given key
   *
   * @param pKey          key
   * @param pDefaultValue default string property
   * @return string property
   */
  public String getStringProperty(String pKey, String pDefaultValue)
  {
    if (mProperties == null)
    {
      // warning("Could not find entry %s, using default value %s\n",
      // pKey,
      // pDefaultValue);
      return pDefaultValue;
    }
    return mProperties.getProperty(pKey, pDefaultValue);
  }

  /**
   * Returns an integer property for a given key
   *
   * @param pKey          key
   * @param pDefaultValue default integer property
   * @return interger property
   */
  public Integer getIntegerProperty(String pKey, Integer pDefaultValue)
  {
    if (mProperties == null) return pDefaultValue;
    final String lProperty = mProperties.getProperty(pKey);
    if (lProperty == null)
    {
      warning("Could not find entry %s, using default value %d\n", pKey, pDefaultValue);
      return pDefaultValue;
    }

    return Integer.parseInt(lProperty);
  }

  /**
   * Returns a long property for a given key
   *
   * @param pKey          key
   * @param pDefaultValue default long property
   * @return long property
   */
  public Long getLongProperty(String pKey, Long pDefaultValue)
  {
    if (mProperties == null) return pDefaultValue;
    final String lProperty = mProperties.getProperty(pKey);
    if (lProperty == null)
    {
      warning("Could not find entry %s, using default value %d\n", pKey, pDefaultValue);
      return pDefaultValue;
    }

    return Long.parseLong(lProperty);
  }

  /**
   * Returns double property for a given key
   *
   * @param pKey          key
   * @param pDefaultValue default double value
   * @return double property
   */
  public Double getDoubleProperty(String pKey, Double pDefaultValue)
  {
    if (mProperties == null) return pDefaultValue;
    final String lProperty = mProperties.getProperty(pKey);
    if (lProperty == null)
    {
      // warning("Could not find entry %s, using default value %g\n",
      // pKey,
      // pDefaultValue);
      return pDefaultValue;
    }

    return Double.parseDouble(lProperty);
  }

  /**
   * Returns boolean proprty for a given key
   *
   * @param pKey          key
   * @param pDefaultValue default boolean value
   * @return boolean property
   */
  public boolean getBooleanProperty(String pKey, Boolean pDefaultValue)
  {
    if (mProperties == null) return pDefaultValue;
    final String lProperty = mProperties.getProperty(pKey);
    if (lProperty == null)
    {
      // warning("Could not find entry %s, using default value %s\n",
      // pKey,
      // pDefaultValue ? "true" : "false");
      return pDefaultValue;
    }

    return Boolean.parseBoolean(lProperty.toLowerCase()) || lProperty.trim().equals("1") || lProperty.trim().equalsIgnoreCase("on") || lProperty.trim().equalsIgnoreCase("present") || lProperty.trim().equalsIgnoreCase("true");
  }

  /**
   * Returns file property for a given key
   *
   * @param pKey         key
   * @param pDefaultFile default file
   * @return file property
   */
  public File getFileProperty(String pKey, File pDefaultFile)
  {
    String lStringProperty = getStringProperty(pKey, null);

    if (lStringProperty == null)
    {
      // warning("Could not find entry %s, using default value %s\n",
      // pKey,
      // pDefaultFile.getAbsolutePath());
      return pDefaultFile;
    }

    return new File(lStringProperty);
  }

  /**
   * Returns serial device port for a given device name and device index
   *
   * @param pDeviceName  device name
   * @param pDeviceIndex device index
   * @param pDefaultPort default port
   * @return serial device port
   */
  public String getSerialDevicePort(String pDeviceName, int pDeviceIndex, String pDefaultPort)
  {
    final String lKey = "device.serial." + pDeviceName + "." + pDeviceIndex;
    return getStringProperty(lKey, pDefaultPort);
  }

  /**
   * Returns a networ device host name and port for a given device name and
   * index
   *
   * @param pDeviceName             device name
   * @param pDeviceIndex            device index
   * @param pDefaultHostNameAndPort default host name amd port
   * @return hostname and port
   */
  public String[] getNetworkDeviceHostnameAndPort(String pDeviceName, int pDeviceIndex, String pDefaultHostNameAndPort)
  {
    final String lKey = "device.network." + pDeviceName + "." + pDeviceIndex;
    final String lHostnameAndPort = getStringProperty(lKey, pDefaultHostNameAndPort);
    return lHostnameAndPort.split(":");
  }

  /**
   * Returns IO device port for given device name
   *
   * @param pDeviceName  device name
   * @param pDefaultPort default IO port
   * @return IO device port
   */
  public Integer getIODevicePort(String pDeviceName, Integer pDefaultPort)
  {
    final String lKey = "device." + pDeviceName;
    return getIntegerProperty(lKey, pDefaultPort);
  }

  /**
   * Returns true if the given device is present
   *
   * @param pDeviceName  device name
   * @param pDeviceIndex device index
   * @return true if device present
   */
  public boolean getIsDevicePresent(String pDeviceName, int pDeviceIndex)
  {
    final String lKey = "device." + pDeviceName + "." + pDeviceIndex;
    return getBooleanProperty(lKey, false);
  }

  /**
   * Returns a list of values given a prefix key. Keys have the format:
   * prefix.0, prefix.1, prefix.2, ... prefix.n
   *
   * @param pPrefix prefix
   * @return list of values (strings)
   */
  public ArrayList<String> getList(String pPrefix)
  {
    final ArrayList<String> lList = new ArrayList<>();
    for (int i = 0; i < Integer.MAX_VALUE; i++)
    {
      final String lKey = pPrefix + "." + i;
      final String lProperty = mProperties.getProperty(lKey, null);
      if (lProperty == null) break;
      lList.add(lProperty);
    }
    return lList;
  }

  /**
   * Returns a folder within the clearcontrol folder (.clearcontrol). The folder
   * is created if it does not exist.
   *
   * @param pFolderName folder name
   * @return folder
   */
  public File getFolder(String pFolderName)
  {
    File lFolder = new File(mClearControlFolder, pFolderName);
    lFolder.mkdirs();
    return lFolder;
  }

  /**
   * Returns the folder holding any persistency information
   *
   * @return persistency folder
   */
  public File getPersistencyFolder()
  {
    return mPersistentVariablesFolder;
  }

  /**
   * Returns the file for persisting a variable with given name
   *
   * @param pVariableName variable name
   * @return file
   */
  public File getPersistentVariableFile(String pVariableName)
  {
    return new File(getPersistencyFolder(), pVariableName);
  }

  /**
   * Returns a univariate affine function given a function name
   *
   * @param pFunctionName function name
   * @return affine function
   */
  public UnivariateAffineFunction getUnivariateAffineFunction(String pFunctionName)
  {
    String lAffineFunctionString = getStringProperty(pFunctionName, null);

    if (lAffineFunctionString == null)
    {
      warning("Cannot find following function def in configuration file: " + pFunctionName);
      UnivariateAffineFunction lUnivariateAffineFunction = new UnivariateAffineFunction(1, 0);
      return lUnivariateAffineFunction;
    }

    TypeReference<HashMap<String, Double>> lTypeReference = new TypeReference<HashMap<String, Double>>()
    {
    };

    try
    {
      HashMap<String, Double> lMap = sObjectMapper.readValue(lAffineFunctionString, lTypeReference);

      UnivariateAffineFunction lUnivariateAffineFunction = new UnivariateAffineFunction(lMap.get("a"), lMap.get("b"));

      return lUnivariateAffineFunction;

    } catch (IOException e)
    {
      e.printStackTrace();
      return null;
    }

  }

  /**
   * Sets the bounds for a given variable.
   *
   * @param pBoundsName bounds name
   * @param pVariable   variable
   */
  @SuppressWarnings("unchecked")
  public <T extends Number, F extends UnivariateFunction> void getBoundsForVariable(String pBoundsName, BoundedVariable<T> pVariable)
  {
    getBoundsForVariable(pBoundsName, pVariable, (T) new Double(-100), (T) new Double(100));
  }

  /**
   * Sets the bounds for a given variable.
   *
   * @param pBoundsName bounds name
   * @param pVariable   variable
   * @param pDefaultMin default min
   * @param pDefaultNax default max
   */
  public <T extends Number, F extends UnivariateFunction> void getBoundsForVariable(String pBoundsName, BoundedVariable<T> pVariable, T pDefaultMin, T pDefaultNax)
  {
    String lAffineFunctionString = getStringProperty(pBoundsName, null);

    if (lAffineFunctionString == null)
    {
      warning("Cannot find following bounds def in configuration file: " + pBoundsName);
      pVariable.setMinMax(pDefaultMin, pDefaultNax);

      return;
    }

    TypeReference<HashMap<String, Double>> lTypeReference = new TypeReference<HashMap<String, Double>>()
    {
    };

    try
    {
      HashMap<String, Double> lMap = sObjectMapper.readValue(lAffineFunctionString, lTypeReference);

      Double lMin = lMap.get("min");
      Double lMax = lMap.get("max");

      if (lMin == null || lMax == null)
      {
        warning("Cannot find following bounds def in configuration file: " + pBoundsName);
        pVariable.setMinMax(-100.0, 100.0);
        return;
      }

      pVariable.setMinMax(lMin, lMax);

      Double lGranularity = lMap.get("granularity");
      if (lGranularity == null) pVariable.setGranularity(0);
      else pVariable.setGranularity(lGranularity);

      return;

    } catch (IOException e)
    {
      e.printStackTrace();
    }

  }


  public void clearProperties(String pPrefixFilter, String pPostfixFilter)
  {
    HashSet<String> lKeysToClearSet = new HashSet<>();

    for (Map.Entry<Object, Object> lEntry : mProperties.entrySet())
    {
      String lKey = (String) lEntry.getKey();
      if (lKey.startsWith(pPrefixFilter) && lKey.endsWith(pPostfixFilter)) lKeysToClearSet.add(lKey);
    }

    for (String lKey : lKeysToClearSet)
    {
      mProperties.remove(lKey);
    }
  }
}
