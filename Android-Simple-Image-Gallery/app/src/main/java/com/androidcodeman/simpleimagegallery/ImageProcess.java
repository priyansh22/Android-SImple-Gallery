//package com.androidcodeman.simpleimagegallery;
//
//public class ImageProcess {
//}
package com.androidcodeman.simpleimagegallery;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentText;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentTextRecognizer;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceImageLabelerOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.ml.vision.text.RecognizedLanguage;
//import android.content.Context;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import android.os.Environment;
import java.io.File;


public class ImageProcess {
    final String CAMERA_SERVICE ="camera";
    String a = "";
    DBHandler dbHandler = null;
    public Context context=null;


    public  ImageProcess(Context context) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        dbHandler = new DBHandler(context);
this.context=context;

    }

    public void Act(List<String> l) {
        try {
            dbHandler.truncateOldEntries();
            final List<String>labelstextlist=new ArrayList<String>();
            Result r = new Result(l,labelstextlist);
            r.execute();
//            Toast.makeText(context, dbHandler.getImages("image.jpg").toString(), Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, e.getMessage() + "activity failed!", Toast.LENGTH_SHORT).show();
        }


    }


    class ImageLabelingActivity {


        public void labelImages(FirebaseVisionImage image, final String path1)//    pass    Uri uri
        {
            // [START set_detector_options]
            final List<String> labelslist = new ArrayList<String>();
            FirebaseVisionOnDeviceImageLabelerOptions options =
                    new FirebaseVisionOnDeviceImageLabelerOptions.Builder()
                            .setConfidenceThreshold(0.8f)
                            .build();

            FirebaseVisionImageLabeler detector = FirebaseVision.getInstance()
                    .getOnDeviceImageLabeler();

            Task<List<FirebaseVisionImageLabel>> result =
                    detector.processImage(image)
                            .addOnSuccessListener(
                                    new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
                                        @Override
                                        public void onSuccess(List<FirebaseVisionImageLabel> labels) {
                                            // Task completed successfully
                                            // [START_EXCLUDE]
                                            // [START get_labels]

                                            for (FirebaseVisionImageLabel label : labels) {
                                                String text = label.getText();
                                                labelslist.add(text);
                                                System.out.println(labelslist+"list in method");
                                                String entityId = label.getEntityId();
                                                float confidence = label.getConfidence();
                                                Toast.makeText(context, "label generated...." + text + "  entity id:" + entityId + "Confidence:" + confidence, Toast.LENGTH_SHORT).show();



                                            }


                                            dbHandler.addImageMeta(path1, labelslist, 0L);
                                        }

                                    })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            a.concat("failed labeling..");
//                                            return labelslist;
                                        }
                                    });


        }


    }

    class TextRecognitionActivity {

        public void  recognizeText(FirebaseVisionImage image, final String path ) {

            final List<String> textlist = new ArrayList<String>();
            FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                    .getOnDeviceTextRecognizer();
            // [END get_detector_default]

            // [START run_detector]
            Task<FirebaseVisionText> result =
                    detector.processImage(image)
                            .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                                @Override
                                public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                    // Task completed successfully
                                    // [START_EXCLUDE]
                                    // [START get_text]
                                    for (FirebaseVisionText.TextBlock block : firebaseVisionText.getTextBlocks()) {
                                        Rect boundingBox = block.getBoundingBox();
                                        Point[] cornerPoints = block.getCornerPoints();
                                        String text = block.getText();
                                        Toast.makeText(context, "text generated..." + text, Toast.LENGTH_SHORT).show();
//                                        a.concat(text);
                                        textlist.add(text);
                                        for (FirebaseVisionText.Line line : block.getLines()) {
                                            // ...
                                            for (FirebaseVisionText.Element element : line.getElements()) {
                                                // ...
                                            }
                                        }
                                    }
                                    dbHandler.addImageMeta(path,textlist,0L);
                                    // [END get_text]
//                                    return textlist;
                                    // [END_EXCLUDE]
                                }
                            })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Task failed with an exception
                                            // ...
                                            Toast.makeText(context, "text not detected.", Toast.LENGTH_SHORT).show();
