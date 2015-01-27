package com.example.myswipelist;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.widget.*;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myswipelist.MyEnhancedListView.SwipeDirection;
import com.example.myswiplelist.data.AttachmentModel;
import com.example.myswiplelist.util.DummyDataUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity implements  SwipeRefreshLayout.OnRefreshListener, SearchView.OnQueryTextListener, SearchView.OnCloseListener,
												MyEnhancedListView.ViewRotationCallback, BaseViewFragment.OnFragmentInteractionListener {

	//private MyListAdapter mAdapter;
    private MyEnhancedListView mListView;
 	private ViewGroup mContainer;
 	private ViewGroup contentViewContainer;
	private Animation animation1;
	private Animation animation2; 
	private FragmentManager fragmentManager;
	private SwipeRefreshLayout swipeLayout;
	private SearchView search;

    /* RecyclerView items */
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter<CustomViewHolder> mAdapter;
    private List<AttachmentModel> mItems = new ArrayList<AttachmentModel>();
    private Drawable tagged;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_main);

//    	swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
//        swipeLayout.setOnRefreshListener(this);
//        swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
//                android.R.color.holo_green_light,
//                android.R.color.holo_orange_light,
//                android.R.color.holo_red_light);
        
    	fragmentManager = getFragmentManager();
    	
    	//contentViewContainer = (ViewGroup) findViewById(R.id.contentViewContainer);
//    	mListView = (MyEnhancedListView)findViewById(R.id.list);

//    	mAdapter = new MyListAdapter(this);
    	resetItems();
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        tagged = getResources().getDrawable(R.drawable.tagged_bg);
        mLayoutManager = new LinearLayoutManager(this);
//        mLayoutManager = new GridLayoutManager(this, 2);
//        mLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
//
//    	mListView.setAdapter(mAdapter);

        /* new implementation of mAdapter */
        mAdapter = new RecyclerView.Adapter<CustomViewHolder>() {
            @Override
            public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item
                        , viewGroup, false);
                return new CustomViewHolder(view);
            }

            @Override
            public void onBindViewHolder(CustomViewHolder viewHolder, int i) {
                viewHolder.position = i;
                viewHolder.senderName.setText(mItems.get(i).getSenderName());
                viewHolder.fileName.setText(mItems.get(i).getAttchFileName());
                viewHolder.date.setText(mItems.get(i).getDate());
                viewHolder.file_type.setImageResource(detectType(mItems.get(i).getAttchType()));
                if(mItems.get(i).getTagName() != ""){
                    //viewHolder.tag.setBackground(tagged);
                }
            }

            @Override
            public int getItemCount() {
                int size = mItems.size();
                try {
                    return size;
                }
                catch (Exception ex){
                    String asdf = ex.toString();
                    return 1;
                }
            }
        };
        /* end */
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
                                //Display action type via dialog for now
                                displayAlert(actionType);

                                /* commenting out item removal part. this was originally used for swipe to dismiss */
//                                for (int position : reverseSortedPositions) {
//                                    mItems.remove(position);
//                                }
//                                // do not call notifyItemRemoved for every item, it will cause gaps on deleting items
//                                mAdapter.notifyDataSetChanged();
                            }
                        });
        mRecyclerView.setOnTouchListener(touchListener);
        // Setting this scroll listener is required to ensure that during ListView scrolling,
        // we don't look for swipes.
        mRecyclerView.setOnScrollListener(touchListener.makeScrollListener());
        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this,
                new OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Toast.makeText(MainActivity.this, "Clicked " + mItems.get(position), Toast.LENGTH_SHORT).show();
                    }
                }));
//    	mListView.enableSwipeToDismiss();
//    	 mListView.setSwipeRefresh(swipeLayout);
//    	 mListView.setSwipeDirection(SwipeDirection.BOTH);
//
//    	 mListView.setOnScrollListener(new OnScrollListener() {
//
//    		 @Override
//    		 public void onScrollStateChanged(AbsListView view, int scrollState) {
//    		 }
//
//    		 @Override
//    		 public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//
//    	            int position = firstVisibleItem+visibleItemCount;
//    	            int limit = 10;
//    	            int totalItems = 20;
//    	            if(position>limit && totalItemCount>0 && !swipeLayout.isRefreshing() && position <totalItems){
//    	            	swipeLayout.setRefreshing(true);
//    	                onRefresh();
//    	            }
//    	        }
//    	    });
//
//         // Enable or disable swiping layout feature
//         mListView.setSwipingLayout(R.id.swiping_layout);
//         mListView.setSwipingBackgroundMsgView(R.id.swipeBackgroundView);
//
//         mListView.setViewRotationCallback(this);

