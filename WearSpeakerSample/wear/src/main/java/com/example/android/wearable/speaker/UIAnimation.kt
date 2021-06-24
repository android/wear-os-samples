/*
 * Copyright (C) 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.wearable.speaker

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Point
import android.graphics.Rect
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView

/**
 * A helper class to provide a simple animation when user selects any of the three icons on the
 * main UI.
 */
class UIAnimation(
    private val containerView: View,
    private val thumbs: Array<ImageView>,
    private val expandedImageView: ImageView,
    private val animationDurationTime: Int,
    private val mListener: UIStateListener
) {
    private var mCurrentAnimator: AnimatorSet? = null
    private val mLargeDrawables = intArrayOf(R.drawable.ic_mic_120dp,
                                             R.drawable.ic_play_arrow_120dp, R.drawable.ic_audiotrack_120dp)
    private var mState: UIState? = UIState.HOME
    private fun zoomImageFromThumb(index: Int) {
        val imageResId = mLargeDrawables[index]
        val thumbView = thumbs[index]
        if (mCurrentAnimator != null) {
            return
        }
        expandedImageView.setImageResource(imageResId)
        val startBounds = Rect()
        val finalBounds = Rect()
        val globalOffset = Point()
        thumbView!!.getGlobalVisibleRect(startBounds)
        containerView.getGlobalVisibleRect(finalBounds, globalOffset)
        startBounds.offset(-globalOffset.x, -globalOffset.y)
        finalBounds.offset(-globalOffset.x, -globalOffset.y)
        val startScale: Float
        if (finalBounds.width().toFloat() / finalBounds.height()
            > startBounds.width().toFloat() / startBounds.height()
        ) {
            startScale = startBounds.height().toFloat() / finalBounds.height()
            val startWidth = startScale * finalBounds.width()
            val deltaWidth = (startWidth - startBounds.width()) / 2
            startBounds.left -= deltaWidth.toInt()
            startBounds.right += deltaWidth.toInt()
        } else {
            startScale = startBounds.width().toFloat() / finalBounds.width()
            val startHeight = startScale * finalBounds.height()
            val deltaHeight = (startHeight - startBounds.height()) / 2
            startBounds.top -= deltaHeight.toInt()
            startBounds.bottom += deltaHeight.toInt()
        }
        for (k in 0..2) {
            thumbs[k]!!.alpha = 0f
        }
        expandedImageView.visibility = View.VISIBLE
        expandedImageView.pivotX = 0f
        expandedImageView.pivotY = 0f
        val zommInAnimator = AnimatorSet()
        zommInAnimator.play(ObjectAnimator
                                .ofFloat(expandedImageView,
                                         View.X,
                                         startBounds.left.toFloat(),
                                         finalBounds.left.toFloat())).with(
            ObjectAnimator.ofFloat(expandedImageView, View.Y, startBounds.top.toFloat(), finalBounds.top.toFloat()))
            .with(
                ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X, startScale, 1f))
            .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_Y, startScale, 1f))
        zommInAnimator.duration = animationDurationTime.toLong()
        zommInAnimator.interpolator = DecelerateInterpolator()
        zommInAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                mCurrentAnimator = null
                if (mListener != null) {
                    mState = UIState.getUIState(index)
                    mListener.onUIStateChanged(mState)
                }
            }

            override fun onAnimationCancel(animation: Animator) {
                mCurrentAnimator = null
            }
        })
        zommInAnimator.start()
        mCurrentAnimator = zommInAnimator
        expandedImageView.setOnClickListener(View.OnClickListener {
            if (mCurrentAnimator != null) {
                return@OnClickListener
            }
            val zoomOutAnimator = AnimatorSet()
            zoomOutAnimator.play(ObjectAnimator
                                     .ofFloat(expandedImageView, View.X, startBounds.left.toFloat()))
                .with(ObjectAnimator
                          .ofFloat(expandedImageView,
                                   View.Y, startBounds.top.toFloat()))
                .with(ObjectAnimator
                          .ofFloat(expandedImageView,
                                   View.SCALE_X, startScale))
                .with(ObjectAnimator
                          .ofFloat(expandedImageView,
                                   View.SCALE_Y, startScale))
            zoomOutAnimator.duration = animationDurationTime.toLong()
            zoomOutAnimator.interpolator = DecelerateInterpolator()
            zoomOutAnimator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    for (k in 0..2) {
                        thumbs[k]!!.alpha = 1f
                    }
                    expandedImageView.visibility = View.GONE
                    mCurrentAnimator = null
                    if (mListener != null) {
                        mState = UIState.HOME
                        mListener.onUIStateChanged(mState)
                    }
                }

                override fun onAnimationCancel(animation: Animator) {
                    thumbView.alpha = 1f
                    expandedImageView.visibility = View.GONE
                    mCurrentAnimator = null
                }
            })
            zoomOutAnimator.start()
            mCurrentAnimator = zoomOutAnimator
        })
    }

    enum class UIState(private val mState: Int) {
        MIC_UP(0), SOUND_UP(1), MUSIC_UP(2), HOME(3);

        companion object {
            fun getUIState(state: Int): UIState? {
                for (uiState in values()) {
                    if (uiState.mState == state) {
                        return uiState
                    }
                }
                return null
            }
        }
    }

    interface UIStateListener {
        fun onUIStateChanged(state: UIState?)
    }

    fun transitionToHome() {
        if (mState == UIState.HOME) {
            return
        }
        expandedImageView.callOnClick()
    }

    init {
        thumbs[0]!!.setOnClickListener { zoomImageFromThumb(0) }
        thumbs[1]!!.setOnClickListener { zoomImageFromThumb(1) }
        thumbs[2]!!.setOnClickListener { zoomImageFromThumb(2) }
    }
}