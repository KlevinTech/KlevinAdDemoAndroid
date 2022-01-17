package com.tencent.klevinDemo.ads.nativeAD;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tencent.klevin.ads.ad.AppDownloadListener;
import com.tencent.klevin.ads.ad.NativeAd;
import com.tencent.klevin.ads.ad.NativeAdRequest;
import com.tencent.klevinDemo.ConfigConsts;
import com.tencent.klevinDemo.R;
import com.tencent.klevinDemo.ads.BaseADActivity;
import com.tencent.klevinDemo.utils.NoDoubleClickUtil;
import com.tencent.klevinDemo.utils.UIUtils;
import com.tencent.klevinDemo.view.LinearProgressBar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class NativeADActivity extends BaseADActivity {
    private static final String TAG = ConfigConsts.DEMO_TAG + "RecyclerTest";

    private Spinner mAutoDownloadSpinner;
    private MyAdapter mAdapter;
    private List<NativeAd> mAds;

    private TextView statusTxt;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_native_ad);
        initViews();
        initCommonView(getIntent().getLongExtra(getString(R.string.intent_pod_id), -1));
        initLog();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAds != null) {
            for (NativeAd ad : mAds) {
                ad.destroy();
            }
        }
        mAds = null;
    }

    private void initViews() {
        mAutoDownloadSpinner = findViewById(R.id.spinner_auto_download);
        statusTxt = findViewById(R.id.txt_native_status);
        findViewById(R.id.btn_add_native_ad).setOnClickListener(this::loadAdInfo);
        RecyclerView mRecyclerView = findViewById(R.id.recycler_content);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mAdapter = new MyAdapter(this, findViewById(R.id.spinner_auto_play), findViewById(R.id.spinner_register));
        mRecyclerView.setAdapter(mAdapter);
    }

    private void loadAdInfo(View v) {
        if (NoDoubleClickUtil.isDoubleClick()) {
            return;
        }
        statusTxt.setText("");
        NativeAdRequest.Builder builder = new NativeAdRequest.Builder();
        builder.setAutoDownloadPolicy(getAutoDownloadPolicy())
                .setPosId(getPosId());
        if (getAdCount() <= 2) {
            builder.setAdCount(getAdCount());
            NativeAd.load(builder.build(), new NativeAd.NativeAdLoadListener() {
                @Override
                public void onAdLoadError(int err, String msg) {
                    String tips = "err: " + err + " " + msg;
                    Log.e(TAG, tips);
                    statusTxt.setText(tips);
                }

                @Override
                public void onAdLoaded(List<NativeAd> ads) {
                    if (mAds == null) mAds = new ArrayList<>();
                    mAds.addAll(ads);
                    mAdapter.addData(ads);
                    statusTxt.setText(R.string.unified_recyclerview_tips_success);
                }
            });
        } else {
            statusTxt.setText(R.string.unified_recyclerview_tips_illegal_amount);
        }
    }

    private int getAutoDownloadPolicy() {
        int result = NativeAdRequest.AUTO_DOWNLOAD_POLICY_ALWAYS;
        if (mAutoDownloadSpinner.getSelectedItemPosition() == 0) {
            result = NativeAdRequest.AUTO_DOWNLOAD_POLICY_ALWAYS;
        } else if (mAutoDownloadSpinner.getSelectedItemPosition() == 1) {
            result = NativeAdRequest.AUTO_DOWNLOAD_POLICY_WIFI;
        }
        return result;
    }

    private static class MyAdapter extends RecyclerView.Adapter<MyAdapter.AdViewHolder> {

        private static final int ITEM_VIEW_TYPE_LOAD_MORE = -1;
        private static final int ITEM_VIEW_TYPE_NORMAL = 0;
        private static final int ITEM_VIEW_TYPE_IMAGE = 1;
        private static final int ITEM_VIEW_TYPE_VIDEO = 2;
        private final Map<MyAdapter.AdViewHolder, AppDownloadListener> AppDownloadListenerMap = new WeakHashMap<>();

        private final List<NativeAd> mData;
        private final Activity mContext;
        private Spinner autoPlaySpinner;
        private Spinner registerSpinner;

        public MyAdapter(Activity context, Spinner autoPlaySpinner, Spinner registerSpinner) {
            this.mContext = context;
            this.autoPlaySpinner = autoPlaySpinner;
            this.registerSpinner = registerSpinner;
            mData = new ArrayList<>();
            autoPlaySpinner.setSelection(1);
            registerSpinner.setSelection(0);
        }

        @SuppressLint("NotifyDataSetChanged")
        public void addData(List<NativeAd> ads) {
            mData.addAll(ads);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public MyAdapter.AdViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            switch (viewType) {
                case ITEM_VIEW_TYPE_VIDEO:
                    return new MyAdapter.VideoAdViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_video_view, parent, false));
                case ITEM_VIEW_TYPE_IMAGE:
                    return new MyAdapter.ImageAdViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_image_view, parent, false));
                default:
                    return new MyAdapter.NormalViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_normal_view, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(@NonNull MyAdapter.AdViewHolder holder, int position) {
            if (holder instanceof MyAdapter.VideoAdViewHolder) {
                MyAdapter.VideoAdViewHolder videoAdViewHolder = (MyAdapter.VideoAdViewHolder) holder;
                NativeAd ad = mData.get(position);
                bindCommonData(videoAdViewHolder, ad);
                videoAdViewHolder.muteImage.setImageResource(ad.isMute() ? R.mipmap.native_ad_mute_on : R.mipmap.native_ad_mute_off);
                videoAdViewHolder.muteImage.setOnClickListener(v -> {
                    if (ad.isMute()) {
                        ad.setMute(false);
                        videoAdViewHolder.muteImage.setImageResource(R.mipmap.native_ad_mute_off);
                    } else {
                        ad.setMute(true);
                        videoAdViewHolder.muteImage.setImageResource(R.mipmap.native_ad_mute_on);
                    }
                });
                ad.setVideoAdListener(new NativeAd.VideoAdListener() {
                    @Override
                    public void onVideoCached(NativeAd ad) {
                        Log.d(TAG, "onVideoCached");
                    }

                    @Override
                    public void onVideoLoad(NativeAd ad) {
                        Log.d(TAG, "onVideoLoad");
                    }

                    @Override
                    public void onVideoError(int what, int extra) {
                        Log.d(TAG, "onVideoError what=" + what + " extra=" + extra);
                    }

                    @Override
                    public void onVideoStartPlay(NativeAd ad) {
                        Log.d(TAG, "onVideoStartPlay");
                    }

                    @Override
                    public void onVideoPaused(NativeAd ad) {
                        Log.d(TAG, "onVideoPaused");
                    }

                    @Override
                    public void onProgressUpdate(long current, long duration) {
                        Log.d(TAG, "onProgressUpdate current=" + current + " duration=" + duration);
                    }

                    @Override
                    public void onVideoComplete(NativeAd ad) {
                        Log.d(TAG, "onVideoComplete");
                    }
                });
                ad.setAutoPlayPolicy(getAutoPlayPolicy());
                videoAdViewHolder.videoContainer.post(() -> {
                    int width = videoAdViewHolder.videoContainer.getWidth();
                    int videoWidth = ad.getAdViewWidth();
                    int videoHeight = ad.getAdViewHeight();
                    // 根据广告内容的宽高比，调整videoContainer的高度
                    UIUtils.setViewSize(videoAdViewHolder.videoContainer, width, (int) (width / (videoWidth / (double) videoHeight)));
                });
                View videoView = ad.getAdView();
                holder.adViewIdentity = System.identityHashCode(videoView);
                if (videoView != null) {
                    videoAdViewHolder.videoContainer.removeAllViews();
                    if (videoView.getParent() instanceof ViewGroup) {
                        ViewGroup parent = (ViewGroup) videoView.getParent();
                        parent.removeView(videoView);
                        addAdViewToContainer(videoAdViewHolder, videoView);
                    } else {
                        videoAdViewHolder.videoContainer.addView(videoView);
                    }
                }
                bindDownloadListener(videoAdViewHolder, videoView, mData.get(position));
            } else if (holder instanceof MyAdapter.ImageAdViewHolder) {
                MyAdapter.ImageAdViewHolder imageAdViewHolder = (MyAdapter.ImageAdViewHolder) holder;
                NativeAd ad = mData.get(position);
                bindCommonData(imageAdViewHolder, ad);
                imageAdViewHolder.imageContainer.post(() -> {
                    int width = imageAdViewHolder.imageContainer.getWidth();
                    int videoWidth = ad.getAdViewWidth();
                    int videoHeight = ad.getAdViewHeight();
                    // 根据广告内容的宽高比，调整imageContainer的高度
                    UIUtils.setViewSize(imageAdViewHolder.imageContainer, width, (int) (width / (videoWidth / (double) videoHeight)));
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
                bindDownloadListener(imageAdViewHolder, imageView, mData.get(position));
            } else if (holder instanceof MyAdapter.NormalViewHolder) {
                MyAdapter.NormalViewHolder normalViewHolder = (MyAdapter.NormalViewHolder) holder;
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
        private void bindCommonData(MyAdapter.AdViewHolder holder, NativeAd ad) {
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

        private void bindDownloadListener(final MyAdapter.AdViewHolder adViewHolder, final View adView, NativeAd ad) {
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
            List<View> adClickViews = new ArrayList<>(), detailClickViews = new ArrayList<>();
            switch (ad.getMediaMode()) {
                case NativeAd.MEDIA_MODE_IMAGE:
                    adClickViews.add(adViewHolder.adContainer);
                    break;
                case NativeAd.MEDIA_MODE_VIDEO:
                    adClickViews.add(adViewHolder.downloadButton);
                    detailClickViews.add(adViewHolder.adContainer);
                    break;
            }
            if (adView != null) {
                if (registerSpinner.getSelectedItemPosition() == 1) {
                    detailClickViews.add(adView);
                } else if (registerSpinner.getSelectedItemPosition() == 2) {
                    adClickViews.add(adView);
                }
            }
            // 注册广告点击事件，涉及广告计费，必须调用！而且不能再给注册的view设置OnClickListener
            ad.registerAdInteractionViews(mContext, adViewHolder.itemView, adClickViews, detailClickViews, new NativeAd.AdInteractionListener() {
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
            AppDownloadListenerMap.put(adViewHolder, downloadListener);
        }

        private void addAdViewToContainer(AdViewHolder holder, View adView) {
            if (holder instanceof VideoAdViewHolder) {
                VideoAdViewHolder videoAdViewHolder = (VideoAdViewHolder) holder;
                videoAdViewHolder.videoContainer.postDelayed(() -> {
                    int adViewIdentity = System.identityHashCode(adView);
                    if (videoAdViewHolder.adViewIdentity == adViewIdentity) {
                        //快速滑动时，ViewHolder需要承载AdView可能已经改变了
                        if (adView.getParent() instanceof ViewGroup) {
                            //AdView还有旧的父控件，重试
                            addAdViewToContainer(holder, adView);
                        } else {
                            videoAdViewHolder.videoContainer.addView(adView);
                        }
                    }
                }, 100);
            } else if (holder instanceof ImageAdViewHolder) {
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

        private int getAutoPlayPolicy() {
            int result = NativeAd.AUTO_PLAY_POLICY_ALWAYS;
            if (autoPlaySpinner != null) {
                if (autoPlaySpinner.getSelectedItemPosition() == 0) {
                    result = NativeAd.AUTO_PLAY_POLICY_WIFI;
                } else if (autoPlaySpinner.getSelectedItemPosition() == 1) {
                    result = NativeAd.AUTO_PLAY_POLICY_ALWAYS;
                } else if (autoPlaySpinner.getSelectedItemPosition() == 2) {
                    result = NativeAd.AUTO_PLAY_POLICY_NEVER;
                }
            }
            return result;
        }


        private static class NormalViewHolder extends MyAdapter.AdViewHolder {
            TextView idle;

            public NormalViewHolder(View itemView) {
                super(itemView);
                idle = itemView.findViewById(R.id.tv_idle);
            }
        }

        private static class VideoAdViewHolder extends MyAdapter.AdViewHolder {
            ViewGroup videoContainer;
            ImageView muteImage;

            public VideoAdViewHolder(View itemView) {
                super(itemView);
                icon = itemView.findViewById(R.id.iv_ad_icon);
                title = itemView.findViewById(R.id.tv_ad_title);
                description = itemView.findViewById(R.id.tv_ad_desc);
                logo = itemView.findViewById(R.id.img_logo);
                videoContainer = itemView.findViewById(R.id.layout_ad_video_container);
                muteImage = itemView.findViewById(R.id.iv_ad_mute_icon);
            }
        }

        private static class ImageAdViewHolder extends MyAdapter.AdViewHolder {
            ViewGroup imageContainer;

            public ImageAdViewHolder(View itemView) {
                super(itemView);
                icon = itemView.findViewById(R.id.iv_ad_icon);
                title = itemView.findViewById(R.id.tv_ad_title);
                description = itemView.findViewById(R.id.tv_ad_desc);
                logo = itemView.findViewById(R.id.img_logo);
                imageContainer = itemView.findViewById(R.id.layout_ad_image_container);
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