//         mContainer = (ViewGroup) findViewById(R.id.container);
//         // Since we are caching large views, we want to keep their cache
// 		// between each animation
// 		mContainer.setPersistentDrawingCache(ViewGroup.PERSISTENT_ANIMATION_CACHE);
// 		animation1 = AnimationUtils.loadAnimation(this, R.anim.to_middle);
//		animation2 = AnimationUtils.loadAnimation(this, R.anim.from_middle);
		
    }

    private class CustomViewHolder extends RecyclerView.ViewHolder {
        private TextView senderName;
        private TextView fileName;
        private TextView date;
        private ImageView file_type;
        private ImageView tag;
        private int position;

        public CustomViewHolder(View itemView) {
            super(itemView);

            senderName = (TextView) itemView.findViewById(R.id.senderName);
            fileName = (TextView) itemView.findViewById(R.id.fileName);
            date = (TextView) itemView.findViewById(R.id.date);
            file_type = (ImageView) itemView.findViewById(R.id.file_type);
            senderName = (TextView) itemView.findViewById(R.id.senderName);
        }
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
//		AttachmentModel modelObject = (AttachmentModel)mAdapter.getItem(position);
//
//		if(toRightSide){
//			ThreadViewFragment threadFragment = ThreadViewFragment.newInstance(modelObject.getEmailContent());
//			transaction.replace(R.id.contentViewContainer, threadFragment);
//		}else {
//			transaction.replace(R.id.contentViewContainer, new MessageViewFragment());
//		}
//
//		transaction.commit();
//		applyNewRotation(position, toRightSide);
	}

    @Override
    public void doListRotation(int position, boolean toRightSide, boolean isFullSwipe) {
//        FragmentTransaction transaction = fragmentManager.beginTransaction();
//        AttachmentModel modelObject = (AttachmentModel)mAdapter.getItem(position);
//
//        if(toRightSide){
//            ThreadViewFragment threadFragment = ThreadViewFragment.newInstance(modelObject.getEmailContent());
//            transaction.replace(R.id.contentViewContainer, threadFragment);
//        }else {
//            transaction.replace(R.id.contentViewContainer, new MessageViewFragment());
//            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
//            alertDialog.setTitle("Half Swipe");
//            alertDialog.setMessage("Half");
//
//            if (isFullSwipe){
//                alertDialog.setTitle("Full Swipe");
//                alertDialog.setMessage("Full");
//            }
//
//            alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
//                public void onClick(DialogInterface dialog, int which) {
//                }
//            });
//            alertDialog.show();
//        }
//
//        transaction.commit();
//        applyNewRotation(position, toRightSide);
    }

    /* Originally in MyListAdapter */
    private void resetItems() {
        mItems.clear();
        mItems.addAll(DummyDataUtil.prepareData());
    }

    private int detectType(AttachmentModel.ATTACHMENT_TYPE attchType) {
        int id = R.drawable.no_image;

        switch (attchType) {
            case ARCHIVE :
                id = R.drawable.file_type_archive;
                break;
            case AUDIO :
                id = R.drawable.file_type_audio;
                break;
            case DOC :
                id = R.drawable.file_type_doc;
                break;
            case DRAWING :
                id = R.drawable.file_type_drawing;
                break;
            case EXCEL :
                id = R.drawable.file_type_excel;
                break;
            case TEXT :
                id = R.drawable.file_type_file;
                break;
            case IMAGE :
                id = R.drawable.file_type_image;
                break;
            case PDF :
                id = R.drawable.file_type_pdf;
                break;
            case POWERPOINT :
                id = R.drawable.file_type_powerpoint;
                break;
            case VIDEO :
                id = R.drawable.file_type_video;
                break;
            case WORD :
                id = R.drawable.file_type_word;
                break;
            default :
                id = R.drawable.file_type_fusion;

        }
        return id;

    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }

    public class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {
        private OnItemClickListener mListener;

        GestureDetector mGestureDetector;

        public RecyclerItemClickListener(Context context, OnItemClickListener listener) {
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
