package com.example.myswipelist;

import java.util.LinkedList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

@SuppressLint("NewApi")
public class MyEnhancedListView extends ListView {
	
	 // Cached ViewConfiguration and system-wide constant values
private float mSlop;
private int mMinFlingVelocity;
private int mMaxFlingVelocity;
private long mAnimationTime;

private final Object[] mAnimationLock = new Object[0];

// Swipe-To-Dismiss
private boolean mSwipeEnabled;
private OnShouldSwipeCallback mShouldSwipeCallback;
private SwipeDirection mSwipeDirection = SwipeDirection.BOTH;
private int mSwipingLayout;

private List<View> mAnimatedViews = new LinkedList<View>();

private boolean mSwipePaused;
private boolean mSwiping;
private int mViewWidth = 1; // 1 and not 0 to prevent dividing by zero
private int mDisplayWidthPixels;
private View mSwipeDownView;
private View mSwipeDownChild;
private VelocityTracker mVelocityTracker;
private float mDownX;
private int mDownPosition;
private ViewRotationCallback mRotationCallback;

private SwipeRefreshLayout swipeLayout;
private View mSwipingBackgroundMsgView;
private int mSwipingBackgroundMsgLayout;

// END Swipe-To-Dismiss

public enum SwipeDirection {

    /**
     * The user can swipe each item into both directions (left and right) to delete it.
     */
    BOTH,

    /**
     * The user can only swipe the items to the beginning of the item to
     * delete it. The start of an item is in Left-To-Right languages the left
     * side and in Right-To-Left languages the right side. Before API level
     * 17 this is always the left side.
     */
    START,

    /**
     * The user can only swipe the items to the end of the item to delete it.
     * This is in Left-To-Right languages the right side in Right-To-Left
     * languages the left side. Before API level 17 this will always be the
     * right side.
     */
    END

}	
	public MyEnhancedListView setViewRotationCallback(ViewRotationCallback callback){
		mRotationCallback = callback;
		return this;
	}
	
	public MyEnhancedListView setSwipeRefresh(SwipeRefreshLayout callback){
		swipeLayout = callback;
		return this;
	}
	
	public MyEnhancedListView setSwipingBackgroundMsgView(int swipeBkgMsgViewId){
		mSwipingBackgroundMsgLayout = swipeBkgMsgViewId;
		return this;
	}
	
	public MyEnhancedListView setSwipingLayout(int swipingLayoutId) {
		mSwipingLayout = swipingLayoutId;
		return this;
	}

	public MyEnhancedListView setSwipeDirection(SwipeDirection direction) {
		mSwipeDirection = direction;
		return this;
	}
	
	public interface OnShouldSwipeCallback {
    /**
     * Called when the user is swiping an item from the list.
     * <p>
     * If the user should get the possibility to swipe the item, return true.
     * Otherwise, return false to disable swiping for this item.
     *
     * @param listView The {@link EnhancedListView} the item is wiping from.
     * @param position The position of the item to swipe in your adapter.
     * @return Whether the item should be swiped or not.
     */
    boolean onShouldSwipe(MyEnhancedListView listView, int position);

}

	public interface ViewRotationCallback {
		void doListRotation(int position, boolean toRightSide);
		void doListRotation(int position, boolean toRightSide, boolean isFullSwipe);
	}

 public MyEnhancedListView(Context context) {
        super(context);
        init(context);
    }

