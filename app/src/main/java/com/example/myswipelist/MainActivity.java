package com.example.myswipelist;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.widget.*;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.SearchView;

import com.example.myswipelist.MyEnhancedListView.SwipeDirection;
import com.example.myswiplelist.data.AttachmentModel;

public class MainActivity extends ActionBarActivity implements  SwipeRefreshLayout.OnRefreshListener, SearchView.OnQueryTextListener, SearchView.OnCloseListener,
												MyEnhancedListView.ViewRotationCallback, BaseViewFragment.OnFragmentInteractionListener {
	
	private MyListAdapter mAdapter;
    private MyEnhancedListView mListView;
 	private ViewGroup mContainer;
 	private ViewGroup contentViewContainer;
	private Animation animation1;
	private Animation animation2; 
	private FragmentManager fragmentManager;
	private SwipeRefreshLayout swipeLayout;
	private SearchView search;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_main);
    	
    	
    	
    	  
    	
    	swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorScheme(android.R.color.holo_blue_bright, 
                android.R.color.holo_green_light, 
                android.R.color.holo_orange_light, 
                android.R.color.holo_red_light);
        
    	fragmentManager = getFragmentManager();
    	
    	contentViewContainer = (ViewGroup) findViewById(R.id.contentViewContainer);
    	mListView = (MyEnhancedListView)findViewById(R.id.list);

    	mAdapter = new MyListAdapter(this);
    	mAdapter.resetItems();

    	mListView.setAdapter(mAdapter);
    	mListView.enableSwipeToDismiss();
    	 mListView.setSwipeRefresh(swipeLayout);
    	 mListView.setSwipeDirection(SwipeDirection.BOTH);
    	 
    	 mListView.setOnScrollListener(new OnScrollListener() {

    		 @Override
    		 public void onScrollStateChanged(AbsListView view, int scrollState) {
    		 }

    		 @Override
    		 public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    	            int position = firstVisibleItem+visibleItemCount;
    	            int limit = 10;
    	            int totalItems = 20;
    	            if(position>limit && totalItemCount>0 && !swipeLayout.isRefreshing() && position <totalItems){
    	            	swipeLayout.setRefreshing(true);
    	                onRefresh();
    	            }
    	        }
    	    });
    	 
         // Enable or disable swiping layout feature
         mListView.setSwipingLayout(R.id.swiping_layout);
         mListView.setSwipingBackgroundMsgView(R.id.swipeBackgroundView);
         
         mListView.setViewRotationCallback(this);
         
         mContainer = (ViewGroup) findViewById(R.id.container);
         // Since we are caching large views, we want to keep their cache
 		// between each animation
 		mContainer.setPersistentDrawingCache(ViewGroup.PERSISTENT_ANIMATION_CACHE);
 		animation1 = AnimationUtils.loadAnimation(this, R.anim.to_middle);
		animation2 = AnimationUtils.loadAnimation(this, R.anim.from_middle);
		
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	
	private void applyNewRotation(int position, boolean doRightSide) {
		animation1.setAnimationListener(new DisplayNext(position, doRightSide));
		mContainer.startAnimation(animation1);
	}
	
	
	@Override
	public void doListRotation(int position, boolean toRightSide) {
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		AttachmentModel modelObject = (AttachmentModel)mAdapter.getItem(position);
		
		if(toRightSide){
			ThreadViewFragment threadFragment = ThreadViewFragment.newInstance(modelObject.getEmailContent());
			transaction.replace(R.id.contentViewContainer, threadFragment);
		}else {
			transaction.replace(R.id.contentViewContainer, new MessageViewFragment());
		}
		
		transaction.commit();
		applyNewRotation(position, toRightSide);
	}

    @Override
    public void doListRotation(int position, boolean toRightSide, boolean isFullSwipe) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        AttachmentModel modelObject = (AttachmentModel)mAdapter.getItem(position);

        if(toRightSide){
            ThreadViewFragment threadFragment = ThreadViewFragment.newInstance(modelObject.getEmailContent());
            transaction.replace(R.id.contentViewContainer, threadFragment);
        }else {
            transaction.replace(R.id.contentViewContainer, new MessageViewFragment());
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Half Swipe");
            alertDialog.setMessage("Half");

            if (isFullSwipe){
                alertDialog.setTitle("Full Swipe");
                alertDialog.setMessage("Full");
            }

            alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            alertDialog.show();
        }

        transaction.commit();
        applyNewRotation(position, toRightSide);
    }

	private final class DisplayNext implements Animation.AnimationListener {
		private final int mPosition;
		private boolean doRightSide;
		
		private DisplayNext(int position, boolean toRightSide) {
			mPosition = position;
			doRightSide = toRightSide;
		}
		

		public void onAnimationStart(Animation animation) {
		}

		public void onAnimationEnd(Animation animation) {
		
				// case when Listview is clicked
				if(mPosition > -1 ){
					mListView.setVisibility(View.GONE);
					contentViewContainer.setVisibility(View.VISIBLE);
				}else {
					// case when fragment is clicked
					mListView.setVisibility(View.VISIBLE);
					mListView.requestFocus();
					contentViewContainer.setVisibility(View.GONE);
				}
				mContainer.setAnimation(animation2);
		
		}

		public void onAnimationRepeat(Animation animation) {
		}
	}


	@Override
	public void onFragmentInteraction(boolean isRight) {
		applyNewRotation(-1, isRight);
		
	}

	@Override
	public void onRefresh() {
		new Handler().postDelayed(new Runnable() {
	        @Override public void run() {
	            swipeLayout.setRefreshing(false);
	        }
	    }, 3000);
		
	}

	@Override
	public boolean onClose() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		// TODO Auto-generated method stub
		return false;
	}
	
	
}
