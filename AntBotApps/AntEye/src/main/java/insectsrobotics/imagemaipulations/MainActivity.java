package insectsrobotics.imagemaipulations;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import insectrobotics.broadcastlibrary.BroadcastValues;
import insectsrobotics.imagemaipulations.Receiver_and_Broadcaster.Broadcast;
import insectsrobotics.imagemaipulations.Receiver_and_Broadcaster.Receive;

import static java.lang.Thread.sleep;
import static org.opencv.imgproc.Imgproc.GaussianBlur;
import static org.opencv.imgproc.Imgproc.INTER_LANCZOS4;
import static org.opencv.imgproc.Imgproc.INTER_LINEAR;
import static org.opencv.imgproc.Imgproc.goodFeaturesToTrack;
import static org.opencv.imgproc.Imgproc.remap;
import static org.opencv.imgproc.Imgproc.resize;
import static org.opencv.video.Video.calcOpticalFlowFarneback;
import static org.opencv.video.Video.calcOpticalFlowPyrLK;


public class MainActivity extends Activity implements CvCameraViewListener2 , BroadcastValues{
    private static final String TAG = "OCVSample::Activity";
    boolean opticCheck = false;
    public Mat processedSourceImage;
    public Mat processedDestImage;
    public Mat displayedImage;
    Button learnImageBtn;
    Button calcErrorBtn;
    int counting =0;
    Mat imageToDisplay;
    Mat imageToCompare;
    Mat flow;
    Mat pastFlow;
    //Receiver and Broadcaster
    Broadcast broadcast;
    Receive receive;
    //Image processing variables
    Mat BlueChannel;
    MatOfInt from_to;
    Mat rgba;
    List<Mat> rgbaList;
    List<Mat> BlueChannelList;
    double[] pixel;
    String myTag ="Canny";
    boolean check = false;
    String flowTag="flow";
    //AdvancedSettings Data
    Bundle advancedSettingsBundle;
    Mat[] storedPics = new Mat[4];



    //Camera Parameters
    double imageCorrection;
    int cameraViewStartedWidth = 0;
    int cameraViewStartedHeight = 0;


    //Calibration resulting variables
    Bundle mBundle;
    double[] theta_new = new double[40];
    int[][][] directoryArray = new int[(theta_new.length + 1)][360][2];


    //Unwrapping variables
    int sourceResolution = 1;
    int resolution = 4;

    //Optical flow variables
    Mat first_frame=null;
    Mat second_frame;
    double opticalSumLimit =50;


    //Background processor
    AsyncTask<Object, Integer, Boolean> broadcastImage = null;


    //Module Selection from StartScreen
    String visualModule;
    String pathIntegratorModule;
    String combinerModule;
    boolean notification = true;

    //Layout Views
    TextView serialConnectionTextView;
    TextView serverConnectionTextView;
    ProgressBar serialProgressBar;
    ProgressBar serverProgressBar;
    ImageView serialCheckImageView;
    ImageView serverCheckImageView;

    //DEBUG variables to save and load images from txt files
    boolean saveImages = false;
    boolean loadImages = false;
    int numberOfImages = 0;
    int errorCounter = 0;
    int numberOfErrorCalculations = 50;
    int errorCounter100 = 0;
    Handler learnHandler = new Handler();
    File sdCard = Environment.getExternalStorageDirectory();
    File dir = new File(sdCard.getAbsolutePath() + "/");
    File file = new File(dir, "text.txt");

    //*** Camera calibration data
    double R1 = 104.7; //radius of the inner circle
    double R2 = 217.2; //radius of the outer circle
    double centreX = 527; // X coordinate of the image centre
    double centreY = 406; // Y coordiate of the image centre
    double[] affine = new double[]{1.000329,0.000171,-0.000339,1.0}; // Affine parameters c, d, e and 1
    double[] polynom = new double[]{-141.742948747035,0,-0.000022888213756,0.000010694034632,0.000000001664105}; // polynomial coefficients
    Mat affineMat;

    //** Unwrapped image parameters
    double unwrapHeight = 0;
    double unwrapWidth = 0;
    Mat imageMapX;
    Mat imageMapY;
    double scale =1;

    MatOfPoint initial;
    MatOfByte status;
    MatOfFloat err;
    MatOfPoint2f prevPts;
    MatOfPoint2f nextPts;
    Mat mGray1 = null;
    Mat mGray2;
    double leftFlow=0;
    double rightFlow=0;
    double centreFlow=0;
    double prevLeft=0;
    double prevRight=0;
    double prevCentre=0;

    double xTotal=0;
    boolean beginTurn=false;
    boolean sec_check=false;


    //Visual Homing
    public Mat snapShot;
    public Mat fullSnapShot;
    public Mat fullImageToDisplay;
    public Mat temporaryImageToCompare;
    List<Mat> rotatedImageDB;
    boolean savedList=false;
    boolean stopThread=false;
    Thread startHoming;
    Thread runDown;
    Thread turnToMinimum;
    Thread startSearching;
    Thread search;

