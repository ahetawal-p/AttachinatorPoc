package com.example.myswipelist;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.widget.*;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myswiplelist.data.AttachmentModel;
import com.example.myswiplelist.util.DeferDialogUtil;
import com.example.myswiplelist.util.DummyDataUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity implements  SwipeRefreshLayout.OnRefreshListener, SearchView.OnQueryTextListener, SearchView.OnCloseListener,
												MyEnhancedListView.ViewRotationCallback, BaseViewFragment.OnFragmentInteractionListener {

	//private MyListAdapter mAdapter;
    //private MyEnhancedListView mListView;
 	private ViewGroup mContainer;
 	private ViewGroup contentViewContainer;
	private Animation animation1;
	private Animation animation2; 
	private FragmentManager fragmentManager;
	private SwipeRefreshLayout swipeLayout;
	private SearchView search;

    /* RecyclerView items */
    private RecyclerView mRecyclerView;
    //private RecyclerView.LayoutManager mLayoutManager;
    private LinearLayoutManager mLayoutManager;
    private RecyclerViewAdapter mAdapter;
    private List<AttachmentModel> mItems = new ArrayList<AttachmentModel>();
    private Drawable tagged;
    private DeferDialogUtil deferDialogUtil;

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
        deferDialogUtil = new DeferDialogUtil();
    	contentViewContainer = (ViewGroup) findViewById(R.id.contentViewContainer);
    	resetItems();
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        tagged = getResources().getDrawable(R.drawable.tagged_bg);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
//
        mAdapter = new RecyclerViewAdapter((ArrayList)mItems);
        try {
            mRecyclerView.setAdapter(mAdapter);
        }
        catch (Exception ex){
            String asdf = ex.toString();
        }
        RecyclerViewTouchListener touchListener =
                new RecyclerViewTouchListener(
                        mRecyclerView,
                        new RecyclerViewTouchListener.DismissCallbacks() {
                            @Override
                            public boolean canDismiss(int position) {
                                return true;
                            }

                            @Override
                            public void onDismiss(RecyclerView recyclerView, int[] reverseSortedPositions, ActionType actionType) {
                                if (actionType == ActionType.VIEW_THREAD){
                                    int swipedItemPosition = reverseSortedPositions[0];
                                    doListRotation(swipedItemPosition, true);
                                }else if (actionType == ActionType.DEFER){
                                    openDeferDialog();
                                }
                                else {
                                    displayAlert(actionType);
                                }

                                /* commenting out item removal part. this was originally used for swipe to dismiss */
                                for (int position : reverseSortedPositions) {
                                    mAdapter.remove(position);
                                    mAdapter.notifyDataSetChanged();
                                }
                                // do not call notifyItemRemoved for every item, it will cause gaps on deleting items
                            }
                        });
        mRecyclerView.setOnTouchListener(touchListener);
        // Setting this scroll listener is required to ensure that during ListView scrolling,
        // we don't look for swipes.
        //mRecyclerView.setOnScrollListener(touchListener.makeScrollListener());
        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this,
                new OnRecyclerItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Toast.makeText(MainActivity.this, "Clicked " + mItems.get(position), Toast.LENGTH_SHORT).show();
                    }
                }));
//    	mListView.enableSwipeToDismiss();
//    	 mListView.setSwipeRefresh(swipeLayout);
//    	 mListView.setSwipeDirection(SwipeDirection.BOTH);

        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {

    		 @Override
    		 public void onScrollStateChanged(RecyclerView view, int scrollState) {
    		 }

             private int previousTotal = 0;
             private boolean loading = true;
             private int visibleThreshold = 5;
             int firstVisibleItem, visibleItemCount, totalItemCount;
    		 @Override
    		 public void onScrolled(RecyclerView recyclerview, int dx, int dy) {
                 super.onScrolled(recyclerview, dx, dy);

                 visibleItemCount = mRecyclerView.getChildCount();
                 totalItemCount = mLayoutManager.getItemCount();
                 firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();

                 if (loading) {
                     if (totalItemCount > previousTotal) {
                         loading = false;
                         previousTotal = totalItemCount;
                     }
                 }
                 if (!loading && (totalItemCount - visibleItemCount)
                         <= (firstVisibleItem + visibleThreshold)) {
                     // End has been reached
                     Log.i("...", "end called");

                     // Do something
                     swipeLayout.setRefreshing(true);
                     onRefresh();
                     loading = true;
                 }
    	        }
    	    });
//
//         // Enable or disable swiping layout feature
//         mListView.setSwipingLayout(R.id.swiping_layout);
//         mListView.setSwipingBackgroundMsgView(R.id.swipeBackgroundView);
//
//         mListView.setViewRotationCallback(this);

         mContainer = (ViewGroup) findViewById(R.id.container);
//         // Since we are caching large views, we want to keep their cache
// 		// between each animation
		mContainer.setPersistentDrawingCache(ViewGroup.PERSISTENT_ANIMATION_CACHE);
		animation1 = AnimationUtils.loadAnimation(this, R.anim.to_middle);
		animation2 = AnimationUtils.loadAnimation(this, R.anim.from_middle);
		
    }

    private void openDeferDialog(){
        // custom dialog
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.defer_dialog);
        GridView gridView = (GridView) dialog.findViewById(R.id.deferGridView);
        gridView.setAdapter(new DeferDialogAdapter(this, deferDialogUtil.getDeferDialogItems()));
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                String label = ((TextView) v.findViewById(R.id.grid_item_label)).getText().toString();
                if (!label.isEmpty())
                    Toast.makeText(
                            getApplicationContext(),
                            label, Toast.LENGTH_SHORT).show();
                dialog.dismiss();

            }
        });

        dialog.show();
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

	private void displayAlert(ActionType actionType){
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle(actionType.toString());
            alertDialog.setMessage(actionType.toString());

            alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            alertDialog.show();
    }

	private void applyNewRotation(int position, boolean doRightSide) {
		animation1.setAnimationListener(new DisplayNext(position, doRightSide));
		mContainer.startAnimation(animation1);
	}


	@Override
	public void doListRotation(int position, boolean toRightSide) {
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		AttachmentModel modelObject = (AttachmentModel)(mItems.get(position));

		if(toRightSide){
			ThreadViewFragment threadFragment = ThreadViewFragment.newInstance(modelObject.getEmailContent());
			transaction.replace(R.id.contentViewContainer, threadFragment);
		}else {
			transaction.replace(R.id.contentViewContainer, new MessageViewFragment());
		}

		transaction.commit();
		applyNewRotation(position, toRightSide);
	}

    /* Originally in MyListAdapter */
    private void resetItems() {
        mItems.clear();
        mItems.addAll(DummyDataUtil.prepareData());

    }

    public interface OnRecyclerItemClickListener {
        public void onItemClick(View view, int position);
    }

    public class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {
        private OnRecyclerItemClickListener mListener;

        GestureDetector mGestureDetector;

        public RecyclerItemClickListener(Context context, OnRecyclerItemClickListener listener) {
            mListener = listener;
            mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
            View childView = view.findChildViewUnder(e.getX(), e.getY());
            if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
                mListener.onItemClick(childView, view.getChildPosition(childView));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) {
        }
    }

    /* End */

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
                    mRecyclerView.setVisibility(View.GONE);
					contentViewContainer.setVisibility(View.VISIBLE);
				}else {
					// case when fragment is clicked
                    mRecyclerView.setVisibility(View.VISIBLE);
                    mRecyclerView.requestFocus();
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
                resetItems();
                mAdapter.notifyDataSetChanged();
                swipeLayout.setRefreshing(false);
	        }
	    }, 2000);
		
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
