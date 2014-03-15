package net.londatiga.android.example;

import net.londatiga.android.example.R;
import net.londatiga.android.example.MainActivity.AnimateFirstDisplayListener;

import java.util.ArrayList;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import android.content.Context;

public class PhotoListAdapter extends BaseAdapter {
	private Context mContext;
	
	private ImageLoader mImageLoader;
	private AnimateFirstDisplayListener mAnimator;
	
	private ArrayList<String> mPhotoList;
	
	private int mWidth;
	private int mHeight;
	
	public PhotoListAdapter(Context context) {
		mContext = context;
		
		DisplayImageOptions displayOptions = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.instagram_logo)
				.showImageForEmptyUri(R.drawable.instagram_logo)
				.showImageOnFail(R.drawable.instagram_logo)
				.cacheInMemory(true)
				.cacheOnDisc(false)
				.considerExifParams(true)
				.build();

		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)                		                       
		        .writeDebugLogs()
		        .defaultDisplayImageOptions(displayOptions)		        
		        .build();

		mImageLoader = ImageLoader.getInstance();
		mImageLoader.init(config);

		mAnimator  = new AnimateFirstDisplayListener();
	}
	
	public void setData(ArrayList<String> data) {
		mPhotoList = data;
	}
	
	public void setLayoutParam(int width, int height) {
		mWidth 	= width;
		mHeight = height;
	}
	
	@Override
	public int getCount() {
		return (mPhotoList == null) ? 0 : mPhotoList.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageIv;
		
		if (convertView == null) {
			imageIv = new ImageView(mContext);
			
			imageIv.setLayoutParams(new GridView.LayoutParams(mWidth, mHeight));
            imageIv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageIv.setPadding(0, 0, 0, 0); 
		} else {
			imageIv = (ImageView) convertView;
		}
		
		mImageLoader.displayImage(mPhotoList.get(position), imageIv, mAnimator);
		
		return imageIv;
	}
}