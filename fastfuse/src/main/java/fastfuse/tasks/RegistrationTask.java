package fastfuse.tasks;

import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;

import clearcl.ClearCLImage;
import clearcl.enums.ImageChannelDataType;
import fastfuse.FastFusionEngineInterface;
import fastfuse.registration.Registration;
import fastfuse.registration.RegistrationParameters;
import fastfuse.utils.smoothing.SimpleExponentialSmoothing;

import org.apache.commons.lang3.tuple.MutablePair;

/**
 * Stack registration. This task takes two images, and applies an affine
 * transform to the second in order to register it to the first. The output is
 * the transformed (second) image.
 *
 * @author royer, uschmidt
 */
public class RegistrationTask extends TaskBase
                              implements TaskInterface
{

  private SimpleExponentialSmoothing mSmoother =
                                               new SimpleExponentialSmoothing(6,
                                                                              0.1);

  private CopyOnWriteArrayList<RegistrationListener> mListenerList =
                                                                   new CopyOnWriteArrayList<>();

  private String[] mInputImagesSlotKeys;
  private String mTransformedImageSlotKey;

  private RegistrationParameters mRegistrationParameters =
                                                         new RegistrationParameters();
  private Registration mRegistration;

  /**
   * Instantiates a registered fusion task
   * 
   * @param pImageProcessedReferenceSlotKey
   *          first stack (reference volume for registration)
   * @param pImageProcessedToRegisterSlotKey
   *          second stack (volume to be registered to reference volume)
   * @param pImageOriginalReferenceSlotKey
   *          original/reference data for pImageProcessedReferenceSlotKey
   * @param pImageOriginalToRegisterSlotKey
   *          original/reference data for pImageprocessedToRegisterSlotKey, to
   *          be transformed after registration has been found
   * @param pImageOriginalToRegisterTransformedKey
   *          transformed version of pImageOriginalToRegisterSlotKey
   */
  public RegistrationTask(String pImageProcessedReferenceSlotKey,
                          String pImageProcessedToRegisterSlotKey,
                          String pImageOriginalReferenceSlotKey,
                          String pImageOriginalToRegisterSlotKey,
                          String pImageOriginalToRegisterTransformedKey)
  {
    super(pImageProcessedReferenceSlotKey,
          pImageProcessedToRegisterSlotKey,
          pImageOriginalReferenceSlotKey,
          pImageOriginalToRegisterSlotKey);
    mInputImagesSlotKeys = new String[]
    { pImageProcessedReferenceSlotKey,
      pImageProcessedToRegisterSlotKey,
      pImageOriginalReferenceSlotKey,
      pImageOriginalToRegisterSlotKey };
    mTransformedImageSlotKey = pImageOriginalToRegisterTransformedKey;

  }

  /**
   * Returns the registration parameters
   * 
   * @return registration parameters
   */
  public RegistrationParameters getParameters()
  {
    return mRegistrationParameters;
  }

  /**
   * Sets the temporal smoothing constant.
   * 
   * @param pSmoothingConstant
   *          smoothing constant
   */
  public void setSmoothingConstant(double pSmoothingConstant)
  {
    mSmoother.setAlpha(pSmoothingConstant);
  }

  @Override
  public boolean enqueue(FastFusionEngineInterface pFastFusionEngine,
                         boolean pWaitToFinish)
  {

    ClearCLImage lImageA, lImageB, lImageC, lImageD;
    lImageA = pFastFusionEngine.getImage(mInputImagesSlotKeys[0]);
    lImageB = pFastFusionEngine.getImage(mInputImagesSlotKeys[1]);
    lImageC = pFastFusionEngine.getImage(mInputImagesSlotKeys[2]);
    lImageD = pFastFusionEngine.getImage(mInputImagesSlotKeys[3]);

    assert TaskHelper.allSameDataType(ImageChannelDataType.Float,
                                      lImageA,
                                      lImageB,
                                      lImageC,
                                      lImageD);
    assert TaskHelper.allSameDimensions(lImageA,
                                        lImageB,
                                        lImageC,
                                        lImageD);

    if (mRegistration == null)
      mRegistration = new Registration(mRegistrationParameters,
                                       lImageA,
                                       lImageB);
    mRegistration.setImages(lImageA, lImageB);

    mRegistration.getParameters().setWaitToFinish(pWaitToFinish);

    double[] lBestTransform =
                            mRegistration.getParameters()
                                         .getInitialTransformation();
    try
    {
      // find best registration
      lBestTransform = mRegistration.register();

      mRegistration.setImages(lImageC, lImageD);
      double lBestScoreOriginalImages =
                                      mRegistration.computeScore(lBestTransform);
      System.out.printf("score = %.6f for best transformation on original images\n",
                        lBestScoreOriginalImages);

      notifyListenersOfNewScoreForComputedTheta(lBestScoreOriginalImages);

      // notify listeners
      notifyListenersOfNewComputedTheta(lBestTransform);

      // temporal smoothing of transformation parameters
      lBestTransform = mSmoother.update(lBestTransform);

      // use smoothed parameters as initial ones for next time
      mRegistrationParameters.setInitialTransformation(lBestTransform);

      lBestScoreOriginalImages =
                               mRegistration.computeScore(lBestTransform);
      System.out.printf("---\nscore = %.6f: %s\nfor smoothed transformation on original images\n",
                        lBestScoreOriginalImages,
                        Arrays.toString(lBestTransform));

      notifyListenersOfNewScoreForUsedTheta(lBestScoreOriginalImages);

    }
    catch (Throwable e)
    {
      System.err.println("Finding an updated volume registration failed (using last best transformation parameters instead).\n");

      e.printStackTrace();
    }

    MutablePair<Boolean, ClearCLImage> lFlagAndRegisteredImage =
                                                               pFastFusionEngine.ensureImageAllocated(mTransformedImageSlotKey,
                                                                                                      lImageA.getChannelDataType(),
                                                                                                      lImageA.getDimensions());

    ClearCLImage lRegisteredImage =
                                  lFlagAndRegisteredImage.getRight();
    mRegistration.transform(lRegisteredImage,
                            lImageD,
                            lBestTransform);
    // notify listeners
    notifyListenersOfNewUsedTheta(lBestTransform);

    lFlagAndRegisteredImage.setLeft(true);
    return true;
  }

  private void notifyListenersOfNewScoreForComputedTheta(double pScore)
  {
    for (RegistrationListener lRegistrationListener : mListenerList)
      lRegistrationListener.notifyListenersOfNewScoreForComputedTheta(pScore);
  }

  private void notifyListenersOfNewScoreForUsedTheta(double pScore)
  {
    for (RegistrationListener lRegistrationListener : mListenerList)
      lRegistrationListener.notifyListenersOfNewScoreForUsedTheta(pScore);
  }

  private void notifyListenersOfNewComputedTheta(double... theta)
  {
    for (RegistrationListener lRegistrationListener : mListenerList)
      lRegistrationListener.newComputedTheta(theta);
  }

  private void notifyListenersOfNewUsedTheta(double... theta)
  {
    for (RegistrationListener lRegistrationListener : mListenerList)
      lRegistrationListener.newUsedTheta(theta);
  }

  /**
   * Adds a registration listener
   * 
   * @param pRegistrationListener
   *          registration listener
   */
  public void addListener(RegistrationListener pRegistrationListener)
  {
    if (!mListenerList.contains(pRegistrationListener))
      mListenerList.add(pRegistrationListener);
  }

}