    //PI
    Thread obstacleAvoid;
    Thread optThread;
    Thread denseOpt;
    Thread outboundStart;
    int direction=0;
    int integratorRunTime=35000; //outbound time (milliseconds)
    boolean outboundStop =false;

    Thread testThread;
    int selectedModule=0;

    //Initiate all ServiceConnections for all Background Services
    ServiceConnection visualNavigationServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(myTag,"VisualNavigationService Started");

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };
    ServiceConnection serialServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };
    ServiceConnection combinerServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };
    ServiceConnection integratorServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    StringBuilder errorString = new StringBuilder();
    StringBuilder perfectMemoryErrorString = new StringBuilder();

    //See learnButton onClickListener
    Runnable learnRunnable = new Runnable() {
        @Override
        public void run() {
            if (!saveImages && !loadImages) {
                errorCounter = 0;
            } else {
                errorCounter = numberOfErrorCalculations;
            }
            //learnImage = true;
            //transmissionRunning = false;
            notification = true;
            errorString = new StringBuilder();
            perfectMemoryErrorString = new StringBuilder();
            calcErrorBtn.setVisibility(View.VISIBLE);
        }
    };


    //The UI Views
    private CameraBridgeViewBase mOpenCvCameraView;
    //Start OpenCV, open CameraBridge
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    imageToDisplay=Mat.zeros(10,90,CvType.CV_8UC1);
                    // Set Camera parameters
                    unwrapHeight = scale*(R2-R1);
                    unwrapWidth = scale*2*Math.PI*(R1+R2)/2;
                    imageMapX = Mat.zeros(((int)unwrapHeight),((int) unwrapWidth),CvType.CV_32FC1);
                    imageMapY = Mat.zeros(((int)unwrapHeight),((int) unwrapWidth),CvType.CV_32FC1);
                    affineMat = new Mat(2,2,CvType.CV_32FC1);
                    //Log.i(myTag, "size: "+ imageMap[0].length +" X "+ imageMap[1].length);
                    unwrapMap();

                    mGray2 = Mat.zeros(theta_new.length / sourceResolution, 360 / sourceResolution, CvType.CV_8UC1);
                    second_frame= Mat.zeros(theta_new.length / resolution, 360 / resolution,CvType.CV_8UC1);
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };


    /**
     * Declare a Receiver and its Listener. The Receiver implements the Listener. On a receive with
     * an intent with the in the IntentFilter declared action, the Listeners onReceive Method is
     * called.
     */

    Receive.ReceiveListener receiveListener = new Receive.ReceiveListener() {
        @Override
        public void onNewMessageFromSerialConnectionApp(Intent intent, String action) {
            if (action.equals(SERIAL_CONNECTION_ESTABLISHED)){
                if (intent.getBooleanExtra("MainData", false)){
                    serialProgressBar.setVisibility(View.GONE);
                    serialCheckImageView.setVisibility(View.VISIBLE);
                } else {
                    serialProgressBar.setVisibility(View.VISIBLE);
                    serialCheckImageView.setVisibility(View.GONE);
                }
            } else if (action.equals(SERVER_CONNECTION_ESTABLISHED)){
                if (intent.getBooleanExtra("MainData", false)) {
                    serverProgressBar.setVisibility(View.GONE);
                    serverCheckImageView.setVisibility(View.VISIBLE);
                } else {
                    serverProgressBar.setVisibility(View.VISIBLE);
                    serverCheckImageView.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public void onNewMessageFromVisualNavigationApp(Intent intent, String action) {
            if (action.equals(NUMBER_OF_IMAGES)){
                numberOfImages = intent.getIntExtra("MainData", 0);
            }
            /*else if (action.equals(STATUS_UPDATE)){
                visualNavigationRunning = intent.getBooleanExtra("MainData", false);
            }*/
        }

        @Override
        public void onNewMessageFromCombinerApp(Intent intent, String action) {
            switch (action){
                case TURN_AE_DATA:
                    try {
                        turnAround(intent.getDoubleExtra("MainData",0));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
        @Override
        public void onNewMessageFromIntegratorApp(Intent intent, String action) {

        }
        @Override
        public void onNewMessageFromAntEyeApp(Intent intent, String action) {
        }
    };

    //MainActivity Constructor
    public MainActivity() {
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("MainActivity", "Main Activity starts");
        //First Bind all other services from apps to this one

        Intent visualNavigationServiceIntent = new Intent(VISUAL_SERVICE);
        bindService(visualNavigationServiceIntent, visualNavigationServiceConnection, BIND_AUTO_CREATE);

        Intent combinerServiceIntent = new Intent(COMBINER_SERVICE);
        bindService(combinerServiceIntent, combinerServiceConnection, BIND_AUTO_CREATE);

        Intent integratorServiceIntent = new Intent(INTEGRATOR_SERVICE);
        bindService(integratorServiceIntent, integratorServiceConnection, BIND_AUTO_CREATE);

        Intent serialServiceIntent = new Intent(SERIAL_SERVICE);
        bindService(serialServiceIntent, serialServiceConnection, BIND_AUTO_CREATE);


        //Initiate all Receiver and Broadcaster
        broadcast = new Broadcast(this,getResources());
        receive = new Receive(receiveListener);
        IntentFilter intentFilter = receive.getIntentFilter();
        registerReceiver(receive, intentFilter);

        //Inflate Layout and initiate the Views
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_image_manipulations);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById
                (insectsrobotics.imagemaipulations.Build.R.id.main_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);


        serialConnectionTextView = (TextView) findViewById(R.id.serialConnectionTextView);
        serverConnectionTextView = (TextView) findViewById(R.id.serverConnectionTextView);
        serialProgressBar = (ProgressBar) findViewById(R.id.serialProgressBar);
        serverProgressBar = (ProgressBar) findViewById(R.id.serverProgressBar);
        serialCheckImageView = (ImageView) findViewById(R.id.serialCheckImageView);
        serverCheckImageView = (ImageView) findViewById(R.id.serverCheckImageView);
        serialCheckImageView.setImageResource(R.drawable.checksymbol);
        serverCheckImageView.setImageResource(R.drawable.checksymbol);


        //Loads calibration_layout data from passed Intent (Coming from StartScreen) and Advanced Settings
        Intent intent = getIntent();
        //Get Advanced Settings selection:
        advancedSettingsBundle = intent.getBundleExtra("ServerConnection");
        Log.e(TAG,"ServerAddress: " + advancedSettingsBundle.get("ServerAddress"));
        //Get Data from Intent
        mBundle = intent.getBundleExtra("Data");
        //Get Module Selection from Bundle
        visualModule = mBundle.getString(VN_MODULE, NO_MODULE);
        pathIntegratorModule = mBundle.getString(PI_MODULE, NO_MODULE);
        combinerModule = mBundle.getString(C_MODULE, NO_MODULE);
        selectedModule=mBundle.getInt("selectModule",0);

        //DEBUG: Create new File for error output
        try {
            FileOutputStream f = new FileOutputStream(file);
            f.write(("Measurements: " + "\n").getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }





    }

    //This has to be done since openCV crops images before the go in the "onNewFrame"-Method.
    //Therefore we have to adjust the View to the Camera parameters of the phone
    private void setCameraViewSize() {
        //Get Camera parameters (0 = BackCamera, 1= FrontCamera)
        Camera mCamera = Camera.open(1);
        Camera.Parameters params = mCamera.getParameters();
        Camera.Size pictureSize = params.getPictureSize();
        int pictureHeight = pictureSize.height;
        int pictureWidth = pictureSize.width;
        mCamera.release();

        Log.d(TAG, "Picture Height = " + pictureHeight + " Width = " + pictureWidth);

        //Used to set the Camera View accordingly
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        double width = size.x;

        //openCv also turns the image by 90°, so here the View is adjusted to the new parameters
        mOpenCvCameraView.getLayoutParams().height = (int) (width / (pictureWidth / pictureHeight));
        imageCorrection = pictureWidth;
    }

    /**
     * Disables CameraBridge/ view on activity pause to prevent memory leaks
     */
    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    /**
     * Enables view again on resume of the activity
     */
    @Override
    public void onResume() {

        super.onResume();
        Log.d("onResume", "onResume called");
        imageCorrection = 0;
        //transmissionRunning = false;
        //visualNavigationRunning = false;

        //setCameraViewSize();

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        if (cameraViewStartedWidth != 0 && cameraViewStartedHeight != 0) {
            onCameraViewStarted(cameraViewStartedWidth, cameraViewStartedHeight);
        }

    }

    /**
     * Disables view to prevent memory leaks after the activity was destroyed
     */
    public void onDestroy() {
        super.onDestroy();

        unbindService(visualNavigationServiceConnection);
        unbindService(combinerServiceConnection);
        unbindService(serialServiceConnection);
        unbindService(integratorServiceConnection);
        unregisterReceiver(receive);
        stopThread=true;
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    /**
     * Called after successful initiation of the camera connection, initiates Variables, sets up
     * a connection array between the "Donut-View" and the unwrapped image.
     */

    public void onCameraViewStarted(int width, int height) {
        Log.e(TAG, "onCameraViewStarted called");

        //Send chosen modules
        broadcast.broadcastModules(visualModule, pathIntegratorModule, combinerModule);
        broadcast.broadcastServerConnection(advancedSettingsBundle);


        cameraViewStartedWidth = width;
        cameraViewStartedHeight = height;





        broadcastImage = new BroadcastImage();


    }

    public void onCameraViewStopped() {
        //optThread.interrupt();

    }

    /**
     *  Building a Map to unwrap the image
     */
    private void unwrapMap(){
        double r;
        double theta;
        int xS;
        int yS;
        int l=0;
        Mat temp= Mat.zeros(2,2,CvType.CV_32FC1);


        for(int x=0;x<affineMat.rows();x++){
            for(int y=0;y<affineMat.cols();y++){

                affineMat.put(x,y,affine[l]);
                l++;
            }
        }

        for(int y=0; y<(int)unwrapHeight; y++){
            for(int x=0; x<(int)unwrapWidth; x++){
                r = ((double)y/unwrapHeight)*(R2-R1)+R1;
                theta = ((double)x/ unwrapWidth) * 2 * Math.PI;
                xS = (int) (centreX + r*Math.sin(theta));
                yS = (int) (centreY + r*Math.cos(theta));

                temp.put(0, 0, xS);
                temp.put(1, 0, yS);
                Core.gemm(affineMat.inv(),temp,1,new Mat(),0,temp,0);
                xS=(int)(temp.get(0,0)[0]);
                yS=(int)temp.get(1,0)[0];


                imageMapX.put(y, x, xS);
                imageMapY.put(y,x,yS);

            }
        }
    }


    /**
     * openCv Method with inputFrame from FrontCamera, imageProcessing and output to display.
     * At the same time the endless loop to do more or less all the work.
     *
     * @param inputFrame Frame from the cameraViewListener
     * @return the displayed Image for the Screen. Important: the returned Image has to be the same
     * size as the inputFrame!
     */
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        //Initiation of the needed Variables
        rgbaList = new ArrayList<>();
        BlueChannelList = new ArrayList<>();
        from_to = new MatOfInt(2, 0);
        rgba = new Mat();

        processedSourceImage = Mat.zeros(theta_new.length / sourceResolution, 360 / sourceResolution, CvType.CV_8UC1);
        processedDestImage = Mat.zeros(theta_new.length / resolution, 360 / resolution, processedSourceImage.type());
        Mat tempRotate = Mat.zeros(processedSourceImage.size(), processedSourceImage.type());
        fullImageToDisplay=Mat.zeros(processedSourceImage.size(),processedSourceImage.type());




        rgba = inputFrame.rgba();                                           //Input Frame in rgba format
        BlueChannel = new Mat(rgba.rows(), rgba.cols(), CvType.CV_8UC1);    //Mat for later image processing

        Mat unwrappedImg =  Mat.zeros((int) unwrapHeight, (int) unwrapWidth, CvType.CV_8UC1);


        double scale = rgba.width() / (360 / resolution);
        double rgbaHeight = rgba.height();
        displayedImage = Mat.zeros((int) (rgbaHeight / scale), 360 / resolution, CvType.CV_8UC1);

        rgbaList.add(rgba);                                                 //Needed for channel extraction from rgba image
        BlueChannelList.add(BlueChannel);                                   //Needed for channel extraction from rgba image
        Core.mixChannels(rgbaList, BlueChannelList, from_to);               //Extract only the blue channel from rgba

        remap(BlueChannel, unwrappedImg, imageMapX, imageMapY, INTER_LINEAR);
        resize(unwrappedImg, tempRotate, processedSourceImage.size(), 2.81, 2.81, INTER_LANCZOS4);

        int counter1 = 0;
        int counter3;
        int colPosition;

        for (int phi = 0; phi < 360; phi = phi + sourceResolution) {
            counter3 = 0;
            for (int theta_tmp = 0; theta_tmp < theta_new.length; theta_tmp = theta_tmp + sourceResolution) {
                colPosition=counter1+180;
                if(colPosition>=360){
                    colPosition=colPosition - 360;
                }
                pixel = tempRotate.get(counter3,colPosition);
                processedSourceImage.put(counter3, counter1, pixel);
                counter3++;
            }
            counter1++;
        }

        Mat serverImage = new Mat(processedSourceImage.size(),processedSourceImage.type());
        Core.flip(processedSourceImage,serverImage, -1);
        broadcast.broadcastImageForServer(serverImage);

        /**
         * If the output resolution is different from 1x1° we have to down-sample the image.
         * openCVs RegionOfInterest method is perfect for this cause.
         *
         */

        if (resolution != 1) {

            int destAzimuthCounter = 0;
            for (int azimuth = 0; azimuth < processedSourceImage.cols(); azimuth = azimuth + resolution) {
                int destElevationCounter = 0;
                for (int elevation = 0; elevation < processedSourceImage.rows(); elevation = elevation + resolution) {
                    Rect roi;
                    //New Rectangle with the target resolution, later the ROI of the frame.
                    roi = new Rect(azimuth, elevation, resolution, resolution);
                    if (processedSourceImage.cols() - azimuth < resolution) {
                        roi = new Rect(azimuth, elevation, processedSourceImage.cols() - azimuth, resolution);
                    }
                    if (processedSourceImage.rows() - elevation < resolution) {
                        roi = new Rect(azimuth, elevation, resolution, processedSourceImage.rows() - elevation);
                    }
                    //Getting the pixels of the Region of Interest and averaging the values.
                    Mat ROI = processedSourceImage.submat(roi);
                    int ROIMean = (int) Core.mean(ROI).val[0];
                    processedDestImage.put(destElevationCounter, destAzimuthCounter, ROIMean);
                    destElevationCounter++;

                }
                destAzimuthCounter++;
            }

        }
        /**
         * If the output resolution is the same as the source resolution, we can use the same Image
         */
        else {
            processedDestImage = processedSourceImage;
        }

        Imgproc.equalizeHist(processedDestImage, processedDestImage);
        GaussianBlur(processedDestImage, processedDestImage, new Size(3, 3), 0, 0);
        broadcastImage = new BroadcastImage();
        broadcastImage.execute(processedDestImage);

        processedDestImage.copyTo(second_frame);
        processedSourceImage.copyTo(fullImageToDisplay);

        processedDestImage.copyTo(imageToDisplay);

        if(!opticCheck && counting>7){
            //processedDestImage.copyTo(second_frame);
            temporaryImageToCompare=Mat.zeros(processedDestImage.size(),processedDestImage.type());
            opticCheck=true;
            fullSnapShot=Mat.zeros(processedSourceImage.size(),processedSourceImage.type());
            snapShot=Mat.zeros(processedDestImage.size(),processedDestImage.type());
            //processedDestImage.rowRange(0,processedDestImage.rows()).colRange(8, 37).copyTo(snapShot.rowRange(0, snapShot.rows()).colRange(8, 37));

            processedDestImage.copyTo(snapShot);
            processedSourceImage.copyTo(fullSnapShot);
            rotatedImageDB=new ArrayList<>();
            //testThread=new Thread(translational);
            //testThread.start();
            if(selectedModule==0){
                // Starting all threads for PI

                obstacleAvoid=new Thread(obstacleAvoidance);
                obstacleAvoid.start();
                optThread=new Thread(opticalFlowLK);
                optThread.start();
                outboundStart=new Thread(startInbound);
                outboundStart.start();
                denseOpt = new Thread(opticalFlowFB);
                denseOpt.start();
            }else{
                switch(visualModule){
                    case RUNDOWN:
                        // Starting all RunDownThreads
                        startHoming=new Thread(startHome);
                        startHoming.start();
                        runDown=new Thread(saveImagesRunDown);
                        runDown.start();
                        break;
                    case TURN_TO_MIN:
                        //Starting all Turn to minimum Threads
                        startHoming=new Thread(startHome);
                        startHoming.start();
                        turnToMinimum=new Thread(saveImagesTurnMinimum);
                        turnToMinimum.start();
                        break;
                    case SYSTEMATIC_SEARCH:
                        //Starting all search threads
                        startSearching=new Thread(startSearch);
                        startSearching.start();
                        search=new Thread(saveImagesRunDown);
                        search.start();
                        break;
                    default:
                        // Starting all RunDownThreads
                        startHoming=new Thread(startHome);
                        startHoming.start();
                        runDown=new Thread(saveImagesRunDown);
                        runDown.start();
                        break;
                }
            }

            imageToCompare= Mat.zeros(imageToDisplay.size(), imageToDisplay.type());

            Log.i(flowTag,"Snapshot taken");

        }
        sec_check=true;
        counting++;
        if(counting==20)counting=0;

        //Here we put the processed image back into a Mat with the proportions of the output image
        processedDestImage.copyTo(displayedImage.rowRange(displayedImage.rows() - processedDestImage.rows(), displayedImage.rows())
                .colRange(0, displayedImage.cols()));

        //Resizing of the Image to fit the size of the JavaImageView
        Size size = new Size(rgba.width(), rgba.height());
        Imgproc.resize(displayedImage, displayedImage, size);
        return displayedImage;

    }


    //--------------------




    //------------------

    //Rotating the 360X40 imaage, then down-sample to 90X10
    private Mat rotateFullImage(Mat pic, int angle){
        Mat processedMat;
        processedMat=Mat.zeros(processedDestImage.size(),processedDestImage.type());
        int counter1;
        int counter3=0;
        int colPosition;
        Mat tempRotate;
        double[] pixelNew;

        tempRotate=new Mat(fullSnapShot.size(),fullSnapShot.type());
        for (int theta_tmp = 0; theta_tmp < 40; theta_tmp = theta_tmp + 1) {
            counter1 = 0;
            for (int phi = 0; phi < 360; phi = phi + 1) {
                colPosition=counter1+angle;
                if(colPosition>=360){
                    colPosition=colPosition - 360;
                }
                pixelNew = pic.get(counter3,colPosition);
                tempRotate.put(counter3, counter1, pixelNew);

                counter1++;
            }
            counter3++;
        }

        //downsampling
        int destAzimuthCounter = 0;
        for (int azimuth = 0; azimuth < tempRotate.cols(); azimuth = azimuth + 4) {
            int destElevationCounter = 0;
            for (int elevation = 0; elevation < tempRotate.rows(); elevation = elevation + 4) {
                Rect roi;
                //New Rectangle with the target resolution, later the ROI of the frame.
                roi = new Rect(azimuth, elevation, 4, 4);
                if (tempRotate.cols() - azimuth < 4) {
                    roi = new Rect(azimuth, elevation, tempRotate.cols() - azimuth, 4);
                }
                if (tempRotate.rows() - elevation < 4) {
                    roi = new Rect(azimuth, elevation, 4, tempRotate.rows() - elevation);
                }
                //Getting the pixels of the Region of Interest and averaging the values.
                Mat ROI = tempRotate.submat(roi);
                int ROIMean = (int) Core.mean(ROI).val[0];
                processedMat.put(destElevationCounter, destAzimuthCounter, ROIMean);
                destElevationCounter++;

            }
            destAzimuthCounter++;
        }

        //resize(processedSourceImage,processedDestImage,processedDestImage.size(),4,4,INTER_LANCZOS4);
        Imgproc.equalizeHist(processedMat, processedMat);

        GaussianBlur(processedMat, processedMat, new Size(3, 3), 0, 0);
        broadcast.broadcastRotatedImage(processedMat);
        return processedMat;
    }

    //Rotate the 360X40 snapshot
    private void rotateSnapShot(){
        int counter1 = 0;
        int counter3;
        int colPosition;
        Mat tempRotate;
        tempRotate=Mat.zeros(fullSnapShot.size(),fullSnapShot.type());
        for (int phi = 0; phi < 360; phi = phi + 1) {
            counter3 = 0;
            for (int theta_tmp = 0; theta_tmp < 40; theta_tmp = theta_tmp + 1) {
                colPosition=counter1+180;
                if(colPosition>=360){
                    colPosition=colPosition - 360;
                }
                double[] pixelNew = fullSnapShot.get(counter3,colPosition);
                tempRotate.put(counter3, counter1, pixelNew);
                counter3++;
            }
            counter1++;
        }
        tempRotate.copyTo(fullSnapShot);

    }

    /**
     * Sparse optical flow for tracking change in orientation
     */
    Runnable opticalFlowLK = new Runnable(){
        MatOfPoint2f prevPtsRecalc;
        int detectFeature=0;
        @Override
        public void run() {

            for(;;){
                second_frame.copyTo(mGray2);
                if(mGray1==null){
                    mGray1 = Mat.zeros(mGray2.size(), CvType.CV_8UC1);
                    mGray2.copyTo(mGray1);
                    initial = new MatOfPoint();
                    status = new MatOfByte();
                    err = new MatOfFloat();
                    prevPts = new MatOfPoint2f();
                    nextPts = new MatOfPoint2f();
                    prevPtsRecalc=new MatOfPoint2f();
                    goodFeaturesToTrack(mGray1, initial, 100, 0.5, 3);
                    initial.convertTo(prevPts, CvType.CV_32FC2);


                }else{
                    int counter3;
                    int counter1=0;
                    int count =0;
                    double diffX;
                    double xAngle =0;
                    Mat temp = new Mat(mGray2.size(),mGray2.type());

                    mGray2.copyTo(temp);
                    calcOpticalFlowPyrLK(mGray1, temp, prevPts, nextPts, status, err, new Size(9, 9), 2);
                    calcOpticalFlowPyrLK(temp, mGray1, nextPts,prevPtsRecalc, status,err,new Size(9,9),2);


                    for (int cols = 0; cols < nextPts.cols(); cols++) {
                        counter3 = 0;
                        for (int rows = 0; rows < nextPts.rows(); rows++) {
                            if(status.get(counter3,counter1)[0]==1 && (Math.abs(prevPts.get(counter3,counter1)[0]-prevPtsRecalc.get(counter3,counter1)[0])<1)) {
                                diffX=nextPts.get(counter3, counter1)[0]-prevPts.get(counter3, counter1)[0];

                                if(direction==1){
                                    if(diffX<-1){
                                        diffX=0;
                                        count--;
                                    }
                                }else if(direction==2){
                                    if(diffX>1) {
                                        diffX = 0;
                                        count--;
                                    }
                                }
                                xAngle=xAngle+diffX;
                                count++;
                            }
                            counter3++;

                        }
                        counter1++;
                    }

                    nextPts.copyTo(prevPts);
                    if(count==0) {count=1;}

                    xAngle=(xAngle/count)*4;
                    xTotal=xTotal+xAngle;
                    if(xTotal>=360) xTotal=xTotal-360;
                    temp.copyTo(mGray1);
                    detectFeature++;

                    //Detect new features
                    if(detectFeature==900){
                        detectFeature=0;
                        goodFeaturesToTrack(mGray1, initial, 100, 0.1,3);
                        initial.convertTo(prevPts, CvType.CV_32FC2);
                    }
                    try {
                        sleep(2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

                if(stopThread){
                    return;
                }
            }
        }
    };


    /**
     * Dense Optical flow for Obstacle Avoidance
     */
    Runnable opticalFlowFB = new Runnable(){
        @Override
        public void run() {
            Mat temp=Mat.zeros(second_frame.size(), CvType.CV_8UC1);

            for(;;){
                if(!stopThread){
                    if(first_frame==null){
                        first_frame = Mat.zeros(second_frame.size(), CvType.CV_8UC1);
                        second_frame.copyTo(first_frame);
                        pastFlow = Mat.zeros(second_frame.size(), CvType.CV_32FC2);

                    }else{
                        int counter1 = 11;
                        int counter4= 23;
                        int counter3;
                        double leftTotal=0;
                        double rightTotal=0;
                        double centreTotal=0;
                        second_frame.copyTo(temp);
                        Mat difference = Mat.zeros(second_frame.size(), CvType.CV_32FC2);
                        flow  = Mat.zeros(second_frame.size(), CvType.CV_32FC2);
                        calcOpticalFlowFarneback(first_frame,temp,flow,0.5,1,5,3,3,1.1,0);
                        for (int cols = 11; cols <22; cols++) {
                            counter3 = 0;
                            for (int rows = 0; rows < second_frame.rows(); rows++) {
                                leftTotal = leftTotal+ Math.sqrt(flow.get(counter3,counter1)[0]*flow.get(counter3,counter1)[0] + flow.get(counter3,counter1)[1]*flow.get(counter3,counter1)[1]);
                                rightTotal = rightTotal+Math.sqrt(flow.get(counter3,counter4)[0]*flow.get(counter3,counter4)[0] + flow.get(counter3,counter4)[1]*flow.get(counter3,counter4)[1]);
                                if(counter1>=17){
                                    centreTotal=centreTotal+Math.sqrt(flow.get(counter3,counter1)[0]*flow.get(counter3,counter1)[0] + flow.get(counter3,counter1)[1]*flow.get(counter3,counter1)[1]);
                                }
                                if(counter4<=27){
                                    centreTotal = centreTotal+Math.sqrt(flow.get(counter3,counter4)[0]*flow.get(counter3,counter4)[0] + flow.get(counter3,counter4)[1]*flow.get(counter3,counter4)[1]);
                                }

                                counter3++;
                            }
                            counter1++;
                            counter4++;
                        }
                        temp.copyTo(first_frame);
                        double m = leftTotal-prevLeft;
                        double n = rightTotal-prevRight;
                        double p = centreTotal-prevCentre;
                        prevLeft=leftTotal;
                        prevRight=rightTotal;
                        prevCentre=centreTotal;
                        if(m>opticalSumLimit || n>opticalSumLimit || p>opticalSumLimit){
                            leftFlow=m;
                            rightFlow=n;
                            centreFlow=p;
                        }
                    }

                    try {
                        sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }else{
                    return;
                }

            }
        }
    };

    /**
     * Obstacle avoidance thread
     */
    Runnable obstacleAvoidance=new Runnable() {
        @Override
        public void run() {
            try {
                sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //Log.i(flowTag, "" + xPosOF + "," + yPosOF+","+orientation);
            for(;;){
                //Log.i(flowTag,""+leftFlow+", "+centreFlow+", "+rightFlow);
                double x=leftFlow;
                double y=rightFlow;
                double l=centreFlow;

                if(x>0 || y>0 || l>0){
                    double z=y-x;
                    try {
                        sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(l>40){
                        if(z==0) z=1;
                        double t=(z/Math.abs(z))*60;
                        try {
                            turnAround(t);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }else{
                        if(x>l || y>l){
                            if(z>30){
                                z=30;
                            }else if(z<-30){
                                z=-30;
                            }
                            try {
                                turnAround(z);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    beginTurn=false;
                    leftFlow=0;
                    rightFlow=0;
                    centreFlow=0;
                    direction=0;
                }else{
                    moveForward(0.05);
                    try {
                        sleep(1500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //Log.i(flowTag,""+xPosition+","+yPosition+","+orientation);
                }
                if(stopThread) return;
                if(outboundStop){
                    broadcast.broadcastAlert("start");
                    return;
                }
            }

        }
    };

    // Monitoring time to stop outbound PI and start Inbound

    Runnable startInbound = new Runnable() {
        @Override
        public void run() {
            try {
                sleep(integratorRunTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            outboundStop=true;
        }
    };




    // Calculate the rms difference between two images
    private double rmsDifference(Mat current, Mat ref){
        int counter1 = 0;
        int counter3;
        double rootSum=0;
        int count =0;
        for (int cols = 0; cols < current.cols(); cols++) {
            counter3 = 0;
            for (int rows = 0; rows < current.rows(); rows++) {
                double xDiff = current.get(counter3,counter1)[0]-ref.get(counter3,counter1)[0];
                rootSum = rootSum + (Math.pow(xDiff,2));
                count++;
                counter3++;

            }
            counter1++;
        }
        return (Math.sqrt(rootSum/count));
    }

    //Rotate a 90x10 image (4 degrees intervals)
    private Mat rotateImage(Mat image, int theta){
        int counter1 = 0;
        int counter3;
        int colPosition;
        double[] rotPixel;
        if(theta<0){
            theta=theta+360;
        }
        theta = theta/4;
        Mat rotatedImage = Mat.zeros(image.size(),image.type());

        for (int phi = 0; phi < image.cols(); phi++) {
            counter3 = 0;
            for (int theta_tmp = 0; theta_tmp < image.rows(); theta_tmp++) {
                colPosition=counter1+theta;
                if(colPosition >=90){
                    colPosition=colPosition - 90;
                }
                rotPixel = image.get(counter3,colPosition);
                rotatedImage.put(counter3, counter1, rotPixel);
                counter3++;
            }
            counter1++;
        }
        broadcast.broadcastRotatedImage(rotatedImage);
        return rotatedImage;
    }

    // Drive forward
    public void moveForward(double dist){
        try{
            sleep(100);
        }catch(InterruptedException e){
            e.printStackTrace();
        }
        broadcast.broadcastDistance(dist);

    }

    // Turn by an angle
    public void turnAround(double theta) throws InterruptedException {
        try {
            sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(theta>180){
            theta=theta-360;
        }else if(theta<-180){
            theta=theta+360;
        }
        beginTurn=true;
        xTotal=0;
        if(theta<0){
            direction=2;
        }else if(theta>0){
            direction=1;
        }else{
            direction=0;
        }
        broadcast.broadcastAngle(theta);
        sleep(3000);
        broadcast.broadcastOpticalFlowAngle(xTotal);

    }

    /**
     * start searching
     */

    Runnable startSearch=new Runnable() {
        @Override
        public void run() {
            for(;;){
                if(savedList){
                    savedList=false;
                    try {
                        sleep(15000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    broadcast.broadcastStartSearch(true);
                }
            }

        }
    };


    /**
     *
     * Start Homing
     */
    Runnable startHome = new Runnable() {
        @Override
        public void run() {
            for(;;){
                if(savedList){
                    savedList=false;
                    try {
                        sleep(15000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    broadcast.broadcastStartHome(true);


                        }
                    }

                }
    };

    Runnable saveImagesRunDown =new Runnable() {
        @Override
        public void run() {
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Mat temp=new Mat();
            for(int i=0;i<360;i++){
                temp=rotateFullImage(fullSnapShot,i);
            }
            Log.i(flowTag,"Saving Images Done");
            savedList=true;
        }
    };

    Runnable saveImagesTurnMinimum = new Runnable() {
        @Override
        public void run() {
            for(int n=0;n<4;n++){
                Log.i(flowTag,"start with list "+(n+1));
                Mat temp=new Mat();
                for(int i=0;i<360;i=i+4){
                    temp=rotateImage(imageToDisplay, i);
                }
                Log.i(flowTag,"Done with list "+(n+1));
                try {
                    sleep(15000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            /*
            moveForward(0.1);
            try {
                sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                turnAround(180);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Mat temp=new Mat();
            for(int i=0;i<360;i++){
                temp=rotateFullImage(fullImageToDisplay,i);
            }
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            broadcast.broadcastCompleteFlag(true);

            moveForward(0.1);
            try {
                sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            moveForward(0.1);
            try {
                sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                turnAround(180);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for(int i=0;i<360;i++){
                temp=rotateFullImage(fullImageToDisplay,i);
            }
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            broadcast.broadcastCompleteFlag(true);

            moveForward(0.1);
            try {
                sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                turnAround(90);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            moveForward(0.1);
            try {
                sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                turnAround(180);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for(int i=0;i<360;i++){
                temp=rotateFullImage(fullImageToDisplay,i);
            }
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            broadcast.broadcastCompleteFlag(true);

            moveForward(0.1);
            try {
                sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            moveForward(0.1);
            try {
                sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                turnAround(180);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for(int i=0;i<360;i++){
                temp=rotateFullImage(fullImageToDisplay,i);
            }
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            */
            Log.i(flowTag,"Images saved");
            savedList=true;

        }
    };

    /**
     * The broadcast of the image takes place in a different thread so we have a proper running UI
     */
    private class BroadcastImage extends AsyncTask<Object, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Object... transmission) {
            Mat matImage = (Mat) transmission[0];
            //Since there is no possibility to send a 2D array or Mat file the image is transformed to a 1D int array
            byte[] imageArray_tmp = new byte[matImage.height() * matImage.width()];
            matImage.get(0, 0, imageArray_tmp);
            int[] imageArray = new int[imageArray_tmp.length];
            for (int n = 0; n < imageArray_tmp.length; n++) {
                imageArray[n] = (int) imageArray_tmp[n] & 0xFF;
            }
            //Log.i(myTag, "Broadcasting*******");
            broadcast.broadcastImage(imageArray);
            return false;
        }
    }
}