package com.example.myswipelist;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.widget.*;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myswiplelist.data.AttachmentModel;
import com.example.myswiplelist.util.DeferDialogUtil;
import com.example.myswiplelist.util.DummyDataUtil;

import org.apmem.tools.layouts.FlowLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
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
    private List<String> usersTags;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_main);
        initData();
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
        mAdapter = new RecyclerViewAdapter((ArrayList)mItems, new RecyclerViewAdapter.ClickTagCallback() {
            @Override
            public void onClickTag() {
                openTagDialog();
            }
        });
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
                        openTagDialog();
                        //Toast.makeText(MainActivity.this, "Clicked " + mItems.get(position), Toast.LENGTH_SHORT).show();
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
    Dialog dialog;

    private void initData(){
        usersTags = DummyDataUtil.getRecentTags();
    }

    private void openTagDialog(){
        dialog = new Dialog(this);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setTitle("Tag this attachment");
        dialog.setContentView(R.layout.tag_dialog);

        LayoutInflater inflater =
                (LayoutInflater)this.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        final MultiAutoCompleteTextView tagInputTextView = (MultiAutoCompleteTextView)dialog.findViewById(R.id.tagInputTextView);
        final SpannableStringBuilder sb = new SpannableStringBuilder();
        Button saveButton = (Button)dialog.findViewById(R.id.saveButton);

        FlowLayout layout = (FlowLayout) dialog.findViewById(R.id.recentTagsContainer);
        for(int ii=0;ii< usersTags.size();ii++){
            String recentTagText = usersTags.get(ii);
            View recentTagParent = inflater.inflate( R.layout.recent_tag_dialog_item, null );
            TextView recentTagView = (TextView)recentTagParent.findViewById(R.id.recentTagTextView);
            recentTagView.setText(recentTagText);
            recentTagView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String tagText = ((TextView)v).getText().toString();
                    appendToInputTextView(tagText, sb, tagInputTextView);
                }
            });
            layout.addView(recentTagParent);
        }

        /* handle tag user input */
        tagInputTextView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer() );
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
        android.R.layout.simple_dropdown_item_1line, usersTags);
        tagInputTextView.setAdapter(adapter);
        tagInputTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // TODO Auto-generated method stub
                String selectedRecentTag = (String) parent.getItemAtPosition(position);
//                TextView tv = createContactTextView(selectedRecentTag);
                appendToInputTextView(selectedRecentTag, sb, tagInputTextView);
            }
        });

        /* delete button to clear all input usersTags */
        dialog.findViewById(R.id.deleteTextView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tagInputTextView.setText("");
                sb.clear();
            }
        });

        /* need to update SpannableStringBuilder to match user input's text */
        tagInputTextView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_DEL){
                    //this is for backspace
                    sb.clear();
                    sb.append(tagInputTextView.getText());
                }
                return false;
            }
        });

        /* need this or else keyboard won't show up for AutoCompleteTextView */
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(tagInputTextView, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputText = tagInputTextView.getText().toString();
                List<String> inputTags = Arrays.asList(inputText.split(",\\s*"));
                /* dedupe usersTags in case of duplicates */
                for(String inputTag : inputTags){
                    if (usersTags.contains(inputTag)){}
                    else{
                        usersTags.add(inputTag);
                     }
                }

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void appendToInputTextView(String selectedRecentTag, SpannableStringBuilder sb, MultiAutoCompleteTextView tagInputTextView) {
        View tv = createContactTextView(selectedRecentTag);

        BitmapDrawable bd = (BitmapDrawable) convertViewToDrawable(tv);
        bd.setBounds(0, 0, tv.getWidth(),
                tv.getHeight());
        sb.append(selectedRecentTag + ", ");

        sb.setSpan(new ImageSpan(bd),
                sb.length() - selectedRecentTag.length()-2, sb.length()-2,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        tagInputTextView.setText("");
        tagInputTextView.setText(sb);
        int length = sb.length();
        tagInputTextView.setSelection(length, length);
    }

    private void deleteTag(String tagText){
        String fullTagText = tagText + ", ";
        TextView inputTextView = (TextView)dialog.findViewById(R.id.tagInputTextView);

        String inputTextString = inputTextView.getText().toString();
        inputTextString.replace(fullTagText, "");
        inputTextView.setText(inputTextString);
    }

    public View createContactTextView(String text) {
        LayoutInflater inflater =
                (LayoutInflater)this.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View recentTagParent = inflater.inflate( R.layout.tag_dialog_item, null );
        TextView recentTagView = (TextView)recentTagParent.findViewById(R.id.tagTextView);
        recentTagView.setText(text);

/*        TextView deleteTextView = (TextView)recentTagParent.findViewById(R.id.deleteTextView);
        deleteTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View parentView = ((View)v.getParent());
                TextView siblingTextView = ((TextView)parentView.findViewById(R.id.tagTextView));
                String tagText = siblingTextView.getText().toString();
                deleteTag(tagText);
            }
        });*/

        return recentTagParent;
        //LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 44);
        //llp.setMargins(5, 0, 5, 0);
/*        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(20);
        tv.setTextColor(Color.WHITE);
        tv.setPadding(5, 0, 5, 0);
        tv.setBackgroundResource(R.drawable.recent_tags_shape);*/
        //tv.setLayoutParams(llp);
        //Resources r = getResources();
//        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
//                44, r.getDisplayMetrics());
//        tv.setHeight(px);
        //return tv;
    }

    public static Object convertViewToDrawable(View view) {
        int spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(spec, spec);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        Bitmap b = Bitmap.createBitmap(view.getMeasuredWidth(),
                view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        c.translate(-view.getScrollX(), -view.getScrollY());
        view.draw(c);
        view.setDrawingCacheEnabled(true);
        Bitmap cacheBmp = view.getDrawingCache();
        Bitmap viewBmp = cacheBmp.copy(Bitmap.Config.ARGB_8888, true);
        view.destroyDrawingCache();
        return new BitmapDrawable(viewBmp);
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
