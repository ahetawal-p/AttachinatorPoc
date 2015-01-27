/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.myswipelist;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.SystemClock;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A {@link View.OnTouchListener} that makes the list items in a {@link ListView}
 * dismissable. {@link ListView} is given special treatment because by default it handles touches
 * for its list items... i.e. it's in charge of drawing the pressed state (the list selector),
 * handling list item clicks, etc.
 *
 * <p>After creating the listener, the caller should also call
 * {@link ListView#setOnScrollListener(AbsListView.OnScrollListener)}, passing
 * in the scroll listener returned by {@link #makeScrollListener()}. If a scroll listener is
 * already assigned, the caller should still pass scroll changes through to this listener. This will
 * ensure that this {@link RecyclerViewTouchListener} is paused during list view
 * scrolling.</p>
 *
 * <p>Example usage:</p>
 *
 * <pre>
 * RecyclerViewTouchListener touchListener =
 *         new RecyclerViewTouchListener(
 *                 listView,
 *                 new RecyclerViewTouchListener.OnDismissCallback() {
 *                     public void onDismiss(ListView listView, int[] reverseSortedPositions) {
 *                         for (int position : reverseSortedPositions) {
 *                             adapter.remove(adapter.getItem(position));
 *                         }
 *                         adapter.notifyDataSetChanged();
 *                     }
 *                 });
 * listView.setOnTouchListener(touchListener);
 * listView.setOnScrollListener(touchListener.makeScrollListener());
 * </pre>
 *
 * <p>This class Requires API level 12 or later due to use of {@link
 * ViewPropertyAnimator}.</p>

 *
 */
public class RecyclerViewTouchListener implements View.OnTouchListener {
    // Cached ViewConfiguration and system-wide constant values
    private int mSlop;
    private int mMinFlingVelocity;
    private int mMaxFlingVelocity;
    private long mAnimationTime;

    // Fixed properties
    private RecyclerView mRecyclerView;
    private TextView swipeBackgroundView;
    private View swipeLayoutView;
    private DismissCallbacks mCallbacks;
    private int mViewWidth = 1; // 1 and not 0 to prevent dividing by zero

    // Transient properties
    private List<PendingDismissData> mPendingDismisses = new ArrayList<PendingDismissData>();
    private int mDismissAnimationRefCount = 0;
    private float mDownX;
    private float mDownY;
    private boolean mSwiping;
    private int mSwipingSlop;
    private VelocityTracker mVelocityTracker;
    private int mDownPosition;
    private View mDownView;
    private boolean mPaused;
    private ActionType actionType;

    /**
     * The callback interface used by {@link RecyclerViewTouchListener} to inform its client
     * about a successful dismissal of one or more list item positions.
     */
    public interface DismissCallbacks {
        /**
         * Called to determine whether the given position can be dismissed.
         */
        boolean canDismiss(int position);

        /**
         * Called when the user has indicated they she would like to dismiss one or more list item
         * positions.
         *
         * @param recyclerView               The originating {@link ListView}.
         * @param reverseSortedPositions An array of positions to dismiss, sorted in descending
         *                               order for convenience.
         */
        void onDismiss(RecyclerView recyclerView, int[] reverseSortedPositions, ActionType actionType);
    }

    /**
     * Constructs a new swipe-to-dismiss touch listener for the given list view.
     *
     * @param recyclerView  The list view whose items should be dismissable.
     * @param callbacks The callback to trigger when the user has indicated that she would like to
     *                  dismiss one or more list items.
     */
    public RecyclerViewTouchListener(RecyclerView recyclerView, DismissCallbacks callbacks) {
        ViewConfiguration vc = ViewConfiguration.get(recyclerView.getContext());
        mSlop = vc.getScaledTouchSlop();
        mMinFlingVelocity = vc.getScaledMinimumFlingVelocity() * 16;
        mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
        mAnimationTime = recyclerView.getContext().getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        mRecyclerView = recyclerView;
        mCallbacks = callbacks;
    }

    /**
     * Enables or disables (pauses or resumes) watching for swipe-to-dismiss gestures.
     *
     * @param enabled Whether or not to watch for gestures.
     */
    public void setEnabled(boolean enabled) {
        mPaused = !enabled;
    }

    /**
     * Returns an {@link AbsListView.OnScrollListener} to be added to the {@link
     * ListView} using {@link ListView#setOnScrollListener(AbsListView.OnScrollListener)}.
     * If a scroll listener is already assigned, the caller should still pass scroll changes through
     * to this listener. This will ensure that this {@link RecyclerViewTouchListener} is
     * paused during list view scrolling.</p>
     *
     * @see RecyclerViewTouchListener
     */
    public RecyclerView.OnScrollListener makeScrollListener() {
        return new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                setEnabled(newState != AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            }
        };
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (mViewWidth < 2) {
            mViewWidth = mRecyclerView.getWidth();
        }

        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                if (mPaused) {
                    return false;
                }

                // TODO: ensure this is a finger, and set a flag

                // Find the child view that was touched (perform a hit test)
                Rect rect = new Rect();
                int childCount = mRecyclerView.getChildCount();
                int[] listViewCoords = new int[2];
                mRecyclerView.getLocationOnScreen(listViewCoords);
                int x = (int) motionEvent.getRawX() - listViewCoords[0];
                int y = (int) motionEvent.getRawY() - listViewCoords[1];
                View child;
                for (int i = 0; i < childCount; i++) {
                    child = mRecyclerView.getChildAt(i);
                    child.getHitRect(rect);
                    if (rect.contains(x, y)) {
                        mDownView = child;
                        swipeBackgroundView = (TextView)mDownView.findViewById(R.id.swipeBackgroundView);
                        swipeLayoutView = mDownView.findViewById(R.id.swiping_layout);
                        break;
                    }
                }

                if (mDownView != null) {
                    mDownX = motionEvent.getRawX();
                    mDownY = motionEvent.getRawY();
                    mDownPosition = mRecyclerView.getChildPosition(mDownView);
                    if (mCallbacks.canDismiss(mDownPosition)) {
                        mVelocityTracker = VelocityTracker.obtain();
                        mVelocityTracker.addMovement(motionEvent);
                    } else {
                        mDownView = null;
                    }
                }
                return false;
            }

            case MotionEvent.ACTION_CANCEL: {
                if (mVelocityTracker == null) {
                    break;
                }

                if (mDownView != null && mSwiping) {
                    // cancel
                    mDownView.animate()
                            .translationX(0)
                            .alpha(1)
                            .setDuration(mAnimationTime)
                            .setListener(null);
                }
                mVelocityTracker.recycle();
                mVelocityTracker = null;
                mDownX = 0;
                mDownY = 0;
                mDownView = null;
                mDownPosition = ListView.INVALID_POSITION;
                mSwiping = false;
                break;
            }

            case MotionEvent.ACTION_UP: {
                if (mVelocityTracker == null) {
                    break;
                }

                float deltaX = motionEvent.getRawX() - mDownX;
                mVelocityTracker.addMovement(motionEvent);
                mVelocityTracker.computeCurrentVelocity(1000);
                float velocityX = mVelocityTracker.getXVelocity();
                float absVelocityX = Math.abs(velocityX);
                float absVelocityY = Math.abs(mVelocityTracker.getYVelocity());
                float actionLowerLimit = mViewWidth / 4 ;
                boolean dismiss = false;
                boolean dismissRight = false;
                if (Math.abs(deltaX) > actionLowerLimit && mSwiping) {
                    dismiss = true;
                    dismissRight = deltaX > 0;
                    actionType = getActionType(deltaX);

                } else if (mMinFlingVelocity <= absVelocityX && absVelocityX <= mMaxFlingVelocity
                        && absVelocityY < absVelocityX && mSwiping) {
                    // dismiss only if flinging in the same direction as dragging
                    dismiss = (velocityX < 0) == (deltaX < 0);
                    dismissRight = mVelocityTracker.getXVelocity() > 0;
                }

                if (dismiss && mDownPosition != ListView.INVALID_POSITION) {
                    // dismiss
                    final View swipeView = swipeLayoutView;
                    final int downPosition = mDownPosition;
                    final ActionType finalActionType = actionType;
                    ++mDismissAnimationRefCount;
                    swipeLayoutView.animate()
                            .translationX(dismissRight ? mViewWidth : -mViewWidth)
                            .alpha(1)
                            .setDuration(mAnimationTime)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    //performDismiss(swipeView, downPosition, finalActionType);
                                }
                            });
                } else {
                    // cancel
                    swipeLayoutView.animate()
                            .translationX(0)
                            .alpha(1)
                            .setDuration(mAnimationTime)
                            .setListener(null);
                }

                mVelocityTracker.recycle();
                mVelocityTracker = null;
                mDownX = 0;
                mDownY = 0;
                mDownView = null;
                mDownPosition = ListView.INVALID_POSITION;
                mSwiping = false;
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                if (mVelocityTracker == null || mPaused) {
                    break;
                }

                mVelocityTracker.addMovement(motionEvent);
                float deltaX = motionEvent.getRawX() - mDownX;
                float deltaY = motionEvent.getRawY() - mDownY;
                if (Math.abs(deltaX) > mSlop && Math.abs(deltaY) < Math.abs(deltaX) / 2) {
                    mSwiping = true;
                    mSwipingSlop = (deltaX > 0 ? mSlop : -mSlop);
                    mRecyclerView.requestDisallowInterceptTouchEvent(true);

                    // Cancel ListView's touch (un-highlighting the item)
                    MotionEvent cancelEvent = MotionEvent.obtain(motionEvent);
                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL |
                            (motionEvent.getActionIndex()
                                    << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                    mRecyclerView.onTouchEvent(cancelEvent);
                    cancelEvent.recycle();
                }

                changeSwipeBackgroundView(swipeBackgroundView, deltaX, mSwiping);

                if (mSwiping) {

                    swipeLayoutView.setTranslationX(deltaX - mSwipingSlop);
//                    swipeLayoutView.setAlpha(Math.max(0f, Math.min(1f,
//                            1f - 2f * Math.abs(deltaX) / mViewWidth)));

                    return true;
                }
                break;
            }
        }
        return false;
    }

    class PendingDismissData implements Comparable<PendingDismissData> {
        public int position;
        public View view;

        public PendingDismissData(int position, View view) {
            this.position = position;
            this.view = view;
        }

        @Override
        public int compareTo(PendingDismissData other) {
            // Sort by descending position
            return other.position - position;
        }
    }

    private void changeSwipeBackgroundView(View swipeBackgroundView, float deltaX, Boolean swiping){
        if (!swiping){
            ((TextView)swipeBackgroundView).setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            ((TextView)swipeBackgroundView).setText("Done");

            return;
        }

        ActionType actionType = getActionType(deltaX);
        switch (actionType){
            case VIEW_THREAD:
                setSwipeBackgroundView(mRecyclerView.getContext().getResources().getColor(R.color.Indigo),
                                        "View Thread", Gravity.LEFT | Gravity.CENTER_VERTICAL, swipeBackgroundView);
                break;
            case DONE:
                setSwipeBackgroundView(mRecyclerView.getContext().getResources().getColor(R.color.Green),
                                        "Done", Gravity.RIGHT | Gravity.CENTER_VERTICAL, swipeBackgroundView);
                break;
            case DEFER:
                setSwipeBackgroundView(mRecyclerView.getContext().getResources().getColor(R.color.Red),
                        "Defer", Gravity.RIGHT | Gravity.CENTER_VERTICAL, swipeBackgroundView);
                break;
            case SHORT_SWIPE:
                setSwipeBackgroundView(mRecyclerView.getContext().getResources().getColor(R.color.Gray),
                        "Defer", Gravity.RIGHT | Gravity.CENTER_VERTICAL, swipeBackgroundView);
                break;
        }
    }

    private void setSwipeBackgroundView(int color, String text, int gravity, View view){
        view.setBackgroundColor(color);
        ((TextView)view).setGravity(gravity);
        ((TextView)view).setText(text);
    }

    private ActionType getActionType(float deltaX){
        ActionType actionType;

        if(deltaX > 0){
            actionType = ActionType.VIEW_THREAD;
        }else {
            int halfWidth = mViewWidth / 2;
            int quarterWidth = mViewWidth / 4;
            if (deltaX < -halfWidth){
                actionType = ActionType.DONE;
            }
            else if (deltaX < -quarterWidth){
                actionType = ActionType.DEFER;
            }
            else{
                actionType = ActionType.SHORT_SWIPE;
            }
        }

        return actionType;
    }

    private void performDismiss(final View dismissView, final int dismissPosition, final ActionType actionType) {
        // Animate the dismissed list item to zero-height and fire the dismiss callback when
        // all dismissed list item animations have completed. This triggers layout on each animation
        // frame; in the future we may want to do something smarter and more performant.

        final ViewGroup.LayoutParams lp = dismissView.getLayoutParams();
        final int originalHeight = dismissView.getHeight();

        ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 1).setDuration(mAnimationTime);

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                --mDismissAnimationRefCount;
                if (mDismissAnimationRefCount == 0) {
                    // No active animations, process all pending dismisses.
                    // Sort by descending position
                    Collections.sort(mPendingDismisses);

                    int[] dismissPositions = new int[mPendingDismisses.size()];
                    for (int i = mPendingDismisses.size() - 1; i >= 0; i--) {
                        dismissPositions[i] = mPendingDismisses.get(i).position;
                    }
                    mCallbacks.onDismiss(mRecyclerView, dismissPositions, actionType);

                    // Reset mDownPosition to avoid MotionEvent.ACTION_UP trying to start a dismiss
                    // animation with a stale position
                    mDownPosition = ListView.INVALID_POSITION;

                    ViewGroup.LayoutParams lp;
                    for (PendingDismissData pendingDismiss : mPendingDismisses) {
                        // Reset view presentation
                        pendingDismiss.view.setAlpha(1f);
                        pendingDismiss.view.setTranslationX(0);
                        lp = pendingDismiss.view.getLayoutParams();
                        lp.height = originalHeight;
                        pendingDismiss.view.setLayoutParams(lp);
                    }

                    // Send a cancel event
                    long time = SystemClock.uptimeMillis();
                    MotionEvent cancelEvent = MotionEvent.obtain(time, time,
                            MotionEvent.ACTION_CANCEL, 0, 0, 0);
                    mRecyclerView.dispatchTouchEvent(cancelEvent);

                    mPendingDismisses.clear();
                }
            }
        });

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                lp.height = (Integer) valueAnimator.getAnimatedValue();
                dismissView.setLayoutParams(lp);
            }
        });

        mPendingDismisses.add(new PendingDismissData(dismissPosition, dismissView));
        animator.start();
    }