    /**
     * {@inheritDoc}
     */
    public MyEnhancedListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    /**
     * {@inheritDoc}
     */
    public MyEnhancedListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context ctx) {

        if(isInEditMode()) {
            // Skip initializing when in edit mode (IDE preview).
            return;
        }
        ViewConfiguration vc =ViewConfiguration.get(ctx);
        mSlop = getResources().getDimension(R.dimen.elv_touch_slop);
		mMinFlingVelocity = vc.getScaledMinimumFlingVelocity();
        mDisplayWidthPixels = ctx.getResources().getDisplayMetrics().widthPixels;
        mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
        mAnimationTime = ctx.getResources().getInteger(
                android.R.integer.config_shortAnimTime);

        // END initialize undo popup

        setOnScrollListener(makeScrollListener());

    }
    
    private OnScrollListener makeScrollListener() {
        return new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                mSwipePaused = scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL;
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        };
    }
    
    public MyEnhancedListView enableSwipeToDismiss() {
        mSwipeEnabled = true;

        return this;
    }
    
    @SuppressLint("NewApi")
	private boolean isSwipeDirectionValid(float deltaX) {

        int rtlSign = 1;
        // On API level 17 and above, check if we are in a Right-To-Left layout
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if(getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
                rtlSign = -1;
            }
        }

        // Check if swipe has been done in the correct direction
        switch(mSwipeDirection) {
            default:
            case BOTH:
                return true;
            case START:
                return rtlSign * deltaX < 0;
            case END:
                return rtlSign * deltaX > 0;
        }

    }
    
    /**
     * Slide out a view to the right or left of the list. After the animation has finished, the
     * view will be dismissed by calling {@link #performDismiss(android.view.View, android.view.View, int)}.
     *
     * @param view The view, that should be slided out.
     * @param childView The whole view of the list item.
     * @param position The item position of the item.
     * @param toRightSide Whether it should slide out to the right side.
     */
    private void slideOutView(final View view, final View childView, final int position, boolean toRightSide, boolean isFullSwipe) {

        // Only start new animation, if this view isn't already animated (too fast swiping bug)
        synchronized(mAnimationLock) {
            if(mAnimatedViews.contains(view)) {
                return;
            }
            mAnimatedViews.add(view);
        }
        
        mRotationCallback.doListRotation(position, toRightSide, isFullSwipe);
        mAnimatedViews.remove(view);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
    	
        if (!mSwipeEnabled) {
            return super.onTouchEvent(ev);
        }

        // Store width of this list for usage of swipe distance detection
        if (mViewWidth < 2) {
            mViewWidth = getWidth();
        }

        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                if (mSwipePaused) {
                    return super.onTouchEvent(ev);
                }

                // TODO: ensure this is a finger, and set a flag

                // Find the child view that was touched (perform a hit test)
                Rect rect = new Rect();
                int childCount = getChildCount();
                int[] listViewCoords = new int[2];
                getLocationOnScreen(listViewCoords);
                int x = (int) ev.getRawX() - listViewCoords[0];
                int y = (int) ev.getRawY() - listViewCoords[1];
                View child;
                for (int i = getHeaderViewsCount(); i < childCount; i++) {
                    child = getChildAt(i);
                    if(child != null) {
                        child.getHitRect(rect);
                        if (rect.contains(x, y)) {
                            // if a specific swiping layout has been giving, use this to swipe.
                            if(mSwipingLayout > 0) {
                                View swipingView = child.findViewById(mSwipingLayout);
                                mSwipingBackgroundMsgView = child.findViewById(mSwipingBackgroundMsgLayout);
                                if(swipingView != null) {
                                    mSwipeDownView = swipingView;
                                    mSwipeDownChild = child;
                                    
                                    break;
                                }
                            }
                            // If no swiping layout has been found, swipe the whole child
                            mSwipeDownView = mSwipeDownChild = child;
                            break;
                        }
                    }
                }

                if (mSwipeDownView != null) {
                    // test if the item should be swiped
                    int position = getPositionForView(mSwipeDownView) - getHeaderViewsCount();
                    if ((mShouldSwipeCallback == null) || mShouldSwipeCallback.onShouldSwipe(this, position)) {
                    		mDownX = ev.getRawX();
                    		mDownPosition = position;

                    mVelocityTracker = VelocityTracker.obtain();
                    mVelocityTracker.addMovement(ev);
                    } else {
                        // set back to null to revert swiping
                        mSwipeDownView = mSwipeDownChild = null;
                    }
                }
                super.onTouchEvent(ev);
                return true;
            }

            case MotionEvent.ACTION_UP: {
                if (mVelocityTracker == null) {
                    break;
                }

                float deltaX = ev.getRawX() - mDownX;
                mVelocityTracker.addMovement(ev);
                mVelocityTracker.computeCurrentVelocity(1000);
                float velocityX = Math.abs(mVelocityTracker.getXVelocity());
                float velocityY = Math.abs(mVelocityTracker.getYVelocity());
                boolean dismiss = false;
                boolean dismissRight = false;
                Boolean isFullSwipe = false;

                if (Math.abs(deltaX) > mViewWidth / 2 && mSwiping) {
                    dismiss = true;
                    dismissRight = deltaX > 0;
                    isFullSwipe = true;
                }
                else if (Math.abs(deltaX) > mViewWidth / 4 && mSwiping){
                    dismiss = true;
                    dismissRight = deltaX > 0;
                }
                else if (mMinFlingVelocity <= velocityX && velocityX <= mMaxFlingVelocity
                        && velocityY < velocityX && mSwiping && isSwipeDirectionValid(mVelocityTracker.getXVelocity())
                        && deltaX >= mViewWidth * 0.2f) {
                    dismiss = true;
                    dismissRight = mVelocityTracker.getXVelocity() > 0;
                }
                
                if (dismiss) {
                    // dismiss
                    slideOutView(mSwipeDownView, mSwipeDownChild, mDownPosition, dismissRight, isFullSwipe);
                    //slideOutView(mSwipeDownView, mSwipeDownChild, mDownPosition, dismissRight);
                    ViewHelper.setAlpha(mSwipeDownView, 255);
                    ViewHelper.setTranslationX(mSwipeDownView, 0);
                    
                } else if(mSwiping) {
                    // Swipe back to regular position
                    ViewPropertyAnimator.animate(mSwipeDownView)
                            .translationX(0)
                            .alpha(1)
                            .setDuration(mAnimationTime)
                            .setListener(null);
                }
                mVelocityTracker.recycle(); //added new by me
                mVelocityTracker = null;
                mDownX = 0;
                mSwipeDownView = null;
                mSwipeDownChild = null;
                mDownPosition = AbsListView.INVALID_POSITION;
                mSwiping = false;
                mSwipingBackgroundMsgView = null;
                swipeLayout.setEnabled(true);
                break;
            }

            case MotionEvent.ACTION_MOVE: {

                if (mVelocityTracker == null || mSwipePaused) {
                    break;
                }

                mVelocityTracker.addMovement(ev);
                float deltaX = ev.getRawX() - mDownX;
                // Only start swipe in correct direction
                if(isSwipeDirectionValid(deltaX)) {
                    ViewParent parent = getParent();
                    if(parent != null) {
                        // If we swipe don't allow parent to intercept touch (e.g. like NavigationDrawer does)
                        // otherwise swipe would not be working.
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                    if (Math.abs(deltaX) > mSlop) {
                        mSwiping = true;
                        requestDisallowInterceptTouchEvent(true);

                        // Cancel ListView's touch (un-highlighting the item)
                        MotionEvent cancelEvent = MotionEvent.obtain(ev);
                        cancelEvent.setAction(MotionEvent.ACTION_CANCEL
                                | (ev.getActionIndex()
                                << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                        super.onTouchEvent(cancelEvent);
                    }
                } else {
                    // If we swiped into wrong direction, act like this was the new
                    // touch down point
	                    mDownX = ev.getRawX();
	                    deltaX = 0;
	                }
                	
                if(deltaX > 0){
                        mSwipingBackgroundMsgView.setBackgroundColor(getResources().getColor(R.color.Indigo));
                        ((TextView)mSwipingBackgroundMsgView).setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
                        ((TextView)mSwipingBackgroundMsgView).setText("View Thread");
                }else {
                    int halfWidth = mViewWidth / 2;

                    if (deltaX < -halfWidth){
                        mSwipingBackgroundMsgView.setBackgroundColor(getResources().getColor(R.color.Olive));
                        ((TextView)mSwipingBackgroundMsgView).setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
                        ((TextView)mSwipingBackgroundMsgView).setText("Done");
                    }
                    else{
                        mSwipingBackgroundMsgView.setBackgroundColor(getResources().getColor(R.color.Blue_Aqua));
                        ((TextView)mSwipingBackgroundMsgView).setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
                        ((TextView)mSwipingBackgroundMsgView).setText("Defer");
                    }
                }
                
	                if (mSwiping) {
	                	ViewHelper.setTranslationX(mSwipeDownView, deltaX);
	                    ViewHelper.setAlpha(mSwipeDownView, Math.max(0f, Math.min(1f,
	                            1f - 2f * Math.abs(deltaX) / mViewWidth)));
	                    
	                    ViewHelper.setAlpha(mSwipingBackgroundMsgView, Math.max(0f, Math.min(1f,
	                            1f - 1f * Math.abs(deltaX) / mViewWidth)));
	                    swipeLayout.setEnabled(false);
	                    return true;
	                }
	                break;
	            }
	        }
	        return super.onTouchEvent(ev);
	    }

	    

}
