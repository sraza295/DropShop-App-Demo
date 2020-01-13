package com.example.dropshopappdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.dropshopappdemo.Utils.FileUtils;
import com.example.dropshopappdemo.adapter.MyAdapter;
import com.example.dropshopappdemo.modal.Product;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE = 6384; // onActivityResult request code
    private Button btnChoose;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = FirebaseFirestore.getInstance();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewMovieList);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        btnChoose = (Button) findViewById(R.id.button_choose);

        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChooser();
            }
        });
    }
    private void showChooser() {
        // Use the GET_CONTENT intent from the utility class
        Intent target = FileUtils.createGetContentIntent();
        // Create the chooser Intent
        Intent intent = Intent.createChooser(
                target, getString(R.string.chooser_title));
        try {
            startActivityForResult(intent, REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            // The reason for the existence of aFileChooser
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE:
                // If the file selection was successful
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        // Get the URI of the selected file
                        final Uri uri = data.getData();
                        Log.i(TAG, "Uri = " + uri.toString());
                        try {
                            // Get the file path from the URI
                            final String path = FileUtils.getPath(this, uri);
                            Toast.makeText(MainActivity.this,"File Selected: " + path, Toast.LENGTH_LONG).show();

                            String fileName = FileUtils.getFileName(getContentResolver(),uri);
                            String extensionName = FileUtils.getExtension(fileName);
                            if(extensionName.equalsIgnoreCase(".json"))
                            {
                                String fileNameWithoutExtension = FileUtils.getFileNameWithoutExtension(fileName);

                                String jsongString = readFromFile(path);
                                JSONObject jsonData = new JSONObject(jsongString);
                                checkCollectionsExist(jsonData,fileNameWithoutExtension);
                            }

                        } catch (Exception e) {
                            Log.e("FileSelectorTestActivit", "File select error", e);
                        }
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



    private String readFromFile(String path) {

        String ret = "";
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(path));

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }
                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }



    public void checkCollectionsExist(final JSONObject jsonData, final String fileNameWithoutExtension)
    {
        db.collection(fileNameWithoutExtension).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(queryDocumentSnapshots.isEmpty()){

                    Toast.makeText(MainActivity.this, "Collection is Empty", Toast.LENGTH_LONG).show();
                    pushNewJsonDataIntoFireStore(jsonData,fileNameWithoutExtension);
                    getProductDataSortedByExpiry(fileNameWithoutExtension);
                }
                else
                {
                    Toast.makeText(MainActivity.this, "Collection Exist", Toast.LENGTH_LONG).show();
                    checkDocumentsExist(jsonData,fileNameWithoutExtension);
                    getProductDataSortedByExpiry(fileNameWithoutExtension);

                }
            }
        });;
    }

    private void checkDocumentsExist(final JSONObject jsonData, final String fileNameWithoutExtension)
    {
        Iterator itr = jsonData.keys();
        while(itr.hasNext())
        {
                final String keys = (String)itr.next();

                DocumentReference docIdRef = db.collection(fileNameWithoutExtension).document(keys);
                docIdRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
                {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task)
                    {
                        if (task.isSuccessful())
                        {
                            Map<String, Object> product = getProduct(keys, jsonData);
                            DocumentReference documentReference= db.collection(fileNameWithoutExtension).document(keys);

                            DocumentSnapshot document = task.getResult();
                            if (document.exists())
                            {
                                Map<String, Object> listData = getProductList(keys,jsonData);
                                checkValueOfDocuments(keys,fileNameWithoutExtension,listData);
                                //documentReference.update(product);
                               /* Toast.makeText(MainActivity.this, "Updated in "+keys, Toast.LENGTH_SHORT).show();
                                System.out.println("Updated in "+keys);*/

                            }
                            else {
                                documentReference.set(product);
                                Toast.makeText(MainActivity.this, "Added "+keys, Toast.LENGTH_SHORT).show();
                                System.out.println("Added "+keys);
                            }
                        }
                        else {
                                Log.d(TAG, "Failed with: ", task.getException());
                            }
                        }
                    });
        }
    }

    private void checkValueOfDocuments(final String keys, final String fileNameWithoutExtension, final Map<String, Object> listData)
    {
        final DocumentReference docIdRef = db.collection(fileNameWithoutExtension).document(keys);
        docIdRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                //List<Product> list=null;
                Map<String, Object> map = new HashMap<String, Object>();
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        System.out.println("inside exist");

                            Product product = document.toObject(Product.class);
                            map.put("productId",product.getProductId());
                            map.put("productCode",product.getProductCode());
                            map.put("mrp",product.getMrp());
                            map.put("customerId",product.getCustomerId());
                            map.put("brandName",product.getBrandName());
                            map.put("brandCode",product.getBrandCode());
                            map.put("expiry",product.getExpiry());
                            map.put("productDesc",product.getProductDesc());

                            /*System.out.println("map value "+map);
                            System.out.println("listData "+listData);*/

                            if(!listData.equals(map))
                            {
                                docIdRef.update((Map<String, Object>) listData);
                                Toast.makeText(MainActivity.this, "Updated in "+keys, Toast.LENGTH_SHORT).show();
                                System.out.println("Updated in "+keys);
                            }

                    } else {
                        Log.d(TAG, "Document does not exist!");
                    }
                } else {
                    Log.d(TAG, "Failed with: ", task.getException());
                }
            }
        });
    }

    private Map<String, Object> getProduct(String keys, JSONObject jsonData)
    {
        Map<String, Object> product = new HashMap<>();
        try
        {
            Object object = jsonData.get(keys);
            if (object instanceof JSONObject)
            {
                JSONObject childJsonData = (JSONObject) object;
                Iterator iterator = childJsonData.keys();
                while(iterator.hasNext())
                {
                    String childKeys = (String)iterator.next();
                    Object objectValues = childJsonData.get(childKeys);
                    if (objectValues instanceof Integer)
                    {
                        Integer values= (Integer) childJsonData.get(childKeys);
                        //Log.e("chhildKeys", "----"+childKeys+" values "+values);
                        product.put(childKeys,values);
                    }
                    if (objectValues instanceof String)
                    {
                        String values= (String) childJsonData.get(childKeys);
                        //Log.e("chhildKeys", "----"+childKeys+" values "+values);
                        product.put(childKeys,values);
                    }
                }

            }
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        return product;
    }

    private Map<String, Object> getProductList(String keys, JSONObject jsonData)
    {
        Map<String, Object> product = new HashMap<>();
        try
        {
            Object object = jsonData.get(keys);
            if (object instanceof JSONObject)
            {
                JSONObject childJsonData = (JSONObject) object;
                Iterator iterator = childJsonData.keys();
                while(iterator.hasNext())
                {
                    String childKeys = (String)iterator.next();
                    Object objectValues = childJsonData.get(childKeys);
                    if (objectValues instanceof Integer)
                    {
                        Integer values= (Integer) childJsonData.get(childKeys);
                        //Log.e("chhildKeys", "----"+childKeys+" values "+values);
                        product.put(childKeys,values);
                    }
                    if (objectValues instanceof String)
                    {
                        String values= (String) childJsonData.get(childKeys);
                        //Log.e("chhildKeys", "----"+childKeys+" values "+values);
                        product.put(childKeys,values);
                    }
                }

            }


        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        return product;
    }

    public void getProductDataSortedByExpiry(final String fileNameWithoutExtension)
    {
        db.collection(fileNameWithoutExtension).orderBy("expiry", Query.Direction.ASCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                List<Product> list=null;
                if (task.isSuccessful())
                {
                    list = new ArrayList<Product>();
                    for (DocumentSnapshot document : task.getResult()) {
                        Product product = document.toObject(Product.class);
                        product.getProductId();
                        product.getProductDesc();
                        product.getProductCode();
                        product.getMrp();
                        product.getCustomerId();
                        product.getBrandName();
                        product.getBrandCode();
                        product.getExpiry();
                        list.add(product);
                        Log.d("TAG", product.getProductId());
                    }
                }
                recyclerView.setAdapter(new MyAdapter<Product>(list));
            }
        });

    }

    private void pushNewJsonDataIntoFireStore(JSONObject jsonData, String fileNameWithoutExtension)
    {

        CollectionReference collectionReference = db.collection(fileNameWithoutExtension);
        //Use loop to get keys from your response
        Iterator itr = jsonData.keys();
        try
        {
            while(itr.hasNext())
            {
                String keys = (String)itr.next();
                DocumentReference documentReference= collectionReference.document(keys);
                //Log.e("Keys", "----"+keys);
                    Object object = jsonData.get(keys);
                    if (object instanceof JSONObject)
                    {
                        JSONObject childJsonData = (JSONObject) object;
                        Iterator iterator = childJsonData.keys();
                        Map<String, Object> product = new HashMap<>();
                        while(iterator.hasNext())
                        {
                            String childKeys = (String)iterator.next();
                            Object objectValues = childJsonData.get(childKeys);
                            if (objectValues instanceof Integer)
                            {
                                Integer values= (Integer) childJsonData.get(childKeys);
                                Log.e("chhildKeys", "----"+childKeys+" values "+values);
                                product.put(childKeys,values);
                            }
                            if (objectValues instanceof String)
                            {
                               String values= (String) childJsonData.get(childKeys);
                                Log.e("chhildKeys", "----"+childKeys+" values "+values);
                                product.put(childKeys,values);
                            }
                        }
                        documentReference.set(product);
                    }
            }
            Toast.makeText(this, fileNameWithoutExtension+".json has pushed into FireStore", Toast.LENGTH_SHORT).show();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }
}
