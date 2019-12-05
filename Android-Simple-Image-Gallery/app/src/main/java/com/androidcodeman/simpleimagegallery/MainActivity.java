package com.androidcodeman.simpleimagegallery;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.androidcodeman.simpleimagegallery.utils.MarginDecoration;
import com.androidcodeman.simpleimagegallery.utils.PicHolder;
import com.androidcodeman.simpleimagegallery.utils.imageFolder;
import com.androidcodeman.simpleimagegallery.utils.itemClickListener;
import com.androidcodeman.simpleimagegallery.utils.pictureFacer;
import com.androidcodeman.simpleimagegallery.utils.pictureFolderAdapter;
import java.util.ArrayList;

/**
 * Author CodeBoy722
 *
 * The main Activity start and loads all folders containing images in a RecyclerView
 * this folders are gotten from the MediaStore by the Method getPicturePaths()
 */
public class MainActivity extends AppCompatActivity implements itemClickListener {

    RecyclerView folderRecycler;
    TextView empty;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    ArrayList<String> listimage = new ArrayList<>();
    String value = null;
    boolean flag = false;

    /**
     * Request the user for permission to access media files and read images on the device
     * this will be useful as from api 21 and above, if this check is not done the Activity will crash
     *
     * Setting up the RecyclerView and getting all folders that contain pictures from the device
     * the getPicturePaths() returns an ArrayList of imageFolder objects that is then used to
     * create a RecyclerView Adapter that is set to the RecyclerView
     *
     * @param savedInstanceState saving the activity state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if(ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        //____________________________________________________________________________________

        //empty =findViewById(R.id.empty);

        folderRecycler = findViewById(R.id.folderRecycler);
        folderRecycler.addItemDecoration(new MarginDecoration(this));
        folderRecycler.hasFixedSize();
//        ArrayList<imageFolder> folds = getPicturePaths();
//        RecyclerView.Adapter folderAdapter = null;



//        if(folds.isEmpty()){
//            empty.setVisibility(View.VISIBLE);
//        }else{
//            folderAdapter = new pictureFolderAdapter(folds,MainActivity.this,this);
//            folderRecycler.setAdapter(folderAdapter);
//        }

        render();

        Button button = findViewById(R.id.searchButton);
        button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                System.out.println("yes here");
                EditText text = (EditText)findViewById(R.id.searchEditText);
                value = text.getText().toString();
                DBHandler dbhandler = new DBHandler(getApplicationContext());
                listimage=dbhandler.getImages(value);
                if (listimage.size() ==0) {
                    Toast.makeText(MainActivity.this, "empty list", Toast.LENGTH_SHORT).show();
                }
                for (String image : listimage)
                {

                    Log.v("Return list: ", image);
                }
                flag = true;
                //folds = getPicturePaths();
                render();
            }
        });


//        ImageProcess imageprocess=new ImageProcess(getApplicationContext());
//        imageprocess.Act();


    }


    private void render() {
        TextView empty =findViewById(R.id.empty);
        ArrayList<imageFolder> folds = getPicturePaths();
        System.out.println("folds"+folds);
        RecyclerView.Adapter folderAdapter = null;
        if(folds.isEmpty()){
            empty.setVisibility(View.VISIBLE);
        }else{
            folderAdapter = new pictureFolderAdapter(folds,MainActivity.this,this);
            folderRecycler.setAdapter(folderAdapter);
        }
    }

    /**
     * @return
     * gets all folders with pictures on the device and loads each of them in a custom object imageFolder
     * the returns an ArrayList of these custom objects
     */

