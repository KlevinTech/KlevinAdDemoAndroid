package com.tencent.klevinDemo.ads.nativeAD;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tencent.klevin.ads.ad.AppDownloadListener;
import com.tencent.klevin.ads.ad.NativeAd;
import com.tencent.klevin.ads.ad.NativeAdRequest;
import com.tencent.klevin.ads.ad.NativeImage;
import com.tencent.klevinDemo.R;
import com.tencent.klevinDemo.ads.BaseADActivity;
import com.tencent.klevinDemo.utils.UIUtils;
import com.tencent.klevinDemo.view.LinearProgressBar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by weijieyang on 2021/8/18 11:37 上午
 * Please mailto weijieyang@tencent.com
 */
public class UnifiedImageRecyclerTestActivity extends BaseADActivity implements View.OnClickListener {
    private static final String TAG = "Image_Recycler";

    private TextView mTipsView;
    private RadioButton mPattern1Btn, mPattern2Btn;
    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;
    private List<NativeAd> mAds;
    private long posId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        posId = getIntent().getLongExtra("posId", -1);
        setContentView(R.layout.activity_unified_image_recycler_test);
        initViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAds != null) {
            for (NativeAd ad : mAds) {
                ad.destroy();
            }
        }
        mAds = null;
    }

    private void initViews() {
        mTipsView = findViewById(R.id.text_tips);
        findViewById(R.id.btn_add_ad).setOnClickListener(this);

        mRecyclerView = findViewById(R.id.list_content);
        mPattern1Btn = findViewById(R.id.rb_pattern_1);
        mPattern2Btn = findViewById(R.id.rb_pattern_2);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mAdapter = new MyAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
        initCommonView(posId);
    }

    private void loadAdInfo() {
        mTipsView.setText("");
        mAdapter.setImagePattern(mPattern1Btn.isChecked() ? MyAdapter.IMAGE_PATTERN_1 : MyAdapter.IMAGE_PATTERN_2);
        NativeAdRequest.Builder builder = new NativeAdRequest.Builder();
        builder.setPosId(getPosId());
        if (getAdCount() <= 2) {
            builder.setAdCount(getAdCount());
            NativeAd.load(builder.build(), new NativeAd.NativeAdLoadListener() {
                @Override
                public void onAdLoadError(int err, String msg) {
                    String tips = "err: " + err + " " + msg;
                    Log.e(TAG, tips);
                    mTipsView.setText(tips);
                }

                @Override
                public void onAdLoaded(List<NativeAd> ads) {
                    if (mAds == null) mAds = new ArrayList<>();
                    mAds.addAll(ads);
                    mAdapter.addData(ads);
                    mTipsView.setText(R.string.unified_recyclerview_tips_success);
                }
            });
        } else {
            showToast(getText(R.string.unified_recyclerview_tips_illegal_amount));
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_add_ad) {
            loadAdInfo();
        }
    }

    private static class MyAdapter extends RecyclerView.Adapter<MyAdapter.AdViewHolder> {

        public static final int IMAGE_PATTERN_1 = 1;
        public static final int IMAGE_PATTERN_2 = 2;

        private static final int ITEM_VIEW_TYPE_LOAD_MORE = -1;
        private static final int ITEM_VIEW_TYPE_NORMAL = 0;
        private static final int ITEM_VIEW_TYPE_IMAGE = 1;
        private static final int ITEM_VIEW_TYPE_VIDEO = 2;
        private final Map<AdViewHolder, AppDownloadListener> AppDownloadListenerMap = new WeakHashMap<>();

        private final List<NativeAd> mData;
        private final Activity mContext;

        private int mImagePattern;

        public MyAdapter(Activity context) {
            this.mContext = context;
            mData = new ArrayList<>();
        }

        public void setImagePattern(int imagePattern) {
            this.mImagePattern = imagePattern;
        }

        @SuppressLint("NotifyDataSetChanged")
        public void addData(List<NativeAd> ads) {
            mData.addAll(ads);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public AdViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            switch (viewType) {
                case ITEM_VIEW_TYPE_IMAGE:
                    return new ImageAdViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_image_view, parent, false));
                case ITEM_VIEW_TYPE_VIDEO:
                default:
                    return new NormalViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_normal_view, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(@NonNull AdViewHolder holder, int position) {
            if (holder instanceof ImageAdViewHolder) {
                ImageAdViewHolder imageAdViewHolder = (ImageAdViewHolder) holder;
                NativeAd ad = mData.get(position);
                bindCommonData(imageAdViewHolder, ad);
                bindDownloadListener(imageAdViewHolder, mData.get(position));
                registerAdInteraction(imageAdViewHolder, ad);

                if (mImagePattern == IMAGE_PATTERN_1) {
                    //使用getAdView()的方式加载广告图片
                    imageAdViewHolder.imageCustomContainer.setVisibility(View.GONE);
                    imageAdViewHolder.imageContainer.setVisibility(View.VISIBLE);
                    imageAdViewHolder.imageContainer.post(() -> {
                        int width = imageAdViewHolder.imageContainer.getWidth();
                        int videoWidth = ad.getAdViewWidth();
                        int videoHeight = ad.getAdViewHeight();
                        // 根据广告内容的宽高比，调整imageContainer的高度
                        UIUtils.setViewSize(imageAdViewHolder.imageContainer, width, (int) (width / (videoWidth / (double) videoHeight)) + 1);
                    });
                    View imageView = ad.getAdView();
                    holder.adViewIdentity = System.identityHashCode(imageView);
                    if (imageView != null) {
                        imageAdViewHolder.imageContainer.removeAllViews();
                        if (imageView.getParent() instanceof ViewGroup) {
                            ViewGroup parent = (ViewGroup) imageView.getParent();
                            parent.removeView(imageView);
                            addAdViewToContainer(imageAdViewHolder, imageView);
                        } else {
                            imageAdViewHolder.imageContainer.addView(imageView);
                        }
                    }
                } else if (mImagePattern == IMAGE_PATTERN_2) {
                    //使用自定义的ImageView加载广告图片
                    imageAdViewHolder.imageContainer.setVisibility(View.GONE);
                    if (ad.getImageList() == null || ad.getImageList().isEmpty()) return;
                    NativeImage nativeImage = ad.getImageList().get(0);
                    imageAdViewHolder.imageCustomContainer.setVisibility(View.VISIBLE);
                    imageAdViewHolder.imageCustomContainer.post(() -> {
                        int width = imageAdViewHolder.imageCustomContainer.getWidth();
                        int imageWidth = nativeImage.getWidth();
                        int imageHeight = nativeImage.getHeight();
                        // 根据广告内容的宽高比，调整imageContainer的高度
                        UIUtils.setViewSize(imageAdViewHolder.imageCustomContainer, width, (int) (width / (imageWidth / (double) imageHeight)) + 1);
                    });
                    Glide.with(imageAdViewHolder.customImageView.getContext()).load(nativeImage.getImageUrl()).into(imageAdViewHolder.customImageView);
                }
            } else if (holder instanceof NormalViewHolder) {
                NormalViewHolder normalViewHolder = (NormalViewHolder) holder;
                normalViewHolder.idle.setText(mContext.getString(R.string.item_normal_view_idle_recycler, position));
            }
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        @Override
        public int getItemViewType(int position) {
            int count = mData.size();
            if (position >= count) {
                return ITEM_VIEW_TYPE_LOAD_MORE;
            } else {
                NativeAd ad = mData.get(position);
                if (ad == null) {
                    return ITEM_VIEW_TYPE_NORMAL;
                } else if (ad.getMediaMode() == NativeAd.MEDIA_MODE_IMAGE) {
                    return ITEM_VIEW_TYPE_IMAGE;
                } else if (ad.getMediaMode() == NativeAd.MEDIA_MODE_VIDEO) {
                    return ITEM_VIEW_TYPE_VIDEO;
                } else {
                    return ITEM_VIEW_TYPE_NORMAL;
                }
            }
        }

        @SuppressLint("NotifyDataSetChanged")
        private void bindCommonData(AdViewHolder holder, NativeAd ad) {
            if (holder.icon != null) {
                Glide.with(holder.icon.getContext()).load(ad.getIcon()).into(holder.icon);
            }
            if (holder.title != null) {
                holder.title.setText(ad.getTitle());
            }
            if (holder.description != null) {
                holder.description.setText(ad.getDescription());
            }
            if (holder.logo != null) {
                holder.logo.setImageBitmap(ad.getAdLogo());
            }
            if (holder.closeBtn != null) {
                List<View> adDislikeViews = new ArrayList<>();
                adDislikeViews.add(holder.closeBtn);
                ad.registerAdDislikeViews(adDislikeViews, (view) -> {
                    Log.d(TAG, "on ad dislike, view=" + view);
                    ad.destroy();
                    mData.remove(ad);
                    notifyDataSetChanged();
                });
            }
        }

        private void bindDownloadListener(final AdViewHolder adViewHolder, NativeAd ad) {
            AppDownloadListener downloadListener = new AppDownloadListener() {
                @Override
                public void onIdle() {
                    if (!isValid()) {
                        return;
                    }
                    adViewHolder.downloadButton.setProgress(100, ad.getDownloadButtonLabel(), 12, mContext.getResources().getColor(R.color.white));
                }

                @Override
                public void onDownloadStart(long totalBytes, String fileName, String appName) {

                }

                @Override
                public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
                    if (!isValid()) {
                        return;
                    }
                    if (totalBytes <= 0L) {
                        adViewHolder.downloadButton.setProgress(0, "0%", 12, mContext.getResources().getColor(R.color.downloading_pause_text_color));
                    } else {
                        adViewHolder.downloadButton.setProgress((int) (currBytes * 100 / totalBytes), (currBytes * 100 / totalBytes) + "%", 12, mContext.getResources().getColor(R.color.downloading_pause_text_color));

                    }
                }

                @Override
                public void onDownloadPaused(long totalBytes, long currBytes, String fileName, String appName) {
                    if (!isValid()) {
                        return;
                    }
                    adViewHolder.downloadButton.setProgress((int) (currBytes * 100 / totalBytes), "恢复下载", 12, mContext.getResources().getColor(R.color.downloading_pause_text_color));
                }

                @Override
                public void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName) {
                    if (!isValid()) {
                        return;
                    }
                    adViewHolder.downloadButton.setProgress(100, "重新下载", 12, mContext.getResources().getColor(R.color.white));

                }

                @Override
                public void onInstalled(String fileName, String appName) {
                    if (!isValid()) {
                        return;
                    }
                    adViewHolder.downloadButton.setProgress(0, "启动游戏", 12, mContext.getResources().getColor(R.color.install_text_color));

                }

                @Override
                public void onDownloadFinished(long totalBytes, String fileName, String appName) {
                    if (!isValid()) {
                        return;
                    }
                    adViewHolder.downloadButton.setProgress(0, "立即安装", 12, mContext.getResources().getColor(R.color.install_text_color));

                }

                @SuppressWarnings("BooleanMethodIsAlwaysInverted")
                private boolean isValid() {
                    return AppDownloadListenerMap.get(adViewHolder) == this;
                }
            };
            //一个ViewHolder对应一个downloadListener, isValid判断当前ViewHolder绑定的listener是不是自己
            ad.setDownloadListener(downloadListener); // 注册下载监听器
            AppDownloadListenerMap.put(adViewHolder, downloadListener);
        }

        private void registerAdInteraction(AdViewHolder adViewHolder, NativeAd ad) {
            List<View> adClickViews = new ArrayList<>();
            adClickViews.add(adViewHolder.adContainer);
            // 注册广告点击事件，涉及广告计费，必须调用！而且不能再给注册的view设置OnClickListener
            ad.registerAdInteractionViews(mContext, adViewHolder.itemView, adClickViews, new NativeAd.AdInteractionListener() {
                @Override
                public void onAdShow(NativeAd ad) {
                    Log.d(TAG, "onAdShow");
                }

                @Override
                public void onAdClick(NativeAd ad, View view) {
                    Log.d(TAG, "on ad clicked, view=" + view);
                }

                @Override
                public void onAdError(NativeAd ad, int err, String msg) {
                    Log.d(TAG, "onAdError, err=" + err + " msg=" + msg);
                }

                @Override
                public void onDetailClick(NativeAd ad, View view) {
                    Log.d(TAG, "on ad detail clicked, view=" + view);
                }
            });
        }

        private void addAdViewToContainer(AdViewHolder holder, View adView) {
            if (holder instanceof ImageAdViewHolder) {
                ImageAdViewHolder imageAdViewHolder = (ImageAdViewHolder) holder;
                imageAdViewHolder.imageContainer.postDelayed(() -> {
                    int adViewIdentity = System.identityHashCode(adView);
                    if (imageAdViewHolder.adViewIdentity == adViewIdentity) {
                        //快速滑动时，ViewHolder需要承载AdView可能已经改变了
                        if (adView.getParent() instanceof ViewGroup) {
                            //AdView还有旧的父控件，重试
                            addAdViewToContainer(holder, adView);
                        } else {
                            imageAdViewHolder.imageContainer.addView(adView);
                        }
                    }
                }, 100);
            }
        }


        private static class NormalViewHolder extends AdViewHolder {
            TextView idle;

            public NormalViewHolder(View itemView) {
                super(itemView);
                idle = itemView.findViewById(R.id.tv_idle);
            }
        }

        private static class ImageAdViewHolder extends AdViewHolder {
            ViewGroup imageContainer;
            ViewGroup imageCustomContainer;
            ImageView customImageView;

            public ImageAdViewHolder(View itemView) {
                super(itemView);
                icon = itemView.findViewById(R.id.iv_ad_icon);
                title = itemView.findViewById(R.id.tv_ad_title);
                description = itemView.findViewById(R.id.tv_ad_desc);
                logo = itemView.findViewById(R.id.img_logo);
                imageContainer = itemView.findViewById(R.id.layout_ad_image_container);
                imageCustomContainer = itemView.findViewById(R.id.layout_ad_image_custom_container);
                customImageView = itemView.findViewById(R.id.img_custom);
            }
        }

        private static class AdViewHolder extends RecyclerView.ViewHolder {
            int adViewIdentity;
            ImageView icon;
            TextView title;
            TextView description;
            ImageView logo;
            LinearProgressBar downloadButton;
            LinearLayout adContainer;
            View closeBtn;

            public AdViewHolder(View itemView) {
                super(itemView);
                downloadButton = itemView.findViewById(R.id.btn_download);
                adContainer = itemView.findViewById(R.id.ll_ad_container);
                closeBtn = itemView.findViewById(R.id.iv_ad_close_icon);
            }
        }
    }
}
