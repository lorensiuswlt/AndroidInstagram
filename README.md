AndroidInstagram
=================

AndroidInstagram is an Android wrapper library to access [Instagram API][3]. You can use this library to sign in with Instagram and access all Instagram API endpoints. Feel free to use it all you want in your Android apps.

If you are using AndroidInstagram in your app and would like to be listed here, please let me know via [Twitter][1]!

Also, you can follow me on Twitter : [@lorensiuswlt][1] or visit my blog [www.londatiga.net][2]

Setup
-----
* In Eclipse, just import the library as an Android library project. Project > Clean to generate the binaries 
you need, like R.java, etc.
* Then, just add AndroidInstagram as a dependency to your existing project.


Simple Example
-----
This example shows how to sign in with Instagram and access Instagram API `/users/self/feed` to get user's feed (photos and videos)

![Example Image](http://londatiga.net/images/android_instagram_library.jpg) 
![Example Image](http://londatiga.net/images/android_instagram_library2.jpg) 

```java
public class MainActivity extends Activity {
    private InstagramSession mInstagramSession;
    private Instagram mInstagram;
    
    private ProgressBar mLoadingPb;
    private GridView mGridView;
    
    private static final String CLIENT_ID = "your client id";
    private static final String CLIENT_SECRET = "your client secret";
    private static final String REDIRECT_URI = "your redirect uri";  
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mInstagram          = new Instagram(this, CLIENT_ID, CLIENT_SECRET, REDIRECT_URI);
        
        mInstagramSession   = mInstagram.getSession();
        
        if (mInstagramSession.isActive()) {
            setContentView(R.layout.activity_user);
            
            InstagramUser instagramUser = mInstagramSession.getUser();
            
            mLoadingPb  = (ProgressBar) findViewById(R.id.pb_loading);
            mGridView   = (GridView) findViewById(R.id.gridView);
            
            ((TextView) findViewById(R.id.tv_name)).setText(instagramUser.fullName);
            ((TextView) findViewById(R.id.tv_username)).setText(instagramUser.username);
            
            ((Button) findViewById(R.id.btn_logout)).setOnClickListener(new View.OnClickListener() {                
                @Override
                public void onClick(View arg0) {
                    mInstagramSession.reset();
                    
                    startActivity(new Intent(MainActivity.this, MainActivity.class));
                    
                    finish();
                }
            });
            
            ImageView userIv = (ImageView) findViewById(R.id.iv_user);
            
            DisplayImageOptions displayOptions = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.ic_user)
                    .showImageForEmptyUri(R.drawable.ic_user)
                    .showImageOnFail(R.drawable.ic_user)
                    .cacheInMemory(true)
                    .cacheOnDisc(false)
                    .considerExifParams(true)
                    .build();
    
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)                                               
                    .writeDebugLogs()
                    .defaultDisplayImageOptions(displayOptions)             
                    .build();
        
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.init(config);
            
            AnimateFirstDisplayListener animate  = new AnimateFirstDisplayListener();
            
            imageLoader.displayImage(instagramUser.profilPicture, userIv, animate);
            
            new DownloadTask().execute();
            
        } else {
            setContentView(R.layout.activity_main);
            
            ((Button) findViewById(R.id.btn_connect)).setOnClickListener(new View.OnClickListener() {           
                @Override
                public void onClick(View arg0) {                    
                    mInstagram.authorize(mAuthListener);    
                }
            });
        }
    }
    
    private void showToast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
    }
    
    private Instagram.InstagramAuthListener mAuthListener = new Instagram.InstagramAuthListener() {         
        @Override
        public void onSuccess(InstagramUser user) {
            finish();
            
            startActivity(new Intent(MainActivity.this, MainActivity.class));
        }
                    
        @Override
        public void onError(String error) {     
            showToast(error);
        }
    };
            
    public static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

        static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (loadedImage != null) {
                ImageView imageView = (ImageView) view;
                boolean firstDisplay = !displayedImages.contains(imageUri);
                if (firstDisplay) {
                    FadeInBitmapDisplayer.animate(imageView, 500);
                    displayedImages.add(imageUri);
                }
            }
        }
    }
    
    public class DownloadTask extends AsyncTask<URL, Integer, Long> {
        ArrayList<String> photoList;
        
        protected void onCancelled() {
            
        }
        
        protected void onPreExecute() {
            
        }
    
        protected Long doInBackground(URL... urls) {         
            long result = 0;
      
            try {
                List<NameValuePair> params = new ArrayList<NameValuePair>(1);
                
                params.add(new BasicNameValuePair("count", "10"));
                
                InstagramRequest request = new InstagramRequest(mInstagramSession.getAccessToken());
                String response          = request.requestGet("/users/self/feed", params);
                
                if (!response.equals("")) {
                    JSONObject jsonObj  = (JSONObject) new JSONTokener(response).nextValue();                   
                    JSONArray jsonData  = jsonObj.getJSONArray("data");
                    
                    int length = jsonData.length();
                    
                    if (length > 0) {
                        photoList = new ArrayList<String>();
                        
                        for (int i = 0; i < length; i++) {
                            JSONObject jsonPhoto = jsonData.getJSONObject(i).getJSONObject("images").getJSONObject("low_resolution");
                            
                            photoList.add(jsonPhoto.getString("url"));
                        }
                    }
                }
            } catch (Exception e) { 
                e.printStackTrace();
            }
            
            return result;
        }

        protected void onProgressUpdate(Integer... progress) {                  
        }

        protected void onPostExecute(Long result) {
            mLoadingPb.setVisibility(View.GONE);
            
            if (photoList == null) {
                Toast.makeText(getApplicationContext(), "No Photos Available", Toast.LENGTH_LONG).show();
            } else {
                DisplayMetrics dm = new DisplayMetrics();
                
                getWindowManager().getDefaultDisplay().getMetrics(dm);
                   
                int width   = (int) Math.ceil((double) dm.widthPixels / 2);
                width=width-50;
                int height  = width;
            
                PhotoListAdapter adapter = new PhotoListAdapter(MainActivity.this);
                
                adapter.setData(photoList);
                adapter.setLayoutParam(width, height);
            
                mGridView.setAdapter(adapter);
            }
        }                
    }
}
```

1. The main class is `Instagram`. Just instantiate the class and provide the client id, client secret and redirect uri into its constructor.
2. Call `getSession()` to get current session `InstagramSession`. 
3. To check if current session is active (user already signed in), call `isActive()` method from `InstagramSession` class.
4. To get current signed in user profile, call `getUser()` method from `InstagramSession` class.
5. To sign in, call `authorize(InstagramAuthListener listener)` method from Instagram class. You have to define a callback or listener `InstagramAuthListener` to handle the result.
6. If sign in success, you will get access token and instagram user profile.
7. To sign out, just call the `reset()` method from `InstagramSession` class or `resetSession()` from `Instagram` class.
4. To access Instagram API endpoints, use `createRequest(String method, String endpoint, List<NameValuePair> params)` from `InstagramRequest` class. This is a synchronous call so you have to put it on separate thread. Or you can use asynchronous method `createRequest(String method, String endpoint, List<NameValuePair> params, InstagramRequestListener listener)` and define the `InstagramRequestListener` to handle the result. Both of these methods will return response in JSON format. So you have to manually parse the response.
      
For more information please visit my [blog post][4].

Developed By
------------
* Lorensius W. L. T - <lorenz@londatiga.net>

Website
-------
* [www.londatiga.net][2]

License
-------

    Copyright 2014 Lorensius W. L. T

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
    
[1]: http://twitter.com/lorensiuswlt
[2]: http://www.londatiga.net
[3]: http://instagram.com/developer/endpoints/
[4]: http://www.londatiga.net/it/programming/android/how-to-access-instagram-api-from-android