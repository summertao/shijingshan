package com.panguso.android.shijingshan.news;

import java.util.ArrayList;
import java.util.List;

import com.panguso.android.shijingshan.net.NetworkService.ImageRequestListener;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * The article page view.
 * 
 * @author Luo Yinzhuo
 */
public class NewsPageView extends View {
	/** The news page manager. */
	private final NewsPageManager mNewsPageManager;

	/**
	 * Construct a new instance.
	 * 
	 * @param context The context.
	 * @param attrs The attributes.
	 */
	public NewsPageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mNewsPageManager = new NewsPageManager();
	}

	/**
	 * Initialize the {@link NewsPageManager}.
	 * 
	 * @param newsPages The list of {@link NewsPage}.
	 * @param newsPagePosition The initial news page position.
	 * @author Luo Yinzhuo
	 */
	public void initialize(List<NewsPage> newsPages, int newsPagePosition) {
		this.mNewsPageManager.initialize(newsPages, newsPagePosition);
		this.invalidate();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		NewsPage.initialize(w, h);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawColor(Color.WHITE);
		this.mNewsPageManager.draw(canvas);
	}

	private class NewsPageManager implements ImageRequestListener {
		/** The news page list. */
		private final List<NewsPage> mNewsPages = new ArrayList<NewsPage>();
		/** The news page position manager. */
		private final NewsPagePositionManager mNewsPagePositionManager = new NewsPagePositionManager();

		/**
		 * Initialize itself from a list of {@link NewsPage}s.
		 * 
		 * @param newsPages The list of {@link NewsPage}s.
		 * @param newsPagePosition The initial news page position.
		 * @author Luo Yinzhuo
		 */
		public void initialize(List<NewsPage> newsPages, int newsPagePosition) {
			this.mNewsPages.clear();
			this.mNewsPages.addAll(newsPages);
			this.mNewsPagePositionManager.setNewsPagePosition(newsPagePosition);
		}

		/**
		 * Draw the news pages to the canvas.
		 * 
		 * @param canvas The {@link NewsPageView}'s canvas.
		 * @author Luo Yinzhuo
		 */
		public void draw(Canvas canvas) {
			final float newsPagePosition = this.mNewsPagePositionManager.getNewsPagePosition();

			if (this.mNewsPages.size() > 0) {
				final int left = (int) Math.floor(newsPagePosition);
				float offsetX = (left - newsPagePosition) * NewsPageView.this.getWidth();
				canvas.save();
				canvas.translate(offsetX, 0);

				if (left >= 0 && left < this.mNewsPages.size()) {
					NewsPage leftPage = this.mNewsPages.get(left);
					leftPage.draw(canvas, this);
				}

				final int right = left + 1;
				if (right < this.mNewsPages.size()) {
					NewsPage rightPage = this.mNewsPages.get(right);
					canvas.translate(NewsPageView.this.getWidth(), 0);
					rightPage.draw(canvas, this);
				}

				canvas.restore();
			}

			if (this.mNewsPagePositionManager.hasAnimation()) {
				Log.d("NewsPageManager", "draw has animation.");
				NewsPageView.this.invalidate();
			}
		}

		/**
		 * Specific for manage the news page position.
		 * 
		 * @author Luo Yinzhuo
		 */
		private class NewsPagePositionManager {
			/** The animation basic duration time. */
			private static final long DURATION_BASE = 300L;
			/** The mount of bouncing value. */
			private static final float BOUNCING = 0.3f;

			/** The current news page animation. */
			private NewsPageAnimation mNewsPageAnimation;
			/** The direction. */
			private boolean mDirection;

			/**
			 * Start animation.
			 * 
			 * @param direction True to animate to the left page, otherwise
			 *        false.
			 * @param bouncing True to have bounce effect when need, otherwise
			 *        false.
			 * @author Luo Yinzhuo
			 */
			public void animate(boolean direction, boolean bouncing) {
				if (this.mNewsPageAnimation == null) {
					this.mNewsPageAnimation = new NewsPageAnimation(direction, bouncing);
				} else {
					this.mNewsPageAnimation.animate(direction, bouncing);
				}
				this.mDirection = direction;
			}

			/**
			 * Scroll the news page.
			 * 
			 * @param distanceX
			 *        The scroll distance along X axis.
			 * @author Luo Yinzhuo
			 */
			public void scroll(float distanceX) {
				final float leftBorder = -BOUNCING;
				final float leftEdge = 0;
				final float rightEdge = NewsPageManager.this.mNewsPages.size() > 0 ? NewsPageManager.this.mNewsPages
				        .size() - 1 : 0;
				final float rightBorder = rightEdge + BOUNCING;
				float estimateColumnPagePosition;
				if (this.mNewsPagePosition < leftEdge || this.mNewsPagePosition > rightEdge) {
					estimateColumnPagePosition = this.mNewsPagePosition + distanceX
					        / NewsPageView.this.getWidth() * BOUNCING;
				} else {
					estimateColumnPagePosition = this.mNewsPagePosition + distanceX
					        / NewsPageView.this.getWidth();
				}

				if (distanceX > 0) {
					this.mNewsPagePosition = Math.max(estimateColumnPagePosition, leftBorder);
				} else {
					this.mNewsPagePosition = Math.min(estimateColumnPagePosition, rightBorder);
				}
			}

			/**
			 * Pause the animation.
			 * 
			 * @author Luo Yinzhuo
			 */
			public void pause() {
				this.mNewsPagePosition = this.mNewsPageAnimation.getNewsPagePosition();
				this.mNewsPageAnimation = null;
			}

			/**
			 * Resume to animate the nearest {@link ColumnPage}.
			 * 
			 * @return True if it needs to resume, otherwise false.
			 * @author Luo Yinzhuo
			 */
			public boolean resume() {
				if (this.mNewsPagePosition != Math.floor(this.mNewsPagePosition + 0.5f)) {
					this.mNewsPageAnimation = new NewsPageAnimation(
					        (int) Math.floor(this.mNewsPagePosition + 0.5f));
					return true;
				} else {
					return false;
				}
			}

			/**
			 * Check if the news page animation exist.
			 * 
			 * @return True if the news page animation exist, otherwise false.
			 * @author Luo Yinzhuo
			 */
			public boolean hasAnimation() {
				return this.mNewsPageAnimation != null;
			}

			/**
			 * Get the animation direction.
			 * 
			 * @return True to animate to the left page, otherwise to the right
			 *         page.
			 * @author Luo Yinzhuo
			 */
			public boolean getAnimationDirection() {
				return this.mDirection;
			}
			
			/**
			 * Invoked when the news page animation is finished.
			 * 
			 * @author Luo Yinzhuo
			 */
			void onAnimationFinished() {
				this.mNewsPageAnimation = null;
			}

			/**
			 * Specified for manage the news page animation.
			 * 
			 * @author Luo Yinzhuo
			 */
			private class NewsPageAnimation {
				/** The no bounce. */
				private static final int BOUNCE_NONE = 0;
				/** The left bounce. */
				private static final int BOUNCE_LEFT = 1;
				/** The right bounce. */
				private static final int BOUNCE_RIGHT = 2;

				/** The animation start time. */
				private long mStartTime = 0L;
				/** The animation start page position. */
				private float mStartPagePosition = 0f;
				/** The target page position. */
				private int mTargetPagePosition = 0;
				/** The bounce. */
				private int mBouncing = BOUNCE_NONE;

				/** The duration. */
				private long mDuration;

				/**
				 * Construct a new instance.
				 * 
				 * @param direction True to animate to the left page, otherwise
				 *        to the right page.
				 * @param bouncing True to add bounce effect if need, otherwise
				 *        false.
				 */
				public NewsPageAnimation(boolean direction, boolean bouncing) {
					this.mStartTime = System.currentTimeMillis();
					this.mStartPagePosition = NewsPagePositionManager.this.mNewsPagePosition;
					this.mDuration = DURATION_BASE;
					if (direction) {
						this.mTargetPagePosition = (int) Math.floor(this.mStartPagePosition + 0.5f) - 1;
						if (this.mTargetPagePosition < 0) {
							this.mTargetPagePosition = 0;
							if (bouncing) {
								this.mBouncing = BOUNCE_LEFT;
							}
						}
					} else {
						this.mTargetPagePosition = (int) Math.floor(this.mStartPagePosition + 0.5f) + 1;
						final int maxColumnPagePosition = NewsPageManager.this.mNewsPages.size() - 1;
						if (this.mTargetPagePosition > maxColumnPagePosition) {
							this.mTargetPagePosition = maxColumnPagePosition;
							if (bouncing) {
								this.mBouncing = BOUNCE_RIGHT;
							}
						}
					}
				}

				/**
				 * Construct a new instance.
				 * 
				 * @param targetPagePosition The target page position.
				 */
				public NewsPageAnimation(int targetPagePosition) {
					this.mStartTime = System.currentTimeMillis();
					this.mStartPagePosition = NewsPagePositionManager.this.mNewsPagePosition;
					this.mDuration = (long) (DURATION_BASE * Math.abs(this.mTargetPagePosition
					        - this.mStartPagePosition));
					this.mTargetPagePosition = targetPagePosition;
				}

				/**
				 * Append one more animate action based on the original one.
				 * 
				 * @param direction The new animate action direction, True to
				 *        animate to the left page, otherwise to the right page.
				 * @param bouncing True to add bounce effect if need, otherwise
				 *        false.
				 * @author Luo Yinzhuo
				 */
				public void animate(boolean direction, boolean bouncing) {
					this.mStartTime = System.currentTimeMillis();
					this.mStartPagePosition = NewsPagePositionManager.this.mNewsPagePosition;
					this.mDuration = DURATION_BASE;
					if (direction) {
						this.mTargetPagePosition--;
						if (this.mTargetPagePosition < 0) {
							this.mTargetPagePosition = 0;
							if (bouncing) {
								this.mBouncing = BOUNCE_LEFT;
							}
						}
					} else {
						this.mTargetPagePosition++;
						final int maxColumnPagePosition = NewsPageManager.this.mNewsPages.size() - 1;
						if (this.mTargetPagePosition > maxColumnPagePosition) {
							this.mTargetPagePosition = maxColumnPagePosition;
							if (bouncing) {
								this.mBouncing = BOUNCE_RIGHT;
							}
						}
					}
				}

				/**
				 * Get the current news page position.
				 * 
				 * @return The current news page position.
				 * @author Luo Yinzhuo
				 */
				public float getNewsPagePosition() {
					long passTime = System.currentTimeMillis() - this.mStartTime;
					if (passTime >= this.mDuration) {
						NewsPagePositionManager.this.onAnimationFinished();
						return this.mTargetPagePosition;
					} else {
						float estimateNewsPagePosition;
						if (this.mBouncing == BOUNCE_RIGHT) {
							estimateNewsPagePosition = (this.mTargetPagePosition + 2 * BOUNCING - this.mStartPagePosition)
							        * passTime / this.mDuration + this.mStartPagePosition;
							if (estimateNewsPagePosition > this.mTargetPagePosition + BOUNCING) {
								estimateNewsPagePosition = 2
								        * (this.mTargetPagePosition + BOUNCING)
								        - estimateNewsPagePosition;
							}
						} else if (this.mBouncing == BOUNCE_LEFT) {
							estimateNewsPagePosition = (this.mTargetPagePosition - 2 * BOUNCING - this.mStartPagePosition)
							        * passTime / this.mDuration + this.mStartPagePosition;
							if (estimateNewsPagePosition < this.mTargetPagePosition - BOUNCING) {
								estimateNewsPagePosition = 2
								        * (this.mTargetPagePosition - BOUNCING)
								        - estimateNewsPagePosition;
							}
						} else {
							estimateNewsPagePosition = (this.mTargetPagePosition - this.mStartPagePosition)
							        * passTime / this.mDuration + this.mStartPagePosition;
						}
						return estimateNewsPagePosition;
					}
				}
			}

			/**
			 * The news page position. In the range of [-BOUNCING,
			 * mNewsPages.size() - 1 + BOUNCING].
			 */
			private float mNewsPagePosition = 0.0f;

			/**
			 * Set the news page position.
			 * 
			 * @param newsPagePosition The new news page position.
			 * @author Luo Yinzhuo
			 */
			public void setNewsPagePosition(int newsPagePosition) {
				this.mNewsPagePosition = newsPagePosition;
			}

			/**
			 * Get the news page position.
			 * 
			 * @return The new news page position.
			 * @author Luo Yinzhuo
			 */
			public float getNewsPagePosition() {
				if (this.mNewsPageAnimation != null) {
					this.mNewsPagePosition = this.mNewsPageAnimation.getNewsPagePosition();
				}
				return this.mNewsPagePosition;
			}
		}

		@Override
        public void onImageResponseSuccess(Bitmap bitmap) {
			Log.d("NewsPageManager", "onImageResponseSuccess.");
			postInvalidate();
        }

		@Override
        public void onImageResponseFailed() {
	        // TODO Auto-generated method stub
	        
        }
	}

}