//    private void performDismiss(final View dismissView, final int dismissPosition) {
//        // Animate the dismissed list item to zero-height and fire the dismiss callback when
//        // all dismissed list item animations have completed. This triggers layout on each animation
//        // frame; in the future we may want to do something smarter and more performant.
//
//        final ViewGroup.LayoutParams lp = dismissView.getLayoutParams();
//        final int originalHeight = dismissView.getHeight();
//
//        ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 1).setDuration(mAnimationTime);
//
//        animator.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                --mDismissAnimationRefCount;
//                if (mDismissAnimationRefCount == 0) {
//                    // No active animations, process all pending dismisses.
//                    // Sort by descending position
//                    Collections.sort(mPendingDismisses);
//
//                    int[] dismissPositions = new int[mPendingDismisses.size()];
//                    for (int i = mPendingDismisses.size() - 1; i >= 0; i--) {
//                        dismissPositions[i] = mPendingDismisses.get(i).position;
//                    }
//                    mCallbacks.onDismiss(mRecyclerView, dismissPositions);
//
//                    // Reset mDownPosition to avoid MotionEvent.ACTION_UP trying to start a dismiss
//                    // animation with a stale position
//                    mDownPosition = ListView.INVALID_POSITION;
//
//                    ViewGroup.LayoutParams lp;
//                    for (PendingDismissData pendingDismiss : mPendingDismisses) {
//                        // Reset view presentation
//                        pendingDismiss.view.setAlpha(1f);
//                        pendingDismiss.view.setTranslationX(0);
//                        lp = pendingDismiss.view.getLayoutParams();
//                        lp.height = originalHeight;
//                        pendingDismiss.view.setLayoutParams(lp);
//                    }
//
//                    // Send a cancel event
//                    long time = SystemClock.uptimeMillis();
//                    MotionEvent cancelEvent = MotionEvent.obtain(time, time,
//                            MotionEvent.ACTION_CANCEL, 0, 0, 0);
//                    mRecyclerView.dispatchTouchEvent(cancelEvent);
//
//                    mPendingDismisses.clear();
//                }
//            }
//        });
//
//        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator valueAnimator) {
//                lp.height = (Integer) valueAnimator.getAnimatedValue();
//                dismissView.setLayoutParams(lp);
//            }
//        });
//
//        mPendingDismisses.add(new PendingDismissData(dismissPosition, dismissView));
//        animator.start();
//    }
}