//                                            return textlist;
                                        }
                                    });
            System.out.println(result+"result generated");


            // [END run_detector]
        }


        private void processTextBlock(FirebaseVisionText result) {
            // [START mlkit_process_text_block]
            String resultText = result.getText();
            for (FirebaseVisionText.TextBlock block : result.getTextBlocks()) {
                String blockText = block.getText();
                Float blockConfidence = block.getConfidence();
                List<RecognizedLanguage> blockLanguages = block.getRecognizedLanguages();
                Point[] blockCornerPoints = block.getCornerPoints();
                Rect blockFrame = block.getBoundingBox();
                for (FirebaseVisionText.Line line : block.getLines()) {
                    String lineText = line.getText();
                    Float lineConfidence = line.getConfidence();
                    List<RecognizedLanguage> lineLanguages = line.getRecognizedLanguages();
                    Point[] lineCornerPoints = line.getCornerPoints();
                    Rect lineFrame = line.getBoundingBox();
                    for (FirebaseVisionText.Element element : line.getElements()) {
                        String elementText = element.getText();
                        Float elementConfidence = element.getConfidence();
                        List<RecognizedLanguage> elementLanguages = element.getRecognizedLanguages();
                        Point[] elementCornerPoints = element.getCornerPoints();
                        Rect elementFrame = element.getBoundingBox();
                    }
                }
            }
            // [END mlkit_process_text_block]
        }

        private FirebaseVisionDocumentTextRecognizer getLocalDocumentRecognizer() {
            // [START mlkit_local_doc_recognizer]
            FirebaseVisionDocumentTextRecognizer detector = FirebaseVision.getInstance()
                    .getCloudDocumentTextRecognizer();
            // [END mlkit_local_doc_recognizer]

            return detector;
        }



        private void processDocumentImage() {
            // Dummy variables
            FirebaseVisionDocumentTextRecognizer detector = getLocalDocumentRecognizer();
            FirebaseVisionImage myImage = FirebaseVisionImage.fromByteArray(new byte[]{},
                    new FirebaseVisionImageMetadata.Builder().build());

            // [START mlkit_process_doc_image]
            detector.processImage(myImage)
                    .addOnSuccessListener(new OnSuccessListener<FirebaseVisionDocumentText>() {
                        @Override
                        public void onSuccess(FirebaseVisionDocumentText result) {

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Task failed with an exception
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
            // [END mlkit_process_doc_image]
        }

        private void processDocumentTextBlock(FirebaseVisionDocumentText result) {
            // [START mlkit_process_document_text_block]
            String resultText = result.getText();
            for (FirebaseVisionDocumentText.Block block : result.getBlocks()) {
                String blockText = block.getText();
                Float blockConfidence = block.getConfidence();
                List<RecognizedLanguage> blockRecognizedLanguages = block.getRecognizedLanguages();
                Rect blockFrame = block.getBoundingBox();
                for (FirebaseVisionDocumentText.Paragraph paragraph : block.getParagraphs()) {
                    String paragraphText = paragraph.getText();
                    Float paragraphConfidence = paragraph.getConfidence();
                    List<RecognizedLanguage> paragraphRecognizedLanguages = paragraph.getRecognizedLanguages();
                    Rect paragraphFrame = paragraph.getBoundingBox();
                    for (FirebaseVisionDocumentText.Word word : paragraph.getWords()) {
                        String wordText = word.getText();
                        Float wordConfidence = word.getConfidence();
                        List<RecognizedLanguage> wordRecognizedLanguages = word.getRecognizedLanguages();
                        Rect wordFrame = word.getBoundingBox();
                        for (FirebaseVisionDocumentText.Symbol symbol : word.getSymbols()) {
                            String symbolText = symbol.getText();
                            Float symbolConfidence = symbol.getConfidence();
                            List<RecognizedLanguage> symbolRecognizedLanguages = symbol.getRecognizedLanguages();
                            Rect symbolFrame = symbol.getBoundingBox();
                        }
                    }
                }
            }
            // [END mlkit_process_document_text_block]
        }
    }

    class VisionImage {

        private static final String TAG = "MLKIT";
        private static final String MY_CAMERA_ID = "my_camera_id";

        private void imageFromBitmap(Bitmap bitmap) {
            // [START image_from_bitmap]
            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
            // [END image_from_bitmap]
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        private void imageFromMediaImage(Image mediaImage, int rotation) {
            // [START image_from_media_image]
            FirebaseVisionImage image = FirebaseVisionImage.fromMediaImage(mediaImage, rotation);
            // [END image_from_media_image]
        }

        private void imageFromBuffer(ByteBuffer buffer, int rotation) {
            // [START set_metadata]
            FirebaseVisionImageMetadata metadata = new FirebaseVisionImageMetadata.Builder()
                    .setWidth(480)   // 480x360 is typically sufficient for
                    .setHeight(360)  // image recognition
                    .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                    .setRotation(rotation)
                    .build();
            // [END set_metadata]
            // [START image_from_buffer]
            FirebaseVisionImage image = FirebaseVisionImage.fromByteBuffer(buffer, metadata);
            // [END image_from_buffer]
        }

        private FirebaseVisionImage imageFromArray(byte[] byteArray, int rotation) {
            FirebaseVisionImageMetadata metadata = new FirebaseVisionImageMetadata.Builder()
                    .setWidth(480)   // 480x360 is typically sufficient for
                    .setHeight(360)  // image recognition
                    .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                    .setRotation(rotation)
                    .build();
            // [START image_from_array]
            return (FirebaseVisionImage.fromByteArray(byteArray, metadata));
            // [END image_from_array]
        }

        public FirebaseVisionImage imageFromPath(Context context, Uri uri) throws IOException {
            // [START image_from_path]


            return FirebaseVisionImage.fromFilePath(context, uri);
            // [END image_from_path]
        }

        // [START get_rotation]
        private final SparseIntArray ORIENTATIONS = new SparseIntArray();

        {
            ORIENTATIONS.append(Surface.ROTATION_0, 90);
            ORIENTATIONS.append(Surface.ROTATION_90, 0);
            ORIENTATIONS.append(Surface.ROTATION_180, 270);
            ORIENTATIONS.append(Surface.ROTATION_270, 180);
        }

        /**
         * Get the angle by which an image must be rotated given the device's current
         * orientation.
         */
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public int getRotationCompensation(String cameraId, Activity activity, Context context)
                throws CameraAccessException {
            // Get the device's current rotation relative to its "native" orientation.
            // Then, from the ORIENTATIONS table, look up the angle the image must be
            // rotated to compensate for the device's rotation.
            int deviceRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            int rotationCompensation = ORIENTATIONS.get(deviceRotation);

            // On most devices, the sensor orientation is 90 degrees, but for some
            // devices it is 270 degrees. For devices with a sensor orientation of
            // 270, rotate the image an additional 180 ((270 + 270) % 360) degrees.
            CameraManager cameraManager = (CameraManager) context.getSystemService(CAMERA_SERVICE);
            int sensorOrientation = cameraManager
                    .getCameraCharacteristics(cameraId)
                    .get(CameraCharacteristics.SENSOR_ORIENTATION);
            rotationCompensation = (rotationCompensation + sensorOrientation + 270) % 360;

            // Return the corresponding FirebaseVisionImageMetadata rotation value.
            int result;
            switch (rotationCompensation) {
                case 0:
                    result = FirebaseVisionImageMetadata.ROTATION_0;
                    break;
                case 90:
                    result = FirebaseVisionImageMetadata.ROTATION_90;
                    break;
                case 180:
                    result = FirebaseVisionImageMetadata.ROTATION_180;
                    break;
                case 270:
                    result = FirebaseVisionImageMetadata.ROTATION_270;
                    break;
                default:
                    result = FirebaseVisionImageMetadata.ROTATION_0;
                    Log.e(TAG, "Bad rotation value: " + rotationCompensation);
            }
            return result;
        }
        // [END get_rotation]

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public void getCompensation(Activity activity, Context context) throws CameraAccessException {
            // Get the ID of the camera using CameraManager. Then:
            int rotation = getRotationCompensation(MY_CAMERA_ID, activity, context);
        }

    }

    class Result extends AsyncTask<String, String, String> {
        List<String> uris =null;
        List<String>returnlist=null;
        public Result(List<String> uris,List<String> returnlist ){this.uris=uris;
        this.returnlist=returnlist;}
        @Override
        protected String doInBackground(String... strings) {
            TextRecognitionActivity t = new TextRecognitionActivity();
            ImageLabelingActivity i = new ImageLabelingActivity();
            for (String uri: uris) {
//            File videoFile = new
//                   File(Environment.getExternalStoragePublicDirectory().getAbsolutePath() + "/test.jpg");
//                List<String> list = new ArrayList<String>();

//            String imageUri = "storage/emulated/0/Download/test.jpg";
                VisionImage v = new VisionImage();
                File file = new
                        File(uri);
                Uri u = Uri.fromFile(file);
//            File fi=new File(getResources().getResourceEntryName(R.mipmap.g));
//            Uri u=Uri.fromFile(fi);
                try {

                    FirebaseVisionImage myImage = v.imageFromPath(context, u);


                    i.labelImages(myImage,uri);



                       t.recognizeText(myImage,uri);



                }

            catch(Exception e){ Toast.makeText(context, "In Background Task " + e.getMessage(), Toast.LENGTH_LONG).show();}}
            return null;

        }

        @Override
        protected void onProgressUpdate(String... text) {
            Toast.makeText(context, "In Background Task " + a + text[0], Toast.LENGTH_LONG).show();

        }
    }
}