    private ArrayList<imageFolder> getPicturePaths(){
        ArrayList<String> list = new ArrayList<String>();
        ImageProcess imageprocess=new ImageProcess(getApplicationContext());

        ArrayList<imageFolder> picFolders = new ArrayList<>();
        ArrayList<String> picPaths = new ArrayList<>();
        Uri allImagesuri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = { MediaStore.Images.ImageColumns.DATA ,MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,MediaStore.Images.Media.BUCKET_ID};
        Cursor cursor = this.getContentResolver().query(allImagesuri, projection, null, null, null);
        try {
            if (cursor != null) {
                cursor.moveToFirst();
            }
            do{
                imageFolder folds = new imageFolder();
                String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
                String folder = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                String datapath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));

                //String folderpaths =  datapath.replace(name,"");
                String folderpaths = datapath.substring(0, datapath.lastIndexOf(folder+"/"));

                folderpaths = folderpaths+folder+"/";

                if (listimage.size() > 0) {

                    Log.d("Size of listimage", String.valueOf(listimage.size()));
                    Log.d("Original datapath ", datapath);
                    datapath = null;
                    Log.d("PicPaths ", String.valueOf(picPaths));
                    System.out.println("cominh gere");
                    picPaths = listimage;
                    System.out.println(picPaths);
                }
                if (!picPaths.contains(folderpaths)) {
                    picPaths.add(folderpaths);
                    Log.d("FOLDER PATHS -", folderpaths);
                    folds.setPath(folderpaths);
                    folds.setFolderName(folder);
                    Log.d("FOLDSs", String.valueOf(folds));
                    if (listimage.size() == 2) {
                        folds.setFirstPic(String.valueOf(listimage.get(0)));
                    } else {
                        folds.setFirstPic(datapath);//if the folder has only one picture this line helps to set it as first so as to avoid blank image in itemview
                    }
                        folds.addpics();
                    picFolders.add(folds);
                }else{
                    for(int i = 0;i<picFolders.size();i++){
                        if(picFolders.get(i).getPath().equals(folderpaths)){
                            Log.d("picFolders", String.valueOf(picFolders));
                            if (listimage.size() > 0) {
                                datapath = null;
                                datapath = listimage.get(i);
                                list.add(datapath);
                                Log.d("PICTURE KA PATH = ", datapath);
                                picFolders.get(i).setFirstPic(datapath);
                                picFolders.get(i).addpics();
                            } else {
                                list.add(datapath);
                                Log.d("PICTURE KA PATH = ", datapath);
                                picFolders.get(i).setFirstPic(datapath);
                                picFolders.get(i).addpics();
                            }


                        }
                    }
                }
            }while(cursor.moveToNext());
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        for(int i = 0;i < picFolders.size();i++){
            Log.d("picture folders",picFolders.get(i).getFolderName()+" and path = "+picFolders.get(i).getPath()+" "+picFolders.get(i).getNumberOfPics());
        }
        for (imageFolder image : picFolders)
        {
            String s;
            s = image.path;
            Log.v("My array list content: ", s);
        }

        //reverse order ArrayList
       /* ArrayList<imageFolder> reverseFolders = new ArrayList<>();

        for(int i = picFolders.size()-1;i > reverseFolders.size()-1;i--){
            reverseFolders.add(picFolders.get(i));
        }*/
        if (!flag) {
            imageprocess.Act(list);
        }
        return picFolders;
    }


    @Override
    public void onPicClicked(PicHolder holder, int position, ArrayList<pictureFacer> pics) {

    }

    /**
     * Each time an item in the RecyclerView is clicked this method from the implementation of the transitListerner
     * in this activity is executed, this is possible because this class is passed as a parameter in the creation
     * of the RecyclerView's Adapter, see the adapter class to understand better what is happening here
     * @param pictureFolderPath a String corresponding to a folder path on the device external storage
     */
    @Override
    public void onPicClicked(String pictureFolderPath,String folderName) {
        Intent move = new Intent(MainActivity.this,ImageDisplay.class);
        move.putExtra("folderPath",pictureFolderPath);
        move.putExtra("folderName",folderName);

        //move.putExtra("recyclerItemSize",getCardsOptimalWidth(4));
        startActivity(move);
    }


   /* public int getCardsOptimalWidth(int numberOfRows){
        Configuration configuration = MainActivity.this.getResources().getConfiguration();
        int screenWidthDp = configuration.screenWidthDp; //The current width of the available screen space, in dp units, corresponding to screen width resource qualifier.
        int smallestScreenWidthDp = configuration.smallestScreenWidthDp; //The smallest screen size an application will see in normal operation, corresponding to smallest screen width resource qualifier.
        int each = screenWidthDp / numberOfRows;

        return each;
    }*/

   /* private int dpToPx(int dp) {
        float density = MainActivity.this.getResources()
                .getDisplayMetrics()
                .density;
        return Math.round((float) dp * density);
    }*/

}